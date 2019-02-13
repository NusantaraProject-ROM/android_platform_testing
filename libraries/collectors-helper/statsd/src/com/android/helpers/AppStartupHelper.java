/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.util.Log;

import com.android.os.AtomsProto.AppStartFullyDrawn;
import com.android.os.AtomsProto.AppStartOccurred;
import com.android.os.AtomsProto.Atom;
import com.android.os.StatsLog.EventMetricData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AppStartupHelper consist of helper methods to set the app
 * startup configs in statsd to track the app startup related
 * performance metrics and retrieve the necessary information from
 * statsd using the config id.
 */
public class AppStartupHelper implements ICollectorHelper<StringBuilder> {

    private static final String LOG_TAG = AppStartupHelper.class.getSimpleName();

    private static final String COLD_STARTUP = "cold_startup";
    private static final String WARM_STARTUP = "warm_startup";
    private static final String HOT_STARTUP = "hot_startup";
    private static final String COUNT = "count";
    private static final String TOTAL_COUNT = "total_count";

    private static final String STARTUP_FULLY_DRAWN_UNKNOWN = "startup_fully_drawn_unknown";
    private static final String STARTUP_FULLY_DRAWN_WITH_BUNDLE = "startup_fully_drawn_with_bundle";
    private static final String STARTUP_FULLY_DRAWN_WITHOUT_BUNDLE =
            "startup_fully_drawn_without_bundle";

    private StatsdHelper mStatsdHelper = new StatsdHelper();

    /**
     * Set up the app startup statsd config to track the metrics during the app start occurred.
     */
    @Override
    public boolean startCollecting() {
        Log.i(LOG_TAG, "Adding AppStartOccured config to statsd.");
        List<Integer> atomIdList = new ArrayList<>();
        atomIdList.add(Atom.APP_START_OCCURRED_FIELD_NUMBER);
        atomIdList.add(Atom.APP_START_FULLY_DRAWN_FIELD_NUMBER);
        return mStatsdHelper.addEventConfig(atomIdList);
    }

    /**
     * Collect the app startup metrics tracked during the app startup occurred from the statsd.
     */
    @Override
    public Map<String, StringBuilder> getMetrics() {
        List<EventMetricData> eventMetricData = mStatsdHelper.getEventMetrics();
        Map<String, StringBuilder> appStartResultMap = new HashMap<>();
        Map<String, Integer> appStartCountMap = new HashMap<>();
        for (EventMetricData dataItem : eventMetricData) {
            Atom atom = dataItem.getAtom();
            if (atom.hasAppStartOccurred()) {
                AppStartOccurred appStartAtom = atom.getAppStartOccurred();
                String pkgName = appStartAtom.getPkgName();
                String transitionType = appStartAtom.getType().toString();
                int windowsDrawnMillis = appStartAtom.getWindowsDrawnDelayMillis();
                Log.i(LOG_TAG, String.format("Pkg Name: %s, Transition Type: %s, "
                        + "WindowDrawnDelayMillis: %s",
                        pkgName, transitionType, windowsDrawnMillis));

                String metricKey = "";
                // To track number of startups per type per package.
                String metricCountKey = "";
                // To track total number of startups per type.
                String totalCountKey = "";
                switch (appStartAtom.getType()) {
                    case COLD:
                        metricKey = MetricUtility.constructKey(COLD_STARTUP, pkgName);
                        metricCountKey = MetricUtility.constructKey(COLD_STARTUP, COUNT, pkgName);
                        totalCountKey = MetricUtility.constructKey(COLD_STARTUP, TOTAL_COUNT);
                        break;
                    case WARM:
                        metricKey = MetricUtility.constructKey(WARM_STARTUP, pkgName);
                        metricCountKey = MetricUtility.constructKey(WARM_STARTUP, COUNT, pkgName);
                        totalCountKey = MetricUtility.constructKey(WARM_STARTUP, TOTAL_COUNT);
                        break;
                    case HOT:
                        metricKey = MetricUtility.constructKey(HOT_STARTUP, pkgName);
                        metricCountKey = MetricUtility.constructKey(HOT_STARTUP, COUNT, pkgName);
                        totalCountKey = MetricUtility.constructKey(HOT_STARTUP, TOTAL_COUNT);
                        break;
                    case UNKNOWN:
                        break;
                }
                if (!metricKey.isEmpty()) {
                    MetricUtility.addMetric(metricKey, windowsDrawnMillis, appStartResultMap);
                    MetricUtility.addMetric(metricCountKey, appStartCountMap);
                    MetricUtility.addMetric(totalCountKey, appStartCountMap);
                }
            } else if (atom.hasAppStartFullyDrawn()) {
                AppStartFullyDrawn appFullyDrawnAtom = atom.getAppStartFullyDrawn();
                String pkgName = appFullyDrawnAtom.getPkgName();
                String transitionType = appFullyDrawnAtom.getType().toString();
                long startupTimeMillis = appFullyDrawnAtom.getAppStartupTimeMillis();
                Log.i(LOG_TAG, String.format("Pkg Name: %s, Transition Type: %s, "
                        + "AppStartupTimeMillis: %d", pkgName, transitionType, startupTimeMillis));

                String metricKey = "";
                switch (appFullyDrawnAtom.getType()) {
                    case UNKNOWN:
                        metricKey = MetricUtility.constructKey(
                                STARTUP_FULLY_DRAWN_UNKNOWN, pkgName);
                        break;
                    case WITH_BUNDLE:
                        metricKey = MetricUtility.constructKey(
                                STARTUP_FULLY_DRAWN_WITH_BUNDLE, pkgName);
                        break;
                    case WITHOUT_BUNDLE:
                        metricKey = MetricUtility.constructKey(
                                STARTUP_FULLY_DRAWN_WITHOUT_BUNDLE, pkgName);
                        break;
                }
                if (!metricKey.isEmpty()) {
                    MetricUtility.addMetric(metricKey, startupTimeMillis, appStartResultMap);
                }
            }
        }
        // Cast to StringBuilder as the raw app startup metric could be comma separated values
        // if there are multiple app launches.
        Map<String, StringBuilder> finalCountMap = appStartCountMap
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(Map.Entry::getKey,
                                e -> new StringBuilder(Integer.toString(e.getValue()))));
        // Add the count map in the app start result map.
        appStartResultMap.putAll(finalCountMap);
        return appStartResultMap;
    }

    /**
     * Remove the statsd config used to track the app startup metrics.
     */
    @Override
    public boolean stopCollecting() {
        return mStatsdHelper.removeStatsConfig();
    }
}
