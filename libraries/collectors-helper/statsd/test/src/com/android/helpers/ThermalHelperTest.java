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

import static org.mockito.Mockito.when;

import android.os.TemperatureTypeEnum;
import android.os.ThrottlingSeverityEnum;
import androidx.test.runner.AndroidJUnit4;

import com.android.os.AtomsProto.Atom;
import com.android.os.AtomsProto.ThermalThrottlingSeverityStateChanged;
import com.android.os.StatsLog.EventMetricData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Android Unit tests for {@link com.android.helpers.ThermalHelper}.
 *
 * <p>To run this test: Disable SELinux with "adb shell setenforce 0"; if this command fails with
 * "Permission denied", try running "adb shell su 0 setenforce 0". Then run: "atest
 * CollectorsHelperTest:com.android.helpers.ThermalHelperTest".
 */
@RunWith(AndroidJUnit4.class)
public class ThermalHelperTest {

    private ThermalHelper mThermalHelper;
    private StatsdHelper mStatsdHelper;

    @Before
    public void setUp() {
        mThermalHelper = new ThermalHelper();
        mStatsdHelper = Mockito.spy(new StatsdHelper());
        mThermalHelper.setStatsdHelper(mStatsdHelper);
    }

    /** Test registering and unregistering the thermal config. */
    @Test
    public void testThermalConfigRegistration() throws Exception {
        assertTrue(mThermalHelper.startCollecting());
        assertTrue(mThermalHelper.stopCollecting());
    }

    /** Test that no metrics show up when there are no events. */
    @Test
    public void testNoMetricsWithoutEvents() throws Exception {
        when(mStatsdHelper.getEventMetrics()).thenReturn(new ArrayList<EventMetricData>());
        assertTrue(mThermalHelper.startCollecting());
        assertTrue(mThermalHelper.getMetrics().isEmpty());
        assertTrue(mThermalHelper.stopCollecting());
    }

    /** Test that a single event shows up as a single metric event. */
    @Test
    public void testSingleEvent() throws Exception {
        when(mStatsdHelper.getEventMetrics())
                .thenReturn(
                        getFakeEventMetrics(
                                getThermalThrottlingSeverityStateChangedEvent(
                                        TemperatureTypeEnum.TEMPERATURE_TYPE_SKIN,
                                        "sensor_name",
                                        ThrottlingSeverityEnum.NONE)));

        assertTrue(mThermalHelper.startCollecting());
        Map<String, StringBuilder> metrics = mThermalHelper.getMetrics();
        String key = getMetricKey(TemperatureTypeEnum.TEMPERATURE_TYPE_SKIN, "sensor_name");
        assertTrue(metrics.containsKey(key));
        assertEquals(
                metrics.get(key).toString(),
                String.valueOf(ThrottlingSeverityEnum.NONE.getNumber()));
        assertTrue(mThermalHelper.stopCollecting());
    }

    /** Test that multiple, similar events shows up as a single metric with multiple values. */
    @Test
    public void testMultipleSimilarEvents() throws Exception {
        when(mStatsdHelper.getEventMetrics())
                .thenReturn(
                        getFakeEventMetrics(
                                getThermalThrottlingSeverityStateChangedEvent(
                                        TemperatureTypeEnum.TEMPERATURE_TYPE_SKIN,
                                        "sensor_name",
                                        ThrottlingSeverityEnum.NONE),
                                getThermalThrottlingSeverityStateChangedEvent(
                                        TemperatureTypeEnum.TEMPERATURE_TYPE_SKIN,
                                        "sensor_name",
                                        ThrottlingSeverityEnum.LIGHT),
                                getThermalThrottlingSeverityStateChangedEvent(
                                        TemperatureTypeEnum.TEMPERATURE_TYPE_SKIN,
                                        "sensor_name",
                                        ThrottlingSeverityEnum.MODERATE)));

        assertTrue(mThermalHelper.startCollecting());
        Map<String, StringBuilder> metrics = mThermalHelper.getMetrics();
        String key = getMetricKey(TemperatureTypeEnum.TEMPERATURE_TYPE_SKIN, "sensor_name");
        assertTrue(metrics.containsKey(key));
        assertEquals(
                metrics.get(key).toString(),
                String.join(
                        ",",
                        String.valueOf(ThrottlingSeverityEnum.NONE.getNumber()),
                        String.valueOf(ThrottlingSeverityEnum.LIGHT.getNumber()),
                        String.valueOf(ThrottlingSeverityEnum.MODERATE.getNumber())));
        assertTrue(mThermalHelper.stopCollecting());
    }

    /** Test that multiple, different events shows up as a multiple metrics with a single value. */
    @Test
    public void testMultipleDifferentEvents() throws Exception {
        when(mStatsdHelper.getEventMetrics())
                .thenReturn(
                        getFakeEventMetrics(
                                getThermalThrottlingSeverityStateChangedEvent(
                                        TemperatureTypeEnum.TEMPERATURE_TYPE_SKIN,
                                        "sensor1_name",
                                        ThrottlingSeverityEnum.LIGHT),
                                getThermalThrottlingSeverityStateChangedEvent(
                                        TemperatureTypeEnum.TEMPERATURE_TYPE_CPU,
                                        "sensor2_name",
                                        ThrottlingSeverityEnum.LIGHT),
                                getThermalThrottlingSeverityStateChangedEvent(
                                        TemperatureTypeEnum.TEMPERATURE_TYPE_GPU,
                                        "sensor3_name",
                                        ThrottlingSeverityEnum.NONE)));

        assertTrue(mThermalHelper.startCollecting());
        Map<String, StringBuilder> metrics = mThermalHelper.getMetrics();
        String skinKey = getMetricKey(TemperatureTypeEnum.TEMPERATURE_TYPE_SKIN, "sensor1_name");
        String cpuKey = getMetricKey(TemperatureTypeEnum.TEMPERATURE_TYPE_CPU, "sensor2_name");
        String gpuKey = getMetricKey(TemperatureTypeEnum.TEMPERATURE_TYPE_GPU, "sensor3_name");
        assertTrue(metrics.containsKey(skinKey));
        assertTrue(metrics.containsKey(cpuKey));
        assertTrue(metrics.containsKey(gpuKey));
        assertEquals(
                metrics.get(skinKey).toString(),
                String.valueOf(ThrottlingSeverityEnum.LIGHT.getNumber()));
        assertEquals(
                metrics.get(cpuKey).toString(),
                String.valueOf(ThrottlingSeverityEnum.LIGHT.getNumber()));
        assertEquals(
                metrics.get(gpuKey).toString(),
                String.valueOf(ThrottlingSeverityEnum.NONE.getNumber()));
        assertTrue(mThermalHelper.stopCollecting());
    }

    /** Returns a list of {@link EventMetricData} that statsd returns. */
    private List<EventMetricData> getFakeEventMetrics(
            ThermalThrottlingSeverityStateChanged... throttleSeverityEvents) {
        List<EventMetricData> result = new ArrayList<>();
        for (ThermalThrottlingSeverityStateChanged event : throttleSeverityEvents) {
            result.add(
                    EventMetricData.newBuilder()
                            .setAtom(
                                    Atom.newBuilder()
                                            .setThermalThrottlingSeverityStateChanged(event))
                            .build());
        }
        return result;
    }

    /** Returns a state change protobuf for thermal throttling severity. */
    private ThermalThrottlingSeverityStateChanged getThermalThrottlingSeverityStateChangedEvent(
            TemperatureTypeEnum type, String name, ThrottlingSeverityEnum severity) {
        return ThermalThrottlingSeverityStateChanged.newBuilder()
                .setSensorType(type)
                .setSensorName(name)
                .setSeverity(severity)
                .build();
    }

    /** Get the thermal metric key for a thermal sensor type and name. */
    private String getMetricKey(TemperatureTypeEnum type, String name) {
        return MetricUtility.constructKey(
                "thermal", ThermalHelper.getShorthandSensorType(type), name);
    }
}
