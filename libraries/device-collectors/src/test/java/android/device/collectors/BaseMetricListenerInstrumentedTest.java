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
package android.device.collectors;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * Android Unit Tests for {@link BaseMetricListener}.
 */
@RunWith(AndroidJUnit4.class)
public class BaseMetricListenerInstrumentedTest {

    private static final String RUN_START_KEY = "run_start_key";
    private static final String RUN_END_KEY = "run_end_key";
    private static final String RUN_START_VALUE = "run_start_value";
    private static final String RUN_END_VALUE = "run_end_value";
    private static final String TEST_START_KEY = "test_start_key";
    private static final String TEST_END_KEY = "test_end_key";
    private static final String TEST_START_VALUE = "test_start_value";
    private static final String TEST_END_VALUE = "test_end_value";
    private BaseMetricListener mListener;
    private Instrumentation mMockInstrumentation;

    @Before
    public void setUp() {
        mMockInstrumentation = Mockito.mock(Instrumentation.class);
        mListener = new BaseMetricListener() {
            @Override
            public void onTestStart(DataRecord testData, Description description) {
                // In this test check that a new DataRecord is passed to testStart each time.
                assertFalse(testData.hasMetrics());
                testData.addStringMetric(TEST_START_KEY, TEST_START_VALUE);
            }

            @Override
            public void onTestEnd(DataRecord testData, Description description) {
                testData.addStringMetric(TEST_END_KEY, TEST_END_VALUE);
            }

            @Override
            public void onTestRunStart(DataRecord runData, Description description) {
                assertFalse(runData.hasMetrics());
                runData.addStringMetric(RUN_START_KEY, RUN_START_VALUE);
            }

            @Override
            public void onTestRunEnd(DataRecord runData, Result result) {
                runData.addStringMetric(RUN_END_KEY, RUN_END_VALUE);
            }
        };
        mListener.setInstrumentation(mMockInstrumentation);
    }

    /**
     * When metrics are logged during a test, expect them to be added to the bundle.
     */
    @Test
    public void testReportMetrics() throws Exception {
        Description runDescription = Description.createSuiteDescription("run");
        mListener.testRunStarted(runDescription);
        Description testDescription = Description.createTestDescription("class", "method");
        mListener.testStarted(testDescription);
        mListener.testFinished(testDescription);
        mListener.testRunFinished(new Result());
        // AJUR runner is then gonna call instrumentationRunFinished
        Bundle resultBundle = new Bundle();
        mListener.instrumentationRunFinished(System.out, resultBundle, new Result());

        // Check that the in progress status contains the metrics.
        ArgumentCaptor<Bundle> capture = ArgumentCaptor.forClass(Bundle.class);
        Mockito.verify(mMockInstrumentation)
                .sendStatus(Mockito.eq(
                        BaseMetricListener.INST_STATUS_IN_PROGRESS), capture.capture());
        List<Bundle> capturedBundle = capture.getAllValues();
        assertEquals(1, capturedBundle.size());
        Bundle check = capturedBundle.get(0);
        assertEquals(TEST_START_VALUE, check.getString(TEST_START_KEY));
        assertEquals(TEST_END_VALUE, check.getString(TEST_END_KEY));
        assertEquals(2, check.size());

        // Check that final bundle contains run results
        assertEquals(RUN_START_VALUE, resultBundle.getString(RUN_START_KEY));
        assertEquals(RUN_END_VALUE, resultBundle.getString(RUN_END_KEY));
        assertEquals(2, resultBundle.size());
    }
}
