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

package com.android.wearable.sysapp.janktests;

import android.os.Bundle;
import android.support.test.jank.GfxMonitor;
import android.support.test.jank.JankTest;
import android.support.test.jank.JankTestBase;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import junit.framework.Assert;

import java.util.concurrent.TimeoutException;

/**
 * Jank tests for watchFace picker on clockwork device
 */
public class WatchFacePickerJankTest extends JankTestBase {

    private UiDevice mDevice;
    private SysAppTestHelper mHelper;

    private static final String WEARABLE_APP_PACKAGE = "com.google.android.wearable.app";
    private static final String WATCHFACE_PREVIEW_NAME = "preview_image";

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        mDevice = UiDevice.getInstance(getInstrumentation());
        mHelper = SysAppTestHelper.getInstance(mDevice, this.getInstrumentation());
        mDevice.wakeUp();
        super.setUp();
    }

    /**
     * Test the jank by open watchface picker
     */
    @JankTest(beforeLoop = "startFromHome", afterTest = "goBackHome",
            expectedFrames = SysAppTestHelper.EXPECTED_FRAMES_WATCHFACE_PICKER_TEST)
    @GfxMonitor(processName = WEARABLE_APP_PACKAGE)
    public void testOpenWatchFacePicker() throws TimeoutException {
        mHelper.swipeLeft();
        UiObject2 previewImage = mDevice.wait(
                Until.findObject(By.res(WEARABLE_APP_PACKAGE, WATCHFACE_PREVIEW_NAME)),
                SysAppTestHelper.SHORT_TIMEOUT);
        Assert.assertNotNull(previewImage);
    }

    public void startFromHome() {
        mHelper.goBackHome();
    }

    // Ensuring that we head back to the first screen before launching the app again
    public void goBackHome(Bundle metrics) {
        mHelper.goBackHome();
        super.afterTest(metrics);
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
