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

package com.android.jankmicrobenchmark.janktests;

import android.os.RemoteException;
import android.support.test.jank.GfxMonitor;
import android.support.test.jank.JankTest;
import android.support.test.jank.JankTestBase;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;

import junit.framework.Assert;

/**
 * Jank micro benchmark tests
 * App : ApiDemos
 */

public class ApiDemoJankTests extends JankTestBase {
    private static final int LONG_TIMEOUT = 5000;
    private static final int SHORT_TIMEOUT = 500;
    private static final int INNER_LOOP = 5;
    private static final int EXPECTED_FRAMES = 100;
    private static final String PACKAGE_NAME = "com.example.android.apis";
    private static final String RES_PACKAGE_NAME = "android";
    private static final String APP_NAME = "API Demos";
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
        mLauncherStrategy = LauncherStrategyFactory
                .getInstance(mDevice).getLauncherStrategy();
    }

    @Override
    protected void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        super.tearDown();
    }

    public void launchApiDemosAndSelectAnimation(String optionName)
            throws UiObjectNotFoundException {
        mLauncherStrategy.launch(APP_NAME, PACKAGE_NAME);
        UiObject2 animation = mDevice.wait(Until.findObject(
                By.res(RES_PACKAGE_NAME).text("Animation")), LONG_TIMEOUT);
        Assert.assertNotNull("Animation is null", animation);
        animation.click();
        UiObject2 option = mDevice.wait(Until.findObject(
                By.res(RES_PACKAGE_NAME).text(optionName)), LONG_TIMEOUT);
        Assert.assertNotNull("Option is null", option);
        option.click();
    }

    private void selectLoadingOption() {
        // To do
    }
    @JankTest(beforeTest="selectLoadingOption", expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testLoadingJank() {
        // To do
    }
}
