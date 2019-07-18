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

import android.os.TemperatureTypeEnum;
import android.util.Log;
import androidx.annotation.VisibleForTesting;

import com.android.os.AtomsProto.Atom;
import com.android.os.AtomsProto.ThermalThrottlingSeverityStateChanged;
import com.android.os.StatsLog.EventMetricData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ThermalHelper is a helper class to collect thermal events from statsd. Currently, it identifies
 * severity state changes.
 */
public class ThermalHelper implements ICollectorHelper<StringBuilder> {
    private static final String LOG_TAG = ThermalHelper.class.getSimpleName();

    private StatsdHelper mStatsdHelper;

    /** Set up the statsd config to track thermal events. */
    @Override
    public boolean startCollecting() {
        Log.i(LOG_TAG, "Registering thermal config to statsd.");
        List<Integer> atomIdList = new ArrayList<>();
        atomIdList.add(Atom.THERMAL_THROTTLING_SEVERITY_STATE_CHANGED_FIELD_NUMBER);
        // TODO(b/137793331): Add an initial value because this only detects changes.
        return getStatsdHelper().addEventConfig(atomIdList);
    }

    /** Collect the thermal events that occurred during the test. */
    @Override
    public Map<String, StringBuilder> getMetrics() {
        Map<String, StringBuilder> results = new HashMap<>();

        List<EventMetricData> eventMetricData = getStatsdHelper().getEventMetrics();
        Log.i(LOG_TAG, String.format("%d thermal data points found.", eventMetricData.size()));
        // Collect all thermal throttling severity state change events.
        for (EventMetricData dataItem : eventMetricData) {
            if (dataItem.getAtom().hasThermalThrottlingSeverityStateChanged()) {
                // TODO(b/137878503): Add elapsed_timestamp_nanos for timpestamp data.
                // Get thermal throttling severity state change data point.
                ThermalThrottlingSeverityStateChanged stateChange =
                        dataItem.getAtom().getThermalThrottlingSeverityStateChanged();
                String sensorType = getShorthandSensorType(stateChange.getSensorType());
                String sensorName = stateChange.getSensorName();
                int severity = stateChange.getSeverity().getNumber();
                // Store the severity state change by sensor type and name.
                String metricKey = MetricUtility.constructKey("thermal", sensorType, sensorName);
                MetricUtility.addMetric(metricKey, severity, results);
            }
        }

        return results;
    }

    /** Remove the statsd config used to track thermal events. */
    @Override
    public boolean stopCollecting() {
        Log.i(LOG_TAG, "Unregistering thermal config from statsd.");
        return getStatsdHelper().removeStatsConfig();
    }

    /** A shorthand name for temperature sensor types used in metric keys. */
    @VisibleForTesting
    static String getShorthandSensorType(TemperatureTypeEnum type) {
        switch (type) {
            case TEMPERATURE_TYPE_CPU:
                return "cpu";

            case TEMPERATURE_TYPE_GPU:
                return "gpu";

            case TEMPERATURE_TYPE_BATTERY:
                return "battery";

            case TEMPERATURE_TYPE_SKIN:
                return "skin";

            case TEMPERATURE_TYPE_USB_PORT:
                return "usb_port";

            case TEMPERATURE_TYPE_POWER_AMPLIFIER:
                return "power_amplifier";

            default:
                return "unknown";
        }
    }

    private StatsdHelper getStatsdHelper() {
        if (mStatsdHelper == null) {
            mStatsdHelper = new StatsdHelper();
        }
        return mStatsdHelper;
    }

    @VisibleForTesting
    void setStatsdHelper(StatsdHelper helper) {
        mStatsdHelper = helper;
    }
}
