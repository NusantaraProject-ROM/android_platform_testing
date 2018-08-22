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

import android.content.Context;
import android.app.StatsManager;
import android.app.StatsManager.StatsUnavailableException;
import android.util.Log;
import androidx.test.InstrumentationRegistry;

import com.android.internal.os.StatsdConfigProto.AtomMatcher;
import com.android.internal.os.StatsdConfigProto.EventMetric;
import com.android.internal.os.StatsdConfigProto.SimpleAtomMatcher;
import com.android.internal.os.StatsdConfigProto.StatsdConfig;
import com.android.os.StatsLog.ConfigMetricsReport;
import com.android.os.StatsLog.ConfigMetricsReportList;
import com.android.os.StatsLog.EventMetricData;
import com.android.os.StatsLog.StatsLogReport;

import java.util.ArrayList;
import java.util.List;

/**
 * StatsdHelper consist of basic utilities that will be used to setup statsd
 * config, parse the collected information and remove the statsd config.
 */
public class StatsdHelper {

    private static final String LOG_TAG = StatsdHelper.class.getSimpleName();
    private long mConfigId = -1;
    private StatsManager mStatsManager;

    /**
     * Add simple event configuration using atom id.
     *
     * @param atomId uniquely identifies the information that we need to track by StatsManger.
     * @return true if the configuration is added successfully otherwise false.
     */
    public boolean addEventConfig(int atomId) {
        long configId = System.currentTimeMillis();
        String atomName = "Atom" + System.currentTimeMillis();
        String eventName = "Event" + System.currentTimeMillis();
        StatsdConfig config = getSimpleSources(configId)
                .addEventMetric(
                        EventMetric.newBuilder()
                                .setId(eventName.hashCode())
                                .setWhat(atomName.hashCode())
                )
                .addAtomMatcher(
                        AtomMatcher.newBuilder()
                                .setId(atomName.hashCode())
                                .setSimpleAtomMatcher(
                                        SimpleAtomMatcher.newBuilder()
                                                .setAtomId(atomId)
                                )
                )
                .build();
        try {
            getStatsManager().addConfig(configId, config.toByteArray());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Not able to setup the config due to :", e);
            return false;
        }
        Log.i(LOG_TAG, "Successfully added config with config-id:" + configId);
        setConfigId(configId);
        return true;
    }

    /**
     * List of authorized source that can write the information into statsd.
     *
     * @param configId unique id of the configuration tracked by StatsManager.
     * @return
     */
    private static StatsdConfig.Builder getSimpleSources(long configId) {
        return StatsdConfig.newBuilder().setId(configId)
                .addAllowedLogSource("AID_ROOT")
                .addAllowedLogSource("AID_SYSTEM")
                .addAllowedLogSource("AID_RADIO")
                .addAllowedLogSource("AID_BLUETOOTH")
                .addAllowedLogSource("AID_GRAPHICS")
                .addAllowedLogSource("AID_STATSD")
                .addAllowedLogSource("AID_INCIENTD");
    }

    /**
     * Returns the list of EventMetricData tracked under the config.
     */
    public List<EventMetricData> getEventMetrics() {
        ConfigMetricsReportList reportList = null;
        List<EventMetricData> eventData = new ArrayList<>();
        try {
            if (getConfigId() != -1) {
                reportList = ConfigMetricsReportList.parser()
                        .parseFrom(getStatsManager().getReports(getConfigId()));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "getData failed", e);
            return eventData;
        }

        if (reportList != null) {
            ConfigMetricsReport configReport = reportList.getReports(0);
            for (StatsLogReport metric : configReport.getMetricsList()) {
                eventData.addAll(metric.getEventMetrics().getDataList());
            }
        }
        Log.i(LOG_TAG, "Number of events: " + eventData.size());
        return eventData;
    }

    /**
     * Remove the existing config tracked in the statsd.
     *
     * @return true if the config is removed successfully otherwise false.
     */
    public boolean removeStatsConfig() {
        Log.i(LOG_TAG, "Removing statsd config-id: " + getConfigId());
        try {
            getStatsManager().removeConfig(getConfigId());
            Log.i(LOG_TAG, "Successfully removed config-id: " + getConfigId());
            return true;
        } catch (StatsUnavailableException e) {
            Log.e(LOG_TAG, String.format("Not able to remove the config-id: %d due to %s ",
                    getConfigId(), e.getMessage()));
            return false;
        }
    }

    /**
     * StatsManager used to configure, collect and remove the statsd config.
     *
     * @return StatsManager
     */
    private StatsManager getStatsManager() {
        if (mStatsManager == null) {
            mStatsManager = (StatsManager) InstrumentationRegistry.getTargetContext().
                    getSystemService(Context.STATS_MANAGER);
        }
        return mStatsManager;
    }

    /**
     * Set the config id tracked in the statsd.
     */
    private void setConfigId(long configId) {
        mConfigId = configId;
    }

    /**
     * Return the config id tracked in the statsd.
     */
    private long getConfigId() {
        return mConfigId;
    }
}
