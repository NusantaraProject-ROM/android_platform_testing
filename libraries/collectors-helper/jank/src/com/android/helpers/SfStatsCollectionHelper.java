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

import android.support.test.uiautomator.UiDevice;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.test.InstrumentationRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link ICollectorHelper} for collecting SurfaceFlinger time stats. This parses the output of
 * {@code dumpsys SurfaceFlinger --timestats} and returns global metrics like total frames, missed
 * frames, client composition frames. It also parses the output for layers and returns metrics for
 * average fps, dropped frames, and total frames.
 */
public class SfStatsCollectionHelper implements ICollectorHelper<Double> {

    private static final String LOG_TAG = SfStatsCollectionHelper.class.getSimpleName();

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^(\\w+)\\s+=\\s+(\\S+)");

    @VisibleForTesting static final String SFSTATS_METRICS_PREFIX = "SFSTATS";

    @VisibleForTesting static final String SFSTATS_COMMAND = "dumpsys SurfaceFlinger --timestats ";

    @VisibleForTesting
    static final String SFSTATS_COMMAND_ENABLE_AND_CLEAR = SFSTATS_COMMAND + "-enable -clear";

    @VisibleForTesting static final String SFSTATS_COMMAND_DUMP = SFSTATS_COMMAND + "-dump";

    @VisibleForTesting
    static final String SFSTATS_COMMAND_DISABLE_AND_CLEAR = SFSTATS_COMMAND + "-disable -clear";

    private UiDevice mDevice;

    @Override
    public boolean startCollecting() {
        try {
            getDevice().executeShellCommand(SFSTATS_COMMAND_ENABLE_AND_CLEAR);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Encountered exception enabling dumpsys SurfaceFlinger.", e);
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public Map<String, Double> getMetrics() {
        Map<String, Double> results = new HashMap<>();
        String output;
        try {
            output = getDevice().executeShellCommand(SFSTATS_COMMAND_DUMP);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Encountered exception calling dumpsys SurfaceFlinger.", e);
            throw new RuntimeException(e);
        }
        String[] blocks = output.split("\n\n");

        HashMap<String, String> globalPairs = getStatPairs(blocks[0]);

        for (String key : globalPairs.keySet()) {
            String metricKey = constructKey(SFSTATS_METRICS_PREFIX, "GLOBAL", key.toUpperCase());

            results.put(metricKey, Double.valueOf(globalPairs.get(key)));
        }

        for (int i = 1; i < blocks.length; i++) {
            HashMap<String, String> layerPairs = getStatPairs(blocks[i]);
            String layerName = layerPairs.get("layerName");
            String totalFrames = layerPairs.get("totalFrames");
            String droppedFrames = layerPairs.get("droppedFrames");
            String averageFPS = layerPairs.get("averageFPS");
            results.put(
                    constructKey(SFSTATS_METRICS_PREFIX, layerName, "TOTAL_FRAMES"),
                    Double.valueOf(totalFrames));
            results.put(
                    constructKey(SFSTATS_METRICS_PREFIX, layerName, "DROPPED_FRAMES"),
                    Double.valueOf(droppedFrames));
            results.put(
                    constructKey(SFSTATS_METRICS_PREFIX, layerName, "AVERAGE_FPS"),
                    Double.valueOf(averageFPS));
        }

        return results;
    }

    @Override
    public boolean stopCollecting() {
        try {
            getDevice().executeShellCommand(SFSTATS_COMMAND_DISABLE_AND_CLEAR);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Encountered exception disabling dumpsys SurfaceFlinger.", e);
            throw new RuntimeException(e);
        }
        return true;
    }

    /** Returns the {@link UiDevice} under test. */
    @VisibleForTesting
    protected UiDevice getDevice() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }
        return mDevice;
    }

    /**
     * Returns a map of key-value pairs for every line of timestats for each layer handled by
     * SurfaceFlinger as well a some global SurfaceFlinger stats. An output line like {@code
     * totalFrames = 42} would get parsed and be accessable as {@code pairs.get("totalFrames") =>
     * "42"}
     */
    private HashMap<String, String> getStatPairs(String block) {
        HashMap<String, String> pairs = new HashMap<>();
        String[] lines = block.split("\n");
        for (int j = 0; j < lines.length; j++) {
            Matcher keyValueMatcher = KEY_VALUE_PATTERN.matcher(lines[j].trim());
            if (keyValueMatcher.matches()) {
                pairs.put(keyValueMatcher.group(1), keyValueMatcher.group(2));
            }
        }
        return pairs;
    }
}
