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

import android.app.Instrumentation;
import android.device.collectors.util.SendToInstrumentation;
import android.os.Bundle;
import android.os.Environment;

import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Run with {@link LogcatOnFailureCollector}.
 */
@RunWith(AndroidJUnit4.class)
public final class LogcatOnFailureCollectorTest {

    // A {@code Description} to pass when faking a test run start call.
    private static final Description RUN_DESCRIPTION = Description.createSuiteDescription("run");
    private static final Description TEST_DESCRIPTION =
            Description.createTestDescription("run", "test");

    private File mLogDir;
    private LogcatOnFailureCollector mCollector;
    private Instrumentation mMockInstrumentation;

    @Before
    public void setUp() throws Exception {
        mCollector = new LogcatOnFailureCollector();
        mMockInstrumentation = Mockito.mock(Instrumentation.class);
        mLogDir = new File(Environment.getExternalStorageDirectory(), "test_logcat");
        mLogDir.mkdirs();
    }

    @After
    public void tearDown() {
        mCollector.recursiveDelete(mLogDir);
    }

    private LogcatOnFailureCollector initListener() {
        LogcatOnFailureCollector listener = Mockito.spy(mCollector);
        listener.setInstrumentation(mMockInstrumentation);
        Mockito.doReturn(mLogDir).when(listener).createAndEmptyDirectory(Mockito.anyString());
        Mockito.doReturn(new ByteArrayInputStream("".getBytes()))
                .when(listener).getLogcat(Mockito.anyString());
        return listener;
    }

    @Test
    public void testLogcatOnFailure_nofailure() throws Exception {
        LogcatOnFailureCollector listener = initListener();
        // Test run start behavior
        listener.testRunStarted(RUN_DESCRIPTION);

        // Test test start behavior
        listener.testStarted(TEST_DESCRIPTION);
        listener.testFinished(TEST_DESCRIPTION);
        listener.testRunFinished(new Result());
        // AJUR runner is then gonna call instrumentationRunFinished
        Bundle resultBundle = new Bundle();
        listener.instrumentationRunFinished(System.out, resultBundle, new Result());

        Mockito.verify(mMockInstrumentation, Mockito.never())
                .sendStatus(Mockito.eq(
                        SendToInstrumentation.INST_STATUS_IN_PROGRESS), Mockito.any());
    }

    @Test
    public void testLogcatOnFailure() throws Exception {
        LogcatOnFailureCollector listener = initListener();
        // Test run start behavior
        listener.testRunStarted(RUN_DESCRIPTION);

        // Test test start behavior
        listener.testStarted(TEST_DESCRIPTION);
        Failure f = new Failure(TEST_DESCRIPTION, new RuntimeException("I failed."));
        listener.testFailure(f);
        listener.testFinished(TEST_DESCRIPTION);
        listener.testRunFinished(new Result());
        // AJUR runner is then gonna call instrumentationRunFinished
        Bundle resultBundle = new Bundle();
        listener.instrumentationRunFinished(System.out, resultBundle, new Result());
        assertEquals(0, resultBundle.size());

        ArgumentCaptor<Bundle> capture = ArgumentCaptor.forClass(Bundle.class);
        Mockito.verify(mMockInstrumentation)
                .sendStatus(Mockito.eq(
                        SendToInstrumentation.INST_STATUS_IN_PROGRESS), capture.capture());
        List<Bundle> capturedBundle = capture.getAllValues();
        assertEquals(1, capturedBundle.size());
        Bundle check = capturedBundle.get(0);
        // Ensure we received the file
        assertEquals(1, check.size());
        // The only key is ours
        for (String key: check.keySet()) {
            assertTrue(key.contains("run.test.txt"));
        }
    }
}
