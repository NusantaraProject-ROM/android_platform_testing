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
package android.device.collectors;

import android.device.collectors.annotations.OptionClass;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * A {@link BaseMetricListener} that captures logcat after each test case failure.
 *
 * This class needs external storage permission. See {@link BaseMetricListener} how to grant
 * external storage permission, especially at install time.
 *
 */
@OptionClass(alias = "logcat-failure-collector")
public class LogcatOnFailureCollector extends BaseMetricListener {

    public static final String DEFAULT_DIR = "run_listeners/logcats";
    private static final int BUFFER_SIZE = 16 * 1024;

    private File mDestDir;
    private String mStartTime = null;

    public LogcatOnFailureCollector() {
        super();
    }

    /**
     * Constructor to simulate receiving the instrumentation arguments. Should not be used except
     * for testing.
     */
    @VisibleForTesting
    LogcatOnFailureCollector(Bundle args) {
        super(args);
    }

    @Override
    public void onTestRunStart(DataRecord runData, Description description) {
        mDestDir = createAndEmptyDirectory(DEFAULT_DIR);
    }

    @Override
    public void onTestStart(DataRecord testData, Description description) {
        // Capture the start time for logcat purpose
        mStartTime = getCurrentDate();
    }

    @Override
    public void onTestFail(DataRecord testData, Description description, Failure failure) {
        // Capture logcat from start time
        if (mDestDir == null) {
            return;
        }
        String command = String.format("logcat -T \"%s\"", mStartTime);
        try (InputStream is = getLogcat(command);) {
            final String fileName = String.format("%s.%s.txt", description.getClassName(),
                                description.getMethodName());
            // TODO: Refactor in a fileUtil like tradefed one
            File logcat = new File(mDestDir, fileName);
            OutputStream out = new FileOutputStream(logcat);
            byte[] buf = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buf)) >= 0) {
                out.write(buf, 0, length);
            }
            testData.addFileMetric(String.format("%s_%s", getTag(), logcat.getName()), logcat);
        } catch (IOException e) {
            Log.e(getTag(), "Error executing: " + command, e);
        }
    }

    @VisibleForTesting
    protected InputStream getLogcat(String command) {
        return new ParcelFileDescriptor.AutoCloseInputStream(
                getInstrumentation().getUiAutomation().executeShellCommand(command));
    }

    private String getCurrentDate() {
        Date date = new Date();
        String strDateFormat = "MM-DD hh:mm:ss.mmm";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        return dateFormat.format(date);
    }
}
