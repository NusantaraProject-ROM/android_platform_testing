/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.apptransition.tests;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests to test various latencies in the system.
 */
public class LatencyTests {

    private static final int DEFAULT_ITERATION_COUNT = 10;
    private static final String KEY_ITERATION_COUNT = "iteration_count";

    private UiDevice mDevice;
    private int mIterationCount;

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(getInstrumentation());
        Bundle args = InstrumentationRegistry.getArguments();
        mIterationCount = Integer.parseInt(args.getString(KEY_ITERATION_COUNT,
                Integer.toString(DEFAULT_ITERATION_COUNT)));
    }

    /**
     * Test to track how long it takes to expand the notification shade when swiping.
     * <p>
     * Every iteration will output a log in the form of "LatencyTracker/action=0 delay=x".
     */
    @Test
    public void testExpandNotificationsLatency() throws Exception {
        for (int i = 0; i < mIterationCount; i++) {
            swipeDown();
            mDevice.waitForIdle();
            swipeUp();
            mDevice.waitForIdle();

            // Wait for clocks to settle down
            SystemClock.sleep(2000);
        }
    }

    private void swipeDown() {
        mDevice.swipe(mDevice.getDisplayWidth() / 2,
                0, mDevice.getDisplayWidth() / 2,
                mDevice.getDisplayHeight() / 2,
                15);
    }

    private void swipeUp() {
        mDevice.swipe(mDevice.getDisplayWidth() / 2,
                mDevice.getDisplayHeight() / 2,
                mDevice.getDisplayWidth() / 2,
                0,
                15);
    }
}
