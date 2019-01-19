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
import android.device.collectors.util.SendToInstrumentation;
import android.os.Bundle;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Android Unit tests for {@link ScreenshotOnFailureCollector}.
 *
 * <p>To run: atest
 * CollectorDeviceLibTest:android.device.collectors.ScreenshotOnFailureCollectorTest
 */
@RunWith(AndroidJUnit4.class)
public class ScreenshotOnFailureCollectorTest {

    private static final int NUM_TEST_CASE = 5;

    private File mLogDir;
    private File mLogFile;
    private Description mRunDesc;
    private Description mTestDesc;
    private ScreenshotOnFailureCollector mListener;

    @Mock
    private Instrumentation mInstrumentation;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mLogDir = new File("tmp/");
        mLogFile = new File("unique_log_file.log");
        mRunDesc = Description.createSuiteDescription("run");
        mTestDesc = Description.createTestDescription("run", "test");
    }

    @After
    public void tearDown() {
        if (mLogFile != null) {
            mLogFile.delete();
        }
        if (mLogDir != null) {
            mLogDir.delete();
        }
    }

    private ScreenshotOnFailureCollector initListener(Bundle b) {
        ScreenshotOnFailureCollector listener = spy(new ScreenshotOnFailureCollector(b));
        listener.setInstrumentation(mInstrumentation);
        doNothing().when(listener).screenshotToStream(any());
        doReturn(mLogDir).when(listener).createAndEmptyDirectory(anyString());
        doReturn(mLogFile).when(listener).takeScreenshot(anyString());
        return listener;
    }

    @Test
    public void testSaveAsFile() throws Exception {
        Bundle b = new Bundle();
        mListener = initListener(b);

        // Test run start behavior
        mListener.testRunStarted(mRunDesc);
        verify(mListener).createAndEmptyDirectory(ScreenshotOnFailureCollector.DEFAULT_DIR);

        for (int i = 1; i <= NUM_TEST_CASE; i++) {
            mListener.testStarted(mTestDesc);

            mListener.testFailure(new Failure(mTestDesc, new RuntimeException("I failed")));

            // Test test end behavior
            mListener.testFinished(mTestDesc);
            verify(mListener, times(i)).takeScreenshot(any());
        }

        // Test run end behavior
        mListener.testRunFinished(new Result());

        Bundle resultBundle = new Bundle();
        mListener.instrumentationRunFinished(System.out, resultBundle, new Result());

        ArgumentCaptor<Bundle> capture = ArgumentCaptor.forClass(Bundle.class);
        Mockito.verify(mInstrumentation, times(NUM_TEST_CASE))
                .sendStatus(Mockito.eq(
                        SendToInstrumentation.INST_STATUS_IN_PROGRESS), capture.capture());
        List<Bundle> capturedBundle = capture.getAllValues();
        assertEquals(NUM_TEST_CASE, capturedBundle.size());

        int fileCount = 0;
        for(Bundle bundle:capturedBundle){
            for(String key : bundle.keySet()) {
                if (key.contains(mLogFile.getName())) fileCount++;
            }
        }
        assertEquals(NUM_TEST_CASE, fileCount);
    }
}
