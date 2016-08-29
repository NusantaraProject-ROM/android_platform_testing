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

package com.android.androidbvt;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.platform.test.annotations.HermeticTest;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

@HermeticTest
public class SysUIMultiWindowTests extends TestCase {
    private UiAutomation mUiAutomation = null;
    private UiDevice mDevice;
    private Context mContext = null;
    private AndroidBvtHelper mABvtHelper = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = InstrumentationRegistry.getTargetContext();
        mUiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext, mUiAutomation);
        mDevice.setOrientationNatural();
        mDevice.pressMenu();
    }

    @Override
    public void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        mDevice.pressHome();
        mDevice.waitForIdle();
        super.tearDown();
    }

    /**
     * Following test ensures any app can be docked from full-screen to split-screen, another can be
     * launched to multiwindow mode and finally, initial app can be brought back to full-screen
     */
    @LargeTest
    public void testLaunchInMultiwindow() throws InterruptedException, RemoteException {
        mABvtHelper.launchPackage(mABvtHelper.CALCULATOR_PACKAGE);
        int taskId = mABvtHelper.getTaskIdForActivity(mABvtHelper.CALCULATOR_PACKAGE,
                mABvtHelper.CALCULATOR_ACTIVITY);
        try {
            // Convert calculator to multiwindow mode
            mABvtHelper.changeWindowMode(taskId, mABvtHelper.SPLITSCREEN);
            // Launch settings and ensure it is active window
            mABvtHelper.launchPackage(mABvtHelper.SETTINGS_PACKAGE);
            List<AccessibilityWindowInfo> windows = mUiAutomation.getWindows();
            AccessibilityWindowInfo window = windows.get(windows.size() - 1);
            assertTrue("Settings isn't active window",
                    window.getRoot().getPackageName().equals(mABvtHelper.SETTINGS_PACKAGE));

            // Calculate midpoint for Calculator window, click, ensure Calculator is in other half
            // of window
            mDevice.click(mDevice.getDisplayHeight() / 4, mDevice.getDisplayWidth() / 2);
            Thread.sleep(mABvtHelper.SHORT_TIMEOUT * 2);
            windows = mUiAutomation.getWindows();
            window = windows.get(windows.size() - 2);
            assertTrue("Calcualtor isn't active window",
                    window.getRoot().getPackageName().equals(mABvtHelper.CALCULATOR_PACKAGE));

            // Make Calculator FullWindow again and ensure Settings package isn't found on window
            mABvtHelper.changeWindowMode(taskId, mABvtHelper.FULLSCREEN);
            windows = mUiAutomation.getWindows();
            for (int i = 0; i < windows.size() && windows.get(i).getRoot() != null; ++i) {
                assertFalse("Settings have been found",
                        windows.get(i).getRoot().getPackageName()
                                .equals(mABvtHelper.SETTINGS_PACKAGE));
            }
        } finally {
            mABvtHelper.changeWindowMode(taskId, mABvtHelper.FULLSCREEN);
            mDevice.pressHome();
        }
    }

    /**
     * Tests apps do not loose focus and are still visible when apps are launched in MW and
     * landscape mode,
     */
    @LargeTest
    public void testMultiwindowInLandscapeMode() throws InterruptedException, RemoteException {
        // Launch calculator in full screen
        mABvtHelper.launchPackage(mABvtHelper.CALCULATOR_PACKAGE);
        int taskId = mABvtHelper.getTaskIdForActivity(mABvtHelper.CALCULATOR_PACKAGE,
                mABvtHelper.CALCULATOR_ACTIVITY);
        try {
            // Convert calculator to multiwindow mode
            mABvtHelper.changeWindowMode(taskId, mABvtHelper.SPLITSCREEN);
            // Launch Settings
            mABvtHelper.launchPackage(mABvtHelper.SETTINGS_PACKAGE);
            mDevice.setOrientationLeft();
            // Ensure calculator on left
            mDevice.click(mDevice.getDisplayHeight() / 4, mDevice.getDisplayWidth() / 2);
            Thread.sleep(mABvtHelper.SHORT_TIMEOUT * 2);
            List<AccessibilityWindowInfo> windows = mUiAutomation.getWindows();
            AccessibilityWindowInfo window = windows.get(windows.size() - 2);
            assertTrue("Calcualtor isn't left active window",
                    window.getRoot().getPackageName().equals(mABvtHelper.CALCULATOR_PACKAGE));

            // Ensure Settings on right
            mDevice.click((3 * mDevice.getDisplayHeight()) / 4, mDevice.getDisplayWidth() / 2);
            Thread.sleep(mABvtHelper.SHORT_TIMEOUT * 2);
            windows = mUiAutomation.getWindows();
            window = windows.get(windows.size() - 1);
            assertTrue("Settings isn't right active window",
                    window.getRoot().getPackageName().equals(mABvtHelper.SETTINGS_PACKAGE));
        } finally {
            mABvtHelper.changeWindowMode(taskId, mABvtHelper.FULLSCREEN);
            mDevice.pressHome();
        }
    }

    /**
     * Ensure recents show up in MW mode
     */
    @LargeTest
    public void testRecentsInMultiWindowMode() throws InterruptedException, RemoteException {
        mABvtHelper.clearRecents();
        // Launch few packages to populate recents
        mABvtHelper.launchPackage(mABvtHelper.SETTINGS_PACKAGE);
        mABvtHelper.launchPackage(mABvtHelper.DESKCLOCK_PACKAGE);
        mABvtHelper.launchPackage(mABvtHelper.CALCULATOR_PACKAGE);
        int taskId = mABvtHelper.getTaskIdForActivity(mABvtHelper.CALCULATOR_PACKAGE,
                mABvtHelper.CALCULATOR_ACTIVITY);
        try {
            // Convert calculator to multiwindow mode
            mABvtHelper.changeWindowMode(taskId, mABvtHelper.SPLITSCREEN);
            assertTrue("Recents view not loaded after sending foreground calc app to split screen",
                    mDevice.wait(
                            Until.hasObject(By.res(mABvtHelper.SYSTEMUI_PACKAGE, "recents_view")),
                            mABvtHelper.LONG_TIMEOUT));
            // Verify recents has Settings and clock
            List<String> expectedAppsInRecents = new ArrayList<String>();
            expectedAppsInRecents.add("Clock");
            expectedAppsInRecents.add("Settings");
            List<String> actualAppsInRecents = new ArrayList<String>();
            List<UiObject2> recentsObjects = mDevice.wait(
                    Until.findObjects(By.res(mABvtHelper.SYSTEMUI_PACKAGE, "title")),
                    mABvtHelper.LONG_TIMEOUT);
            for (UiObject2 recent : recentsObjects) {
                actualAppsInRecents.add(
                        recent.getText());
            }
            assertTrue("Recents shouldn't have more than 2 apps", actualAppsInRecents.size() == 2);
            actualAppsInRecents.removeAll(expectedAppsInRecents);
            assertTrue("Actual recents apps doesn't match with expected",
                    actualAppsInRecents.size() == 0);
            // Change window mode to full screen
            mABvtHelper.changeWindowMode(taskId, mABvtHelper.FULLSCREEN);
            mDevice.waitForIdle();
        } finally {
            // Ensure nothing in recents
            mABvtHelper.clearRecents();
            mDevice.pressHome();
        }
    }
}
