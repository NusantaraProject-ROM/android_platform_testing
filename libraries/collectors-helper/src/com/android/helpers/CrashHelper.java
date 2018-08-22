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

import com.android.os.AtomsProto.AppCrashOccurred;
import com.android.os.AtomsProto.AppCrashOccurred.ForegroundState;
import com.android.os.AtomsProto.Atom;
import com.android.os.StatsLog.EventMetricData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CrashHelper consist of helper methods to set the crash collection
 * configs in statsd to track the crashes happened during the test
 * and retrieve the necessary information from statsd using the config id.
 */
public class CrashHelper implements ICollectorHelper<Integer> {

    private static final String LOG_TAG = CrashHelper.class.getSimpleName();
    private static final String TOTAL_JAVA_CRASHES = "crash";
    private static final String TOTAL_NATIVE_CRASHES = "native_crash";

    private StatsdHelper mStatsdHelper = new StatsdHelper();

    /**
     * Set up the app crash statsd config to track the crash metrics during the test.
     */
    @Override
    public boolean startCollecting() {
        Log.i(LOG_TAG, "Adding AppCrashOccured config to statsd.");
        return mStatsdHelper.addEventConfig(Atom.APP_CRASH_OCCURRED_FIELD_NUMBER);
    }

    /**
     * Collect the app crash metrics tracked during the test from the statsd.
     */
    @Override
    public Map<String, Integer> getMetrics() {
        List<EventMetricData> eventMetricData = mStatsdHelper.getEventMetrics();
        Map<String, Integer> appCrashResultMap = new HashMap<>();
        // We need this because even if there are no crashes we need to report 0 count
        // in the dashboard for the total crash and native crash.
        appCrashResultMap.put(TOTAL_JAVA_CRASHES, 0);
        appCrashResultMap.put(TOTAL_NATIVE_CRASHES, 0);
        for (EventMetricData dataItem : eventMetricData) {
            AppCrashOccurred appCrashAtom = dataItem.getAtom().getAppCrashOccurred();
            String eventType = appCrashAtom.getEventType();
            String pkgName = appCrashAtom.getPackageName();
            ForegroundState foregroundState = appCrashAtom.getForegroundState();
            Log.i(LOG_TAG, String.format("Event Type:%s Pkg Name: %s "
                    + " ForegroundState: %s", eventType, pkgName, foregroundState.toString()));

            // Track the total crash and native crash count.
            MetricUtility.addMetric(eventType, appCrashResultMap);
            // Add more detailed crash count key metrics.
            String finalKey = MetricUtility.constructKey(eventType, pkgName,
                    foregroundState.toString());
            MetricUtility.addMetric(finalKey, appCrashResultMap);
        }
        return appCrashResultMap;
    }

    /**
     * Remove the statsd config used to track the app crash metrics.
     */
    @Override
    public boolean stopCollecting() {
        return mStatsdHelper.removeStatsConfig();
    }
}
