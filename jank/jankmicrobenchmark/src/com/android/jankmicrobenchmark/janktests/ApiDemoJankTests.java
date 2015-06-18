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

import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.jank.GfxMonitor;
import android.support.test.jank.JankTest;
import android.support.test.jank.JankTestBase;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import android.widget.Button;

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
                By.res(RES_PACKAGE_NAME, "text1").text("Animation")), LONG_TIMEOUT);
        Assert.assertNotNull("Animation is null", animation);
        animation.click();
        UiObject2 option = mDevice.wait(Until.findObject(
                By.res(RES_PACKAGE_NAME, "text1").text(optionName)), LONG_TIMEOUT);
        int maxAttempt = 3;
        while (option == null && maxAttempt > 0) {
            mDevice.wait(Until.findObject(By.res(RES_PACKAGE_NAME, "content")), LONG_TIMEOUT)
                    .scroll(Direction.DOWN, 1.0f);
            option = mDevice.wait(Until.findObject(By.res(RES_PACKAGE_NAME, "text1")
                    .text(optionName)), LONG_TIMEOUT);
            --maxAttempt;
        }
        Assert.assertNotNull("Option is null", option);
        option.click();
    }

    // Since the app doesn't start at the first page when reloaded after the first time,
    // ensuring that we head back to the first screen before going Home so we're always
    // on screen one.
    public void goBackHome(Bundle metrics) throws UiObjectNotFoundException {
        String launcherPackage = mDevice.getLauncherPackageName();
        UiObject2 homeScreen = mDevice.findObject(By.res(launcherPackage,"workspace"));
        while (homeScreen == null) {
            mDevice.pressBack();
            homeScreen = mDevice.findObject(By.res(launcherPackage,"workspace"));
        }
        super.afterTest(metrics);
    }

    // Loads the 'activity transition' animation
    public void selectActivityTransitionAnimation() throws UiObjectNotFoundException {
         launchApiDemosAndSelectAnimation("Activity Transition");
    }

    // Measures jank for activity transition animation
    @JankTest(beforeTest="selectActivityTransitionAnimation", afterTest="goBackHome",
        expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testActivityTransitionAnimation() {
        for (int i = 0; i < INNER_LOOP; i++) {
            UiObject2 redBallTile = mDevice.findObject(By.res(PACKAGE_NAME, "ball"));
            redBallTile.click();
            SystemClock.sleep(LONG_TIMEOUT);
            mDevice.pressBack();
        }
    }

    // Loads the 'view flip' animation
    public void selectViewFlipAnimation() throws UiObjectNotFoundException {
        launchApiDemosAndSelectAnimation("View Flip");
    }

    // Measures jank for view flip animation
    @JankTest(beforeTest="selectViewFlipAnimation", afterTest="goBackHome",
        expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testViewFlipAnimation() {
        for (int i = 0; i < INNER_LOOP; i++) {
            UiObject2 flipButton = mDevice.findObject(By.res(PACKAGE_NAME, "button"));
            flipButton.click();
            SystemClock.sleep(LONG_TIMEOUT);
        }
    }

    // Loads the 'cloning' animation
    public void selectCloningAnimation() throws UiObjectNotFoundException {
        launchApiDemosAndSelectAnimation("Cloning");    
    }

    // Measures jank for cloning animation
    @JankTest(beforeTest="selectCloningAnimation", afterTest="goBackHome",
        expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testCloningAnimation() {
        for (int i = 0; i < INNER_LOOP; i++) {
            UiObject2 runCloningButton = mDevice.findObject(By.res(PACKAGE_NAME, "startButton"));
            runCloningButton.click();
            SystemClock.sleep(LONG_TIMEOUT);
        }
    }

    // Loads the 'loading' animation
    public void selectLoadingOption() throws UiObjectNotFoundException {
        launchApiDemosAndSelectAnimation("Loading");
    }
    // Measures jank for 'loading' animation
    @JankTest(beforeTest="selectLoadingOption", afterTest="goBackHome"
            , expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testLoadingJank() {
        UiObject2 runButton = mDevice.wait(Until.findObject(
            By.res(PACKAGE_NAME, "startButton").text("Run")), LONG_TIMEOUT);
        Assert.assertNotNull("Run button is null", runButton);
        for(int i = 0; i < INNER_LOOP; ++i) {
            runButton.click();
            SystemClock.sleep(SHORT_TIMEOUT * 2);
        }
    }

    // Loads the 'simple transition' animation
    public void selectSimpleTransitionOption() throws UiObjectNotFoundException {
        launchApiDemosAndSelectAnimation("Simple Transitions");
    }
    // Measures jank for 'simple transition' animation
    @JankTest(beforeTest="selectSimpleTransitionOption", afterTest="goBackHome"
            , expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testSimpleTransitionJank() {
        for(int i = 0; i < INNER_LOOP; ++i) {
            UiObject2 scene2 = mDevice.wait(Until.findObject(
                    By.res(PACKAGE_NAME, "scene2")), LONG_TIMEOUT);
            Assert.assertNotNull("Scene2 is null", scene2);
            scene2.click();
            SystemClock.sleep(SHORT_TIMEOUT);
            UiObject2 scene4 = mDevice.wait(Until.findObject(
                    By.res(PACKAGE_NAME, "scene4")), LONG_TIMEOUT);
            Assert.assertNotNull("Scene3 is null", scene4);
            scene4.click();
            SystemClock.sleep(SHORT_TIMEOUT);
        }
    }

    // Loads the 'hide/show' animation
    public void selectHideShowAnimationOption() throws UiObjectNotFoundException {
        launchApiDemosAndSelectAnimation("Hide-Show Animations");
    }
    // Measures jank for 'hide/show' animation
    @JankTest(beforeTest="selectHideShowAnimationOption", afterTest="goBackHome"
            , expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testHideShowAnimationJank() {
        for(int i = 0; i < INNER_LOOP; ++i) {
            UiObject2 showButton = mDevice.wait(Until.findObject(By.res(
                    PACKAGE_NAME, "addNewButton").text("Show Buttons")), LONG_TIMEOUT);
            Assert.assertNotNull("SHow Button is null", showButton);
            showButton.click();
            SystemClock.sleep(SHORT_TIMEOUT);
            UiObject2 button0 = mDevice.wait(Until.findObject(
                    By.clazz(Button.class).text("0")), LONG_TIMEOUT);
            Assert.assertNotNull("Button0 is null", button0);
            button0.click();
            SystemClock.sleep(SHORT_TIMEOUT);
            UiObject2 button1 = mDevice.wait(Until.findObject(
                    By.clazz(Button.class).text("1")), LONG_TIMEOUT);
            Assert.assertNotNull("Button1 is null", button1);
            button1.click();
            SystemClock.sleep(SHORT_TIMEOUT);
            UiObject2 button2 = mDevice.wait(Until.findObject(
                    By.clazz(Button.class).text("2")), LONG_TIMEOUT);
            Assert.assertNotNull("Button2 is null", button2);
            button2.click();
            SystemClock.sleep(SHORT_TIMEOUT);
            UiObject2 button3 = mDevice.wait(Until.findObject(
                    By.clazz(Button.class).text("3")), LONG_TIMEOUT);
            Assert.assertNotNull("Button3 is null", button3);
            button3.click();
            SystemClock.sleep(SHORT_TIMEOUT);
        }
    }
}
