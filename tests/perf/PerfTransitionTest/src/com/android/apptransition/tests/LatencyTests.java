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

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Tests to test various latencies in the system.
 */
public class LatencyTests {

    private static final int DEFAULT_ITERATION_COUNT = 10;
    private static final String KEY_ITERATION_COUNT = "iteration_count";
    private static final long CLOCK_SETTLE_DELAY = 2000;
    private static final String FINGERPRINT_WAKE_FAKE_COMMAND = "am broadcast -a "
            + "com.android.systemui.latency.ACTION_FINGERPRINT_WAKE";
    private static final String TURN_ON_SCREEN_COMMAND = "am broadcast -a "
            + "com.android.systemui.latency.ACTION_TURN_ON_SCREEN";
    private static final String AM_START_COMMAND_TEMPLATE = "am start -a %s";

    private UiDevice mDevice;
    private int mIterationCount;

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(getInstrumentation());
        Bundle args = InstrumentationRegistry.getArguments();
        mIterationCount = Integer.parseInt(args.getString(KEY_ITERATION_COUNT,
                Integer.toString(DEFAULT_ITERATION_COUNT)));
        mDevice.pressHome();
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
            SystemClock.sleep(CLOCK_SETTLE_DELAY);
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

    /**
     * Test to track how long it takes until the animation starts in a fingerprint-wake-and-unlock
     * sequence.
     * <p>
     * Every iteration will output a log in the form of "LatencyTracker/action=2 delay=x".
     */
    @Test
    public void testFingerprintWakeAndUnlock() throws Exception {
        for (int i = 0; i < mIterationCount; i++) {
            mDevice.sleep();

            // Wait for clocks to settle down
            SystemClock.sleep(CLOCK_SETTLE_DELAY);

            mDevice.executeShellCommand(FINGERPRINT_WAKE_FAKE_COMMAND);
            mDevice.waitForIdle();
        }
    }

    /**
     * Test how long it takes until the screen is fully turned on.
     * <p>
     * Every iteration will output a log in the form of "LatencyTracker/action=5 delay=x".
     */
    @Test
    public void testScreenTurnOn() throws Exception {
        for (int i = 0; i < mIterationCount; i++) {
            mDevice.sleep();

            // Wait for clocks to settle down
            SystemClock.sleep(CLOCK_SETTLE_DELAY);

            mDevice.executeShellCommand(TURN_ON_SCREEN_COMMAND);
            mDevice.waitForIdle();
        }

        // Put device to home screen.
        mDevice.pressMenu();
        mDevice.waitForIdle();
    }

    /**
     * Test that measure how long the total time takes until recents is visible after pressing it.
     * Note that this is different from {@link AppTransitionTests#testAppToRecents} as we are
     * measuring the full latency here, but in the app transition test we only measure the time
     * spent after startActivity is called. This might be different as SystemUI does a lot of binder
     * calls before calling startActivity.
     * <p>
     * Every iteration will output a log in the form of "LatencyTracker/action=1 delay=x".
     */
    @Test
    public void testAppToRecents() throws Exception {
        populateRecentApps();
        for (int i = 0; i < mIterationCount; i++) {
            mDevice.executeShellCommand(String.format(AM_START_COMMAND_TEMPLATE,
                    Settings.ACTION_SETTINGS));
            mDevice.waitForIdle();

            // Wait for clocks to settle.
            SystemClock.sleep(CLOCK_SETTLE_DELAY);
            pressUiRecentApps();
            mDevice.waitForIdle();

            // Make sure all the animations are really done.
            SystemClock.sleep(200);
        }
    }

    private void pressUiRecentApps() throws Exception {
        mDevice.findObject(By.res("com.android.systemui", "recent_apps")).click();
    }

    // TODO: Share this code with the jank tests.
    private void populateRecentApps() throws IOException {
        PackageManager pm = getInstrumentation().getContext().getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo pkg : packages) {
            if (pkg.packageName.equals(getInstrumentation().getTargetContext().getPackageName())) {
                continue;
            }
            Intent intent = pm.getLaunchIntentForPackage(pkg.packageName);
            if (intent == null) {
                continue;
            }
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getInstrumentation().getTargetContext().startActivity(intent);

            // Don't overload the system
            SystemClock.sleep(500);
        }

        // Give the apps some time to finish starting. Some apps start another activity while
        // starting, and we don't want to happen when we are testing stuff.
        SystemClock.sleep(3000);

        // Close any crash dialogs
        while (mDevice.hasObject(By.textContains("has stopped"))) {
            mDevice.performActionAndWait(() -> mDevice.pressBack(), Until.newWindow(), 2000);
        }
    }
}
