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

import com.android.os.AtomsProto.AppStartOccurred;
import com.android.os.AtomsProto.AppStartOccurred.TransitionType;
import com.android.os.AtomsProto.Atom;
import com.android.os.StatsLog.EventMetricData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AppStartupHelper consist of helper methods to set the app
 * startup configs in statsd to track the app startup related
 * performance metrics and retrieve the necessary information from
 * statsd using the config id.
 */
public class AppStartupHelper implements ICollectorHelper<StringBuilder> {

    private static final String LOG_TAG = AppStartupHelper.class.getSimpleName();
    private static final String COLD_STARTUP = "cold_startup";

    private StatsdHelper mStatsdHelper = new StatsdHelper();

    /**
     * Set up the app startup statsd config to track the metrics during the app start occurred.
     */
    @Override
    public boolean startCollecting() {
        Log.i(LOG_TAG, "Adding AppStartOccured config to statsd.");
        List<Integer> atomIdList = new ArrayList<>();
        atomIdList.add(Atom.APP_START_OCCURRED_FIELD_NUMBER);
        return mStatsdHelper.addEventConfig(atomIdList);
    }

    /**
     * Collect the app startup metrics tracked during the app startup occurred from the statsd.
     */
    @Override
    public Map<String, StringBuilder> getMetrics() {
        List<EventMetricData> eventMetricData = mStatsdHelper.getEventMetrics();
        Map<String, StringBuilder> appStartResultMap = new HashMap<>();
        for (EventMetricData dataItem : eventMetricData) {
            AppStartOccurred appStartAtom = dataItem.getAtom().getAppStartOccurred();
            String pkgName = appStartAtom.getPkgName();
            String transitionType = appStartAtom.getType().toString();
            int windowsDrawnMillis = appStartAtom.getWindowsDrawnDelayMillis();
            Log.i(LOG_TAG, String.format("Pkg Name: %s, Transition Type: %s,"
                    + " WindowDrawnDelayMillis:%s", pkgName, transitionType, windowsDrawnMillis));

            // Track the cold app startup time.
            if (appStartAtom.getType().equals(TransitionType.COLD)) {
                String metricKey = MetricUtility.constructKey(COLD_STARTUP,
                        appStartAtom.getPkgName());
                MetricUtility
                        .addMetric(metricKey.toString(), windowsDrawnMillis, appStartResultMap);
            }
        }
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
