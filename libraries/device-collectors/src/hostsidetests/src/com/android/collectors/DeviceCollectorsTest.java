/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestRunResult;
import com.android.tradefed.result.CollectingTestListener;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner;
import com.android.tradefed.testtype.junit4.BaseHostJUnit4Test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

/**
 * Host side tests for the device collectors, this ensure that we are able to use the collectors
 * in a similar way as the infra.
 */
@RunWith(DeviceJUnit4ClassRunner.class)
public class DeviceCollectorsTest extends BaseHostJUnit4Test {
    private static final String TEST_APK = "CollectorDeviceLibTest.apk";
    private static final String PACKAGE_NAME = "android.device.collectors";
    private static final String AJUR_RUNNER = "android.support.test.runner.AndroidJUnitRunner";

    private static final String STUB_BASE_COLLECTOR =
            "android.device.collectors.StubTestMetricListener";
    private static final String SCHEDULED_COLLECTOR =
            "android.device.collectors.StubScheduledRunMetricListener";

    @Before
    public void setUp() throws Exception {
        installPackage(TEST_APK);
        assertTrue(isPackageInstalled(PACKAGE_NAME));
    }

    /**
     * Test that our base metric listener can output metrics.
     */
    @Test
    public void testBaseListenerRuns() throws Exception {
        RemoteAndroidTestRunner testRunner =
                new RemoteAndroidTestRunner(PACKAGE_NAME, AJUR_RUNNER, getDevice().getIDevice());
        testRunner.addInstrumentationArg("listener", STUB_BASE_COLLECTOR);
        CollectingTestListener listener = new CollectingTestListener();
        assertTrue(getDevice().runInstrumentationTests(testRunner, listener));
        Collection<TestRunResult> results = listener.getRunResults();
        assertEquals(1, results.size());
        TestRunResult result = results.iterator().next();
        // Ensure the listener added a metric at test run start and end.
        assertTrue(result.getRunMetrics().containsKey("run_start"));
        assertTrue(result.getRunMetrics().containsKey("run_end"));
        // TODO: check each test cases once AJUR is fixed.
    }

    /**
     * Test that our base scheduled listener can output metrics periodically.
     */
    @Test
    public void testScheduledListenerRuns() throws Exception {
        RemoteAndroidTestRunner testRunner =
                new RemoteAndroidTestRunner(PACKAGE_NAME, AJUR_RUNNER, getDevice().getIDevice());
        testRunner.addInstrumentationArg("listener", SCHEDULED_COLLECTOR);
        testRunner.addInstrumentationArg("interval", "100");
        CollectingTestListener listener = new CollectingTestListener();
        assertTrue(getDevice().runInstrumentationTests(testRunner, listener));
        Collection<TestRunResult> results = listener.getRunResults();
        assertEquals(1, results.size());
        TestRunResult result = results.iterator().next();
        // There is time during the test to output at least a handful of periodic metrics.
        assertTrue(result.getRunMetrics().containsKey("collect0"));
        assertTrue(result.getRunMetrics().containsKey("collect1"));
        assertTrue(result.getRunMetrics().containsKey("collect2"));
    }

    /**
     * Test that our base scheduled listener can use its default period to run when the interval
     * given is not valid.
     */
    @Test
    public void testScheduledListenerRuns_defaultValue() throws Exception {
        RemoteAndroidTestRunner testRunner =
                new RemoteAndroidTestRunner(PACKAGE_NAME, AJUR_RUNNER, getDevice().getIDevice());
        testRunner.addInstrumentationArg("listener", SCHEDULED_COLLECTOR);
        // Invalid interval will results in the default period to be used.
        testRunner.addInstrumentationArg("interval", "-100");
        CollectingTestListener listener = new CollectingTestListener();
        assertTrue(getDevice().runInstrumentationTests(testRunner, listener));
        Collection<TestRunResult> results = listener.getRunResults();
        assertEquals(1, results.size());
        TestRunResult result = results.iterator().next();
        // The default interval value is one minute so it will only have time to run once.
        assertEquals(1, result.getRunMetrics().size());
        assertTrue(result.getRunMetrics().containsKey("collect0"));
    }
}
