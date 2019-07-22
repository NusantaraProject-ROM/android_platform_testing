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

import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import androidx.test.InstrumentationRegistry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FreeMemHelper is a helper to parse the free memory based on total memory available from
 * proc/meminfo, private dirty and private clean information of the cached processes from dumpsys
 * meminfo.
 *
 * Example Usage:
 * freeMemHelper.startCollecting();
 * freeMemHelper.getMetrics();
 * freeMemHelper.stopCollecting();
 */
public class FreeMemHelper implements ICollectorHelper<Long> {
    private static final String TAG = FreeMemHelper.class.getSimpleName();
    private static final String SEPARATOR = "\\s+";
    private static final String CACHED_PROCESSES =
            "dumpsys meminfo|awk '/Total PSS by category:"
                    + "/{found=0} {if(found) print} /: Cached/{found=1}'|tr -d ' '";
    private static final String PROC_MEMINFO = "cat /proc/meminfo";
    private static final String LINE_SEPARATOR = "\\n";
    private static final String MEM_AVAILABLE_PATTERN = "^MemAvailable.*";
    private static final Pattern PID_PATTERN = Pattern.compile("^.*pid(?<processid>[0-9]*).*$");
    private static final String DUMPSYS_PROCESS = "dumpsys meminfo %s";
    private static final String MEM_TOTAL = "^\\s+TOTAL\\s+.*";
    private static final String PROCESS_ID = "processid";
    public static final String MEM_AVAILABLE_CACHE_PROC_DIRTY = "MemAvailable_CacheProcDirty_kb";

    private UiDevice mUiDevice;

    @Override
    public boolean startCollecting() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        return true;
    }

    @Override
    public boolean stopCollecting() {
        return true;
    }

    @Override
    public Map<String, Long> getMetrics() {
        String memInfo;
        try {
            memInfo = mUiDevice.executeShellCommand(PROC_MEMINFO);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to read " + PROC_MEMINFO + ".", ioe);
            return null;
        }

        Pattern memAvailablePattern = Pattern.compile(MEM_AVAILABLE_PATTERN, Pattern.MULTILINE);
        Matcher memAvailableMatcher = memAvailablePattern.matcher(memInfo);

        String[] memAvailable = null;
        if (memAvailableMatcher.find()) {
            memAvailable = memAvailableMatcher.group(0).split(SEPARATOR);
        }

        if (memAvailable == null) {
            Log.e(TAG, "MemAvailable is null.");
            return null;
        }
        long cacheProcDirty = Long.parseLong(memAvailable[1]);

        String cachedProcesses;
        try {
            cachedProcesses = mUiDevice.executeShellCommand(CACHED_PROCESSES);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to find cached processes.", ioe);
            return null;
        }

        String[] processes = cachedProcesses.split(LINE_SEPARATOR);

        for (String process : processes) {
            Matcher match;
            if (((match = matches(PID_PATTERN, process))) != null) {
                String processId = match.group(PROCESS_ID);
                String processDumpSysMemInfo = String.format(DUMPSYS_PROCESS, processId);
                String processInfoStr;

                try {
                    processInfoStr = mUiDevice.executeShellCommand(processDumpSysMemInfo);
                } catch (IOException ioe) {
                    Log.e(TAG, "Failed to get " + processDumpSysMemInfo + ".", ioe);
                    return null;
                }

                Pattern memTotalPattern = Pattern.compile(MEM_TOTAL, Pattern.MULTILINE);
                Matcher memTotalMatcher = memTotalPattern.matcher(processInfoStr);

                String[] processInfo = null;
                if (memTotalMatcher.find()) {
                    processInfo = memTotalMatcher.group(0).split(LINE_SEPARATOR);
                }
                if (processInfo != null && processInfo.length > 0) {
                    String[] procDetails = processInfo[0].trim().split(SEPARATOR);
                    int privateDirty = Integer.parseInt(procDetails[2].trim());
                    int privateClean = Integer.parseInt(procDetails[3].trim());
                    cacheProcDirty = cacheProcDirty + privateDirty + privateClean;
                    Log.d(TAG, "Cached process: " + process + " Private Dirty: "
                            + privateDirty + " Private Clean: " + privateClean);
                }
            }
        }

        HashMap<String, Long> memAvailableCacheProcDirty = new HashMap<>(1);
        memAvailableCacheProcDirty.put(MEM_AVAILABLE_CACHE_PROC_DIRTY, cacheProcDirty);
        return memAvailableCacheProcDirty;
    }

    /**
     * Checks whether {@code line} matches the given {@link Pattern}.
     *
     * @return The resulting {@link Matcher} obtained by matching the {@code line} against {@code
     * pattern}, or null if the {@code line} does not match.
     */
    private static Matcher matches(Pattern pattern, String line) {
        Matcher ret = pattern.matcher(line);
        return ret.matches() ? ret : null;
    }
}
