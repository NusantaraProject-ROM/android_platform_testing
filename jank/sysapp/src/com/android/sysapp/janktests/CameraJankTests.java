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
import android.support.test.jank.GfxMonitor;
import android.support.test.jank.JankTest;
import android.support.test.jank.JankTestBase;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import android.widget.Button;

import junit.framework.Assert;

/**
 * Jank test for flipping front and back camera n times.
 */

public class CameraJankTests extends JankTestBase {
    private static final int TIMEOUT = 3000;
    private static final int INNER_LOOP = 5;
    private static final int EXPECTED_FRAMES = 100;
    private static final String PACKAGE_NAME = "com.google.android.GoogleCamera";
    private static final String FRAMEWORK_PACKAGE_NAME = "com.android.camera2";
    private static final String APP_NAME = "Camera";
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

    public void launchCamera () throws UiObjectNotFoundException {
        mLauncherStrategy.launch(APP_NAME, PACKAGE_NAME);
        dismissClings();
        BySelector threeDotsSelector = By.res(
            FRAMEWORK_PACKAGE_NAME, "mode_options_toggle").desc("Options");
        UiObject2 threeDots = mDevice.wait(Until.findObject(threeDotsSelector), TIMEOUT);
        Assert.assertNotNull("Three dot icon is missing", threeDots);
        threeDots.click();
        // wait for next window to show up
        mDevice.wait(Until.gone(threeDotsSelector), TIMEOUT);
    }

    // Measures jank while fling YouTube recommendation
    @JankTest(beforeTest="launchCamera", expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testFrontBackCameraFlip() {
        UiObject2 cameraToggle = null;
        BySelector cameraToggleSelector = By.res(FRAMEWORK_PACKAGE_NAME, "camera_toggle_button");
        for (int i = 0; i < INNER_LOOP; i++) {
          cameraToggle = mDevice.wait(Until.findObject(cameraToggleSelector), 3 * TIMEOUT);
          Assert.assertNotNull("Camera flipper icon is missing", cameraToggle);
          cameraToggle.click();
          mDevice.wait(Until.gone(cameraToggleSelector), TIMEOUT);
        }
    }

    private void dismissClings() {
        // Dismiss tag next screen. It's okay to timeout. These dialog screens might not exist..
        UiObject2 next = mDevice.wait(Until.findObject(
                By.clazz(Button.class).text("NEXT")), 2 * TIMEOUT);
        if (next != null) {
            next.click();
        }
        // Choose sensor size. It's okay to timeout. These dialog screens might not exist..
        UiObject2 sensor = mDevice.wait(Until.findObject(
                By.res(PACKAGE_NAME, "confirm_button").text("OK, GOT IT")), 2 * TIMEOUT);
        if (sensor != null) {
            sensor.click();
        }
        // Dismiss the photo location dialog box if exist.
        UiObject2 thanks = mDevice.wait(Until.findObject(
                By.text("No thanks")), 2 * TIMEOUT);
        if (thanks != null) {
            thanks.click();
        }
        // Dismiss dogfood dialog
        if (mDevice.wait(Until.hasObject(
                By.res(PACKAGE_NAME, "internal_release_dialog_title")), 2 * TIMEOUT)) {
            mDevice.findObject(By.res(FRAMEWORK_PACKAGE_NAME, "ok_button")).click();
        }
    }
}
