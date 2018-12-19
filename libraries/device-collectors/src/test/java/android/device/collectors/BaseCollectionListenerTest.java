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
package android.device.collectors;

import android.os.Bundle;
import androidx.test.runner.AndroidJUnit4;

import com.android.helpers.ICollectorHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Android Unit tests for {@link BaseCollectionListenerTest}.
 *
 * To run:
 * atest CollectorDeviceLibTest:android.device.collectors.BaseCollectionListenerTest
 */
@RunWith(AndroidJUnit4.class)
public class BaseCollectionListenerTest {

    // A {@code Description} to pass when faking a test run start call.
    private static final Description FAKE_DESCRIPTION = Description.createSuiteDescription("run");

    private BaseCollectionListener<String> mListener;

    @Mock
    private ICollectorHelper helper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private BaseCollectionListener initListener(Bundle b) {
        BaseCollectionListener listener = new BaseCollectionListener<String>(b, helper);
        doReturn(true).when(helper).startCollecting();
        doReturn(new HashMap<String, String>()).when(helper).getMetrics();
        doReturn(true).when(helper).stopCollecting();
        return listener;
    }

    /**
     * Verify start and stop collection happens only during test run started
     * and test run ended when per_run option is enabled.
     */
    @Test
    public void testPerRunFlow() throws Exception {
        Bundle b = new Bundle();
        b.putString(BaseCollectionListener.COLLECT_PER_RUN, "true");
        mListener = initListener(b);

        mListener.onTestRunStart(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(1)).startCollecting();
        mListener.onTestStart(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(1)).startCollecting();
        mListener.onTestEnd(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(0)).stopCollecting();
        mListener.onTestRunEnd(mListener.createDataRecord(), new Result());
        verify(helper, times(1)).stopCollecting();
    }

    /**
     * Verify start and stop collection happens before and after each test
     * and not during test run started and test run ended when per_run option is
     * disabled.
     */
    @Test
    public void testPerTestFlow() throws Exception {
        Bundle b = new Bundle();
        b.putString(BaseCollectionListener.COLLECT_PER_RUN, "false");
        mListener = initListener(b);

        mListener.onTestRunStart(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(0)).startCollecting();
        mListener.onTestStart(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(1)).startCollecting();
        mListener.onTestEnd(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(1)).stopCollecting();
        mListener.onTestStart(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(2)).startCollecting();
        mListener.onTestEnd(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(2)).stopCollecting();
        mListener.onTestRunEnd(mListener.createDataRecord(), new Result());
        verify(helper, times(2)).stopCollecting();
    }

    /**
     * Verify start and stop collection happens before and after each test
     * and not during test run started and test run ended by default.
     */
    @Test
    public void testDefaultOptionFlow() throws Exception {
        Bundle b = new Bundle();
        mListener = initListener(b);

        mListener.onTestRunStart(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(0)).startCollecting();
        mListener.onTestStart(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(1)).startCollecting();
        mListener.onTestEnd(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(1)).stopCollecting();
        mListener.onTestStart(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(2)).startCollecting();
        mListener.onTestEnd(mListener.createDataRecord(), FAKE_DESCRIPTION);
        verify(helper, times(2)).stopCollecting();
        mListener.onTestRunEnd(mListener.createDataRecord(), new Result());
        verify(helper, times(2)).stopCollecting();
    }
}
