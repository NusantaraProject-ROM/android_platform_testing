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

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.test.internal.runner.listener.InstrumentationRunListener;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.PrintStream;

/**
 * Base implementation of a device metric listener that will capture and output metrics for each
 * test run or test cases. Collectors will have access to {@link DataRecord} objects where they
 * can put results and the base class ensure these results will be send to the instrumentation.
 */
public class BaseMetricListener extends InstrumentationRunListener {

    /**
     * Metrics will be reported under the "status in progress" for test cases to be associated with
     * the running use cases.
     */
    public static final int INST_STATUS_IN_PROGRESS = 2;

    private DataRecord mRunData;
    private DataRecord mTestData;

    @Override
    public final void testRunStarted(Description description) throws Exception {
        mRunData = createDataRecord();
        onTestRunStart(mRunData, description);
        super.testRunStarted(description);
    }

    @Override
    public final void testRunFinished(Result result) throws Exception {
        onTestRunEnd(mRunData, result);
        super.testRunFinished(result);
    }

    @Override
    public final void testStarted(Description description) throws Exception {
        mTestData = createDataRecord();
        onTestStart(mTestData, description);
        super.testStarted(description);
    }

    @Override
    public final void testFailure(Failure failure) throws Exception {
        Description description = failure.getDescription();
        onTestFail(mTestData, description, failure);
        super.testFailure(failure);
    }

    @Override
    public final void testFinished(Description description) throws Exception {
        onTestEnd(mTestData, description);
        if (mTestData.hasMetrics()) {
            // Only send the status progress if there are metrics
            sendStatus(INST_STATUS_IN_PROGRESS, mTestData.createBundleFromMetrics());
        }
        super.testFinished(description);
    }

    @Override
    public void instrumentationRunFinished(
            PrintStream streamResult, Bundle resultBundle, Result junitResults) {
        // Test Run data goes into the INSTRUMENTATION_RESULT
        resultBundle.putAll(mRunData.createBundleFromMetrics());
    }

    /**
     * Create a {@link DataRecord}. Exposed for testing.
     */
    @VisibleForTesting
    DataRecord createDataRecord() {
        return new DataRecord();
    }

    // ---------- Interfaces that can be implemented to take action on each test state.

    /**
     * Called when {@link #testRunStarted(Description)} is called.
     *
     * @param runData structure where metrics can be put.
     * @param description the {@link Description} for the run about to start.
     */
    public void onTestRunStart(DataRecord runData, Description description) {
        // Does nothing
    }

    /**
     * Called when {@link #testRunFinished(Result result)} is called.
     *
     * @param runData structure where metrics can be put.
     * @param result the {@link Result} for the run coming from the runner.
     */
    public void onTestRunEnd(DataRecord runData, Result result) {
        // Does nothing
    }

    /**
     * Called when {@link #testStarted(Description)} is called.
     *
     * @param testData structure where metrics can be put.
     * @param description the {@link Description} for the test case about to start.
     */
    public void onTestStart(DataRecord testData, Description description) {
        // Does nothing
    }

    /**
     * Called when {@link #testFailure(Failure)} is called.
     *
     * @param testData structure where metrics can be put.
     * @param description the {@link Description} for the test case that just failed.
     * @param failure the {@link Failure} describing the failure.
     */
    public void onTestFail(DataRecord testData, Description description, Failure failure) {
        // Does nothing
    }

    /**
     * Called when {@link #testFinished(Description)} is called.
     *
     * @param testData structure where metrics can be put.
     * @param description the {@link Description} of the test coming from the runner.
     */
    public void onTestEnd(DataRecord testData, Description description) {
        // Does nothing
    }

    /**
     * Returns the name of the current class to be used as a logging tag.
     */
    String getTag() {
        return this.getClass().getName();
    }
}
