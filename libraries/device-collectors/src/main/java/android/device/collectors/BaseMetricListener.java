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
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.VisibleForTesting;
import android.support.test.internal.runner.listener.InstrumentationRunListener;
import android.util.Log;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Base implementation of a device metric listener that will capture and output metrics for each
 * test run or test cases. Collectors will have access to {@link DataRecord} objects where they
 * can put results and the base class ensure these results will be send to the instrumentation.
 *
 * Any subclass that calls {@link #createAndEmptyDirectory(String)} needs external storage permission.
 * So to use this class at runtime, your test need to
 * <a href="{@docRoot}training/basics/data-storage/files.html#GetWritePermission">have storage
 * permission enabled</a>, and preferably granted at install time (to avoid interrupting the test).
 * For testing at desk, run adb install -r -g testpackage.apk
 * "-g" grants all required permission at install time.
 */
public class BaseMetricListener extends InstrumentationRunListener {

    /**
     * Metrics will be reported under the "status in progress" for test cases to be associated with
     * the running use cases.
     */
    public static final int INST_STATUS_IN_PROGRESS = 2;
    public static final int BUFFER_SIZE = 1024;

    private DataRecord mRunData;
    private DataRecord mTestData;

    @Override
    public final void testRunStarted(Description description) throws Exception {
        try {
            mRunData = createDataRecord();
            onTestRunStart(mRunData, description);
        } catch (RuntimeException e) {
            // Prevent exception from reporting events.
            Log.e(getTag(), "Exception during onTestRunStart.", e);
        }
        super.testRunStarted(description);
    }

    @Override
    public final void testRunFinished(Result result) throws Exception {
        try {
            onTestRunEnd(mRunData, result);
        } catch (RuntimeException e) {
            // Prevent exception from reporting events.
            Log.e(getTag(), "Exception during onTestRunEnd.", e);
        }
        super.testRunFinished(result);
    }

    @Override
    public final void testStarted(Description description) throws Exception {
        try {
            mTestData = createDataRecord();
            onTestStart(mTestData, description);
        } catch (RuntimeException e) {
            // Prevent exception from reporting events.
            Log.e(getTag(), "Exception during onTestStart.", e);
        }
        super.testStarted(description);
    }

    @Override
    public final void testFailure(Failure failure) throws Exception {
        Description description = failure.getDescription();
        try {
            onTestFail(mTestData, description, failure);
        } catch (RuntimeException e) {
            // Prevent exception from reporting events.
            Log.e(getTag(), "Exception during onTestFail.", e);
        }
        super.testFailure(failure);
    }

    @Override
    public final void testFinished(Description description) throws Exception {
        try {
            onTestEnd(mTestData, description);
        } catch (RuntimeException e) {
            // Prevent exception from reporting events.
            Log.e(getTag(), "Exception during onTestEnd.", e);
        }
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
     * Turn executeShellCommand into a blocking operation.
     *
     * @param command shell command to be executed.
     * @return byte array of execution result
     */
    public byte[] executeCommandBlocking(String command) {
        try (
                InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(
                        getInstrumentation().getUiAutomation().executeShellCommand(command));
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            byte[] buf = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buf)) >= 0) {
                out.write(buf, 0, length);
            }
            return out.toByteArray();
        } catch (IOException e) {
            Log.e(getTag(), "Error executing: " + command, e);
            return null;
        }
    }

    /**
     * Create a directory inside external storage, and empty it.
     *
     * @param dir full path to the dir to be created.
     * @return directory file created
     */
    public File createAndEmptyDirectory(String dir) {
        File rootDir = Environment.getExternalStorageDirectory();
        File destDir = new File(rootDir, dir);
        executeCommandBlocking("rm -rf " + destDir.getAbsolutePath());
        if (!destDir.exists() && !destDir.mkdirs()) {
            Log.e(getTag(), "Unable to create dir: " + destDir.getAbsolutePath());
            return null;
        }
        return destDir;
    }

    /**
     * Returns the name of the current class to be used as a logging tag.
     */
    String getTag() {
        return this.getClass().getName();
    }
}
