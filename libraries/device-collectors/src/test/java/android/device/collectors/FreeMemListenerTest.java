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
import android.os.Bundle;

import androidx.test.runner.AndroidJUnit4;

import com.android.helpers.FreeMemHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Android Unit tests for {@link FreeMemListener}.
 *
 * To run:
 * atest CollectorDeviceLibTest:android.device.collectors.FreeMemListenerTest
 */
@RunWith(AndroidJUnit4.class)
public class FreeMemListenerTest {

    @Mock
    private Instrumentation mInstrumentation;
    @Mock
    private FreeMemHelper mFreeMemHelper;

    private FreeMemListener mListener;
    private Description mRunDesc;
    private Description mTest1Desc;
    private DataRecord mDataRecord;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mRunDesc = Description.createSuiteDescription("run");
        mTest1Desc = Description.createTestDescription("run", "test1");
    }

    private FreeMemListener initListener() {
        FreeMemListener listener = new FreeMemListener(new Bundle(), mFreeMemHelper);
        listener.setInstrumentation(mInstrumentation);
        mDataRecord = listener.createDataRecord();
        return listener;
    }

    @Test
    public void testFreeMemHelperCalls() throws Exception {
        mListener = initListener();
        mListener.testRunStarted(mRunDesc);

        // Test test start behavior
        mListener.testStarted(mTest1Desc);
        verify(mFreeMemHelper, times(1)).startCollecting();
        mListener.onTestEnd(mDataRecord, mTest1Desc);
        verify(mFreeMemHelper, times(1)).stopCollecting();
    }
}
