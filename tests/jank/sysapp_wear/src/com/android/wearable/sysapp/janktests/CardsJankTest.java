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

package com.android.wearable.sysapp.janktests;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.jank.GfxMonitor;
import android.support.test.jank.JankTest;
import android.support.test.jank.JankTestBase;
import android.support.test.uiautomator.UiDevice;

/**
 * Janks tests for scrolling & swiping off notification cards on wear
 */
public class CardsJankTest extends JankTestBase {

    private UiDevice mDevice;
    private SysAppTestHelper mHelper;

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mHelper = SysAppTestHelper.getInstance(mDevice, this.getInstrumentation().getContext());
        mDevice.wakeUp();
        SystemClock.sleep(SysAppTestHelper.SHORT_TIMEOUT);
    }

    // Prepare device to start scrolling by tapping on the screen
    // As this is done using demo cards a tap on screen will stop animation and show
    // home screen
    public void openScrollCard() throws Exception {
        mHelper.goBackHome();
        mHelper.hasDemoCards();
        SystemClock.sleep(SysAppTestHelper.SHORT_TIMEOUT);
    }

    // Measure card scroll jank

    @JankTest(beforeTest = "openScrollCard", afterTest = "goBackHome",
            expectedFrames = SysAppTestHelper.MIN_FRAMES)
    @GfxMonitor(processName = "com.google.android.wearable.app")
    public void testScrollCard() {
        mHelper.swipeUp();
    }

    // Preparing the cards to full view before dismissing them

    public void openSwipeCard() throws Exception {
        mHelper.hasDemoCards();
        mHelper.swipeUp();
        mHelper.swipeUp();
        SystemClock.sleep(SysAppTestHelper.SHORT_TIMEOUT);
    }

    // Measure jank when dismissing a card

    @JankTest(beforeTest = "openSwipeCard", afterTest = "goBackHome",
            expectedFrames = SysAppTestHelper.MIN_FRAMES)
    @GfxMonitor(processName = "com.google.android.wearable.app")
    public void testSwipeCard() {
        mHelper.swipeRight();
    }

    // Ensuring that we head back to the first screen before launching the app again
    public void goBackHome(Bundle metrics) throws RemoteException {
        mHelper.goBackHome();
        super.afterTest(metrics);
        SystemClock.sleep(SysAppTestHelper.LONG_TIMEOUT);
    }

    /*
     * (non-Javadoc)
     * @see android.test.InstrumentationTestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
