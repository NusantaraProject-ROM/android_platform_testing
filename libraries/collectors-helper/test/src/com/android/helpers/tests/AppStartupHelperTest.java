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
package com.android.helpers.tests;

import android.os.SystemClock;
import android.platform.helpers.HelperAccessor;
import android.platform.helpers.ICalendarHelper;
import androidx.test.runner.AndroidJUnit4;

import com.android.helpers.AppStartupHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Android Unit tests for {@link AppStartupHelper}.
 *
 * To run:
 * Connect to wifi and login to gmail.
 * atest CollectorsHelperTest:com.android.helpers.tests.AppStartupHelperTest
 */
@RunWith(AndroidJUnit4.class)
public class AppStartupHelperTest {

    // Kill the calendar app.
    private static final String KILL_TEST_APP_CMD = "am force-stop com.google.android.calendar";
    // Key used to store the cold launch time of the calendar app.
    private static final String COLD_LAUNCH_TEST_APP_KEY =
            "cold_startup_com.google.android.calendar";

    private AppStartupHelper mAppStartupHelper = new AppStartupHelper();
    private HelperAccessor<ICalendarHelper> mHelper =
            new HelperAccessor<>(ICalendarHelper.class);

    @Before
    public void setUp() {
        mAppStartupHelper = new AppStartupHelper();
        // Make the apps are starting from the clean state.
        HelperTestUtility.clearApp(KILL_TEST_APP_CMD);

    }

    /**
     * Test successfull app launch config.
     */
    @Test
    public void testAppLaunchConfig() throws Exception {
        assertTrue(mAppStartupHelper.startCollecting());
        assertTrue(mAppStartupHelper.stopCollecting());
    }

    /**
     * Test no error is thrown if there is no app launch.
     */
    @Test
    public void testEmptyAppLaunchMetric() throws Exception {
        assertTrue(mAppStartupHelper.startCollecting());
        assertTrue(mAppStartupHelper.getMetrics().isEmpty());
        assertTrue(mAppStartupHelper.stopCollecting());
    }

    /**
     * Test cold launch key.
     */
    @Test
    public void testColdLaunchMetricKey() throws Exception {
        assertTrue(mAppStartupHelper.startCollecting());
        mHelper.get().open();
        Map.Entry<String, StringBuilder> appLaunchEntry = mAppStartupHelper.getMetrics().entrySet()
                .iterator().next();
        assertEquals(COLD_LAUNCH_TEST_APP_KEY, appLaunchEntry.getKey());
        assertTrue(mAppStartupHelper.stopCollecting());
        mHelper.get().exit();
    }

    /**
     * Test cold launch metric.
     */
    @Test
    public void testSingleColdLaunchMetric() throws Exception {
        assertTrue(mAppStartupHelper.startCollecting());
        mHelper.get().open();
        assertEquals(1, mAppStartupHelper.getMetrics().size());
        assertTrue(mAppStartupHelper.stopCollecting());
        mHelper.get().exit();
    }

    /**
     * Test multiple cold launch metric.
     */
    @Test
    public void testMultipleColdLaunchMetric() throws Exception {
        assertTrue(mAppStartupHelper.startCollecting());
        mHelper.get().open();
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        mHelper.get().exit();
        HelperTestUtility.clearApp(KILL_TEST_APP_CMD);
        mHelper.get().open();
        Map.Entry<String, StringBuilder> appLaunchEntry = mAppStartupHelper.getMetrics().entrySet()
                .iterator().next();
        assertEquals(2, appLaunchEntry.getValue().toString().split(",").length);
        assertTrue(mAppStartupHelper.stopCollecting());
        mHelper.get().exit();
    }

}

