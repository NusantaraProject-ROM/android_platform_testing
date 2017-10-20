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
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.PrintStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * Unit tests for {@link BaseMetricListener}.
 */
@RunWith(JUnit4.class)
public class BaseMetricListenerTest {

    private static final String RUN_START_KEY = "run_start_key";
    private static final String RUN_END_KEY = "run_end_key";
    private static final String RUN_START_VALUE = "run_start_value";
    private static final String RUN_END_VALUE = "run_end_value";
    private static final String TEST_START_KEY = "test_start_key";
    private static final String TEST_END_KEY = "test_end_key";
    private static final String TEST_START_VALUE = "test_start_value";
    private static final String TEST_END_VALUE = "test_end_value";
    private BaseMetricListener mListener;
    private Bundle mMockBundle;
    private Bundle mRunBundle;
    private Instrumentation mMockInstrumentation;

    @Before
    public void setUp() {
        mMockBundle = Mockito.mock(Bundle.class);
        mRunBundle = Mockito.mock(Bundle.class);
        mMockInstrumentation = Mockito.mock(Instrumentation.class);
        mListener = new BaseMetricListener() {
            @Override
            public void onTestStart(DataRecord testData) {
                // In this test check that a new DataRecord is passed to testStart each time.
                assertFalse(testData.hasMetrics());
                testData.addStringMetric(TEST_START_KEY, TEST_START_VALUE);
            }

            @Override
            public void onTestEnd(DataRecord testData, Description description) {
                testData.addStringMetric(TEST_END_KEY, TEST_END_VALUE);
            }

            @Override
            public void onTestRunStart(DataRecord runData) {
                assertFalse(runData.hasMetrics());
                runData.addStringMetric(RUN_START_KEY, RUN_END_KEY);
            }

            @Override
            public void onTestRunEnd(DataRecord runData, Result result) {
                runData.addStringMetric(RUN_START_VALUE, RUN_END_VALUE);
            }

            @Override
            DataRecord createDataRecord() {
                return new DataRecord() {
                    @Override
                    Bundle createBundle() {
                        return mMockBundle;
                    }
                };
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
        mListener.instrumentationRunFinished(System.out, mRunBundle, new Result());

        Mockito.verify(mMockBundle).putString(TEST_START_KEY, TEST_START_VALUE);
        Mockito.verify(mMockBundle).putString(TEST_END_KEY, TEST_END_VALUE);
        Mockito.verify(mMockInstrumentation)
                .sendStatus(Mockito.eq(BaseMetricListener.INST_STATUS_IN_PROGRESS), (Bundle) any());
        // The temporary bundle is passed to the run result bundle
        Mockito.verify(mRunBundle, Mockito.times(1)).putAll(mMockBundle);
    }

    /**
     * Ensure that when multiple tests are running, DataRecord object passed to each test is a new
     * empty one.
     */
    @Test
    public void testMultiTestReporting() throws Exception {
        Description testDescription = Description.createTestDescription("class", "method");
        mListener.testStarted(testDescription);
        mListener.testFinished(testDescription);
        Description testDescription2 = Description.createTestDescription("class2", "method2");
        mListener.testStarted(testDescription2);
        mListener.testFinished(testDescription2);

        Mockito.verify(mMockBundle, Mockito.times(2)).putString(TEST_START_KEY, TEST_START_VALUE);
        Mockito.verify(mMockBundle, Mockito.times(2)).putString(TEST_END_KEY, TEST_END_VALUE);
        Mockito.verify(mMockInstrumentation, Mockito.times(2))
                .sendStatus(Mockito.eq(BaseMetricListener.INST_STATUS_IN_PROGRESS), (Bundle) any());
    }
}