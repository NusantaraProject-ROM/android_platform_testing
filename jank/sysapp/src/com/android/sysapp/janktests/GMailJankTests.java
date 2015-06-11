/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sysapp.janktests;

import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.jank.GfxMonitor;
import android.support.test.jank.JankTest;
import android.support.test.jank.JankTestBase;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import junit.framework.Assert;

/**
 * Jank test for scrolling gmail inbox mails
 */

public class GMailJankTests extends JankTestBase {
    private static final int SHORT_TIMEOUT = 1000;
    private static final int LONG_TIMEOUT = 30000;
    private static final int INNER_LOOP = 5;
    private static final int EXPECTED_FRAMES = 100;
    private static final String PACKAGE_NAME = "com.google.android.gm";
    private static final String APP_NAME = "Gmail";
    private UiDevice mDevice;
    private ILauncherStrategy mLauncherStrategy = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        try {
            mDevice.setOrientationNatural();
        } catch (RemoteException e) {
            throw new RuntimeException("failed to freeze device orientaion", e);
        }
        mLauncherStrategy = LauncherStrategyFactory.getInstance(mDevice).getLauncherStrategy();
    }

    @Override
    protected void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        super.tearDown();
    }

    public void launchGMail () throws UiObjectNotFoundException {
        mLauncherStrategy.launch(APP_NAME, PACKAGE_NAME);
        dismissClings();
        // Need any check for account-name??
        waitForEmailSync();
    }

    // Measures jank while scrolling gmail inbox
    @JankTest(beforeTest="launchGMail", expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testGMailInboxFling() {
        UiObject2 list = mDevice.wait(
                Until.findObject(By.res(PACKAGE_NAME, "conversation_list_view")), 5000);
        Assert.assertNotNull("Failed to locate 'conversation_list_view", list);
        for (int i = 0; i < INNER_LOOP; i++) {
          list.scroll(Direction.DOWN, 1.0f);
          SystemClock.sleep(SHORT_TIMEOUT);
          list.scroll(Direction.UP, 1.0f);
        }
    }

    private void dismissClings() {
        UiObject2 welcomeScreenGotIt = mDevice.wait(
            Until.findObject(By.res(PACKAGE_NAME, "welcome_tour_got_it")), 2000);
        if (welcomeScreenGotIt != null) {
            welcomeScreenGotIt.clickAndWait(Until.newWindow(), SHORT_TIMEOUT);
        }
        UiObject2 welcomeScreenSkip = mDevice.wait(
            Until.findObject(By.res(PACKAGE_NAME, "welcome_tour_skip")), 2000);
        if (welcomeScreenSkip != null) {
          welcomeScreenSkip.clickAndWait(Until.newWindow(), SHORT_TIMEOUT);
        }
        UiObject2 tutorialDone = mDevice.wait(
                Until.findObject(By.res(PACKAGE_NAME, "action_done")), 2 * SHORT_TIMEOUT);
        if (tutorialDone != null) {
            tutorialDone.clickAndWait(Until.newWindow(), SHORT_TIMEOUT);
        }
        mDevice.wait(Until.findObject(By.text("CONFIDENTIAL")), 2 * SHORT_TIMEOUT);
        UiObject2 splash = mDevice.findObject(By.text("Ok, got it"));
        if (splash != null) {
            splash.clickAndWait(Until.newWindow(), SHORT_TIMEOUT);
        }
    }

    public void waitForEmailSync() {
        // Wait up to 2 seconds for a "waiting" message to appear
        mDevice.wait(Until.hasObject(By.text("Waiting for sync")), 2 * SHORT_TIMEOUT);
        // Wait until any "waiting" messages are gone
        Assert.assertTrue("'Waiting for sync' timed out",
                mDevice.wait(Until.gone(By.text("Waiting for sync")), LONG_TIMEOUT));
        Assert.assertTrue("'Loading' timed out",
                mDevice.wait(Until.gone(By.text("Loading")), LONG_TIMEOUT));
    }
}
