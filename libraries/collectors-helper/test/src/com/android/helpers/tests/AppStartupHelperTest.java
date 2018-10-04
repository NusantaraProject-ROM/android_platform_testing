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
    private static final String KILL_TEST_APP_CMD_TEMPLATE = "am force-stop %s";
    // Package names used for testing
    private static final String CALENDAR_PKG_NAME = "com.google.android.calendar";
    private static final String CALCULATOR_PKG_NAME = "com.google.android.calculator";
    // Key prefixes to store the cold, warm or hot launch time of the calendar app, respectively.
    private static final String COLD_LAUNCH_KEY_TEMPLATE = "cold_startup_%s";
    private static final String WARM_LAUNCH_KEY_TEMPLATE = "warm_startup_%s";
    private static final String HOT_LAUNCH_KEY_TEMPLATE = "hot_startup_%s";

    // A couple of ADB commands to help with the calculator hot launch test.
    private static final String LUANCH_APP_CMD_TEMPLATE =
            "monkey -p %s -c android.intent.category.LAUNCHER 1";
    private static final String KEYEVENT_CMD_TEMPLATE = "input keyevent %s";
    private static final String KEYCODE_HOME = "KEYCODE_HOME";
    private static final String KEYCODE_WAKEUP = "KEYCODE_WAKEUP";
    private static final String KEYCODE_UNLOCK = "KEYCODE_MENU";

    private AppStartupHelper mAppStartupHelper = new AppStartupHelper();
    private HelperAccessor<ICalendarHelper> mHelper =
            new HelperAccessor<>(ICalendarHelper.class);

    @Before
    public void setUp() {
        mAppStartupHelper = new AppStartupHelper();
        // Make sure the apps are starting from the clean state.
        HelperTestUtility.clearApp(String.format(KILL_TEST_APP_CMD_TEMPLATE, CALENDAR_PKG_NAME));
        HelperTestUtility.clearApp(String.format(KILL_TEST_APP_CMD_TEMPLATE, CALCULATOR_PKG_NAME));
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
        assertEquals(String.format(COLD_LAUNCH_KEY_TEMPLATE, CALENDAR_PKG_NAME),
                appLaunchEntry.getKey());
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
        HelperTestUtility.clearApp(String.format(KILL_TEST_APP_CMD_TEMPLATE, CALENDAR_PKG_NAME));
        mHelper.get().open();
        Map.Entry<String, StringBuilder> appLaunchEntry = mAppStartupHelper.getMetrics().entrySet()
                .iterator().next();
        assertEquals(2, appLaunchEntry.getValue().toString().split(",").length);
        assertTrue(mAppStartupHelper.stopCollecting());
        mHelper.get().exit();
    }

    /**
     * Test warm launch metric.
     */
    @Test
    public void testWarmLaunchMetric() throws Exception {

        // Launch the app once and exit it so it resides in memory.
        mHelper.get().open();
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        // Press home and clear the cache explicitly.
        HelperTestUtility.executeShellCommand(String.format(KEYEVENT_CMD_TEMPLATE, KEYCODE_HOME));
        HelperTestUtility.clearCache();
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        // Start the collection here to test warm launch.
        assertTrue(mAppStartupHelper.startCollecting());
        // Launch the app; a warm launch occurs.
        mHelper.get().open();
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        Map<String, StringBuilder> appLaunchMetrics = mAppStartupHelper.getMetrics();
        String calendarWarmLaunchKey = String.format(WARM_LAUNCH_KEY_TEMPLATE, CALENDAR_PKG_NAME);
        assertTrue(appLaunchMetrics.keySet().contains(calendarWarmLaunchKey));
        assertEquals(1, appLaunchMetrics.get(calendarWarmLaunchKey).toString().split(",").length);
        assertTrue(mAppStartupHelper.stopCollecting());
        mHelper.get().exit();
    }

    /**
     * Test hot launch metric on calculator, which is lightweight enough to trigger a hot launch.
     */
    @Test
    public void testHotLaunchMetric() throws Exception {
        String calculatorLaunchCommand =
                String.format(LUANCH_APP_CMD_TEMPLATE, CALCULATOR_PKG_NAME);
        // Launch the app once and go home so the app resides in memory.
        HelperTestUtility.executeShellCommand(String.format(KEYEVENT_CMD_TEMPLATE, KEYCODE_WAKEUP));
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        HelperTestUtility.executeShellCommand(String.format(KEYEVENT_CMD_TEMPLATE, KEYCODE_UNLOCK));
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        HelperTestUtility.executeShellCommand(calculatorLaunchCommand);
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        HelperTestUtility.executeShellCommand(String.format(KEYEVENT_CMD_TEMPLATE, KEYCODE_HOME));
        // Start the collection here to test hot launch.
        assertTrue(mAppStartupHelper.startCollecting());
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        // Launch the app; a hot launch occurs.
        HelperTestUtility.executeShellCommand(calculatorLaunchCommand);
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        Map<String, StringBuilder> appLaunchMetrics = mAppStartupHelper.getMetrics();
        String calculatoHotLaunchKey = String.format(HOT_LAUNCH_KEY_TEMPLATE, CALCULATOR_PKG_NAME);
        assertTrue(appLaunchMetrics.keySet().contains(calculatoHotLaunchKey));
        assertEquals(1, appLaunchMetrics.get(calculatoHotLaunchKey).toString().split(",").length);
        assertTrue(mAppStartupHelper.stopCollecting());
        HelperTestUtility.executeShellCommand(String.format(KEYEVENT_CMD_TEMPLATE, KEYCODE_HOME));
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        HelperTestUtility.clearApp(String.format(KILL_TEST_APP_CMD_TEMPLATE, CALCULATOR_PKG_NAME));
    }
}

