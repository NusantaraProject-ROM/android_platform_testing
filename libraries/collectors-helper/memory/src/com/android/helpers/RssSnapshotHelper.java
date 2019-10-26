/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.helpers;

import static com.android.helpers.MetricUtility.constructKey;

import android.icu.text.NumberFormat;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;
import androidx.test.InstrumentationRegistry;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

/**
 * Helper to collect rss snapshot for a list of processes.
 */
public class RssSnapshotHelper implements ICollectorHelper<String> {
  private static final String TAG = RssSnapshotHelper.class.getSimpleName();

  private static final String DROP_CACHES_CMD = "echo %d > /proc/sys/vm/drop_caches";
  private static final String PIDOF_CMD = "pidof %s";
  private static final String SHOWMAP_CMD = "showmap -v %d";

  public static final String RSS_METRIC_PREFIX = "showmap_rss_bytes";
  public static final String OUTPUT_FILE_PATH_KEY = "showmap_output_file";

  private String[] mProcessNames = null;
  private String mTestOutputDir = null;
  private String mTestOutputFile = null;

  private int mDropCacheOption;
  private UiDevice mUiDevice;

  // Map to maintain per-process rss.
  private Map<String, String> mRssMap = new HashMap<>();

  public void setUp(String testOutputDir, String... processNames) {
    mProcessNames = processNames;
    mTestOutputDir = testOutputDir;
    mDropCacheOption = 0;
    mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
  }

  @Override
  public boolean startCollecting() {
    if (mTestOutputDir == null || mProcessNames == null) {
      Log.e(TAG, String.format("Invalid test setup"));
      return false;
    }

    File directory = new File(mTestOutputDir);
    String filePath =
        String.format("%s/rss_snapshot%d.txt", mTestOutputDir, UUID.randomUUID().hashCode());
    File file = new File(filePath);

    // Make sure directory exists and file does not
    if (directory.exists()) {
      if (file.exists() && !file.delete()) {
        Log.e(TAG, String.format("Failed to delete result output file %s", filePath));
        return false;
      }
    } else {
      if (!directory.mkdirs()) {
        Log.e(TAG, String.format("Failed to create result output directory %s", mTestOutputDir));
        return false;
      }
    }

    // Create an empty file to fail early in case there are no write permissions
    try {
      if (!file.createNewFile()) {
        // This should not happen unless someone created the file right after we deleted it
        Log.e(TAG, String.format("Race with another user of result output file %s", filePath));
        return false;
      }
    } catch (IOException e) {
      Log.e(TAG, String.format("Failed to create result output file %s", filePath), e);
      return false;
    }

    mTestOutputFile = filePath;
    return true;
  }

  @Override
  public Map<String, String> getMetrics() {
    try {
      // Drop cache if requested
      if (mDropCacheOption > 0) {
        dropCache(mDropCacheOption);
      }

      if (mProcessNames.length == 0) {
        // No processes specified, just return
        return mRssMap;
      }

      FileWriter writer = new FileWriter(new File(mTestOutputFile), true);
      for (String processName : mProcessNames) {
        long pid, rss;
        String showmapOutput;

        // Collect required data
        try {
          pid = getPid(processName);
          showmapOutput = execShowMap(processName, pid);
          rss = extractTotalRss(processName, showmapOutput);
        } catch (RuntimeException e) {
          Log.e(TAG, e.getMessage(), e.getCause());
          // Skip this process and continue with the next one
          continue;
        }

        // Store showmap output into file
        storeToFile(mTestOutputFile, processName, pid, showmapOutput, writer);

        // Store metrics
        mRssMap.put(constructKey(RSS_METRIC_PREFIX, processName), Long.toString(rss * 1024));
      }
      writer.close();
      mRssMap.put(OUTPUT_FILE_PATH_KEY, mTestOutputFile);
    } catch (RuntimeException e) {
      Log.e(TAG, e.getMessage(), e.getCause());
    } catch (IOException e) {
      Log.e(TAG, String.format("Failed to write output file %s", mTestOutputFile), e);
    }

    return mRssMap;
  }

  @Override
  public boolean stopCollecting() {
    return true;
  }

  /**
   * Set drop cache option.
   *
   * @param dropCacheOption drop pagecache (1), slab (2) or all (3) cache
   * @return true on success, false if input option is invalid
   */
  public boolean setDropCacheOption(int dropCacheOption) {
    // Valid values are 1..3
    if (dropCacheOption < 1 || dropCacheOption > 3) {
      return false;
    }

    mDropCacheOption = dropCacheOption;
    return true;
  }

  /**
   * Drops kernel memory cache.
   *
   * @param cacheOption drop pagecache (1), slab (2) or all (3) caches
   */
  private void dropCache(int cacheOption) throws RuntimeException {
    try {
      mUiDevice.executeShellCommand(String.format(DROP_CACHES_CMD, cacheOption));
    } catch (IOException e) {
      throw new RuntimeException("Unable to drop caches", e);
    }
  }

  /**
   * Get pid of the process with {@code processName} name.
   *
   * @param processName name of the process to get pid
   * @return pid of the specified process
   */
  private int getPid(String processName) throws RuntimeException {
    try {
      // Note that only the first pid returned by "pidof" will be used.
      String pidofOutput = mUiDevice.executeShellCommand(String.format(PIDOF_CMD, processName));
      return NumberFormat.getInstance().parse(pidofOutput).intValue();
    } catch (IOException | ParseException e) {
      throw new RuntimeException(String.format("Unable to get pid of %s ", processName), e);
    }
  }

  /**
   * Executes showmap command for the process with {@code processName} name and {@code pid} pid.
   *
   * @param processName name of the process to run showmap for
   * @param pid pid of the process to run showmap for
   * @return the output of showmap command
   */
  private String execShowMap(String processName, long pid) throws IOException {
    try {
      return mUiDevice.executeShellCommand(String.format(SHOWMAP_CMD, pid));
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Unable to execute showmap command for %s ", processName), e);
    }
  }

  /**
   * Extract total RSS from showmap command output for the process with {@code processName} name.
   *
   * @param processName name of the process to extract RSS for
   * @param showmapOutput showmap command output
   * @return total RSS of the process
   */
  private long extractTotalRss(String processName, String showmapOutput) throws RuntimeException {
    try {
      int pos = showmapOutput.lastIndexOf("----");
      Scanner sc = new Scanner(showmapOutput.substring(pos));
      sc.next();
      sc.nextLong();
      return sc.nextLong();
    } catch (IndexOutOfBoundsException | InputMismatchException e) {
      throw new RuntimeException(
          String.format("Unexpected showmap format for %s ", processName), e);
    }
  }

  /**
   * Store test results for one process into file.
   *
   * @param fileName name of the file being written
   * @param processName name of the process
   * @param pid pid of the process
   * @param showmapOutput showmap command output
   * @param writer file writer to write the data
   */
  private void storeToFile(String fileName, String processName, long pid, String showmapOutput,
      FileWriter writer) throws RuntimeException {
    try {
      writer.write(String.format(">>> %s (%d) <<<\n", processName, pid));
      writer.write(showmapOutput);
      writer.write('\n');
    } catch (IOException e) {
      throw new RuntimeException(String.format("Unable to write file %s ", fileName), e);
    }
  }
}
