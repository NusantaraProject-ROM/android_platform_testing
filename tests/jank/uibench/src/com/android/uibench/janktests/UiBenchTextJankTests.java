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

package com.android.uibench.janktests;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.jank.GfxMonitor;
import android.support.test.jank.JankTest;
import android.support.test.jank.JankTestBase;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import android.widget.ListView;

import com.android.uibench.janktests.UiBenchJankTestsHelper;
import static com.android.uibench.janktests.UiBenchJankTestsHelper.PACKAGE_NAME;
import static com.android.uibench.janktests.UiBenchJankTestsHelper.EXPECTED_FRAMES;
import junit.framework.Assert;

/**
 * Jank benchmark Text tests for UiBench app
 */

public class UiBenchTextJankTests extends JankTestBase {

    private UiDevice mDevice;
    private UiBenchJankTestsHelper mHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.setOrientationNatural();
        mHelper = UiBenchJankTestsHelper.getInstance(mDevice,
             this.getInstrumentation().getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        super.tearDown();
    }

    // Open Text Components
    public void openTextComponents(String componentName) {
        mHelper.launchUiBench();
        UiObject2 text = mDevice.wait(Until.findObject(
               By.res(mHelper.RES_PACKAGE_NAME, "text1").text("Text")), mHelper.TIMEOUT);
        Assert.assertNotNull("Text isn't found in UiBench", text);
        text.click();
        SystemClock.sleep(mHelper.TIMEOUT);
        UiObject2 component = mDevice.wait(Until.findObject(
                By.res(mHelper.RES_PACKAGE_NAME, "text1").text(componentName)), mHelper.TIMEOUT);
        Assert.assertNotNull(componentName + ": isn't found in UiBench:Text", component);
        component.clickAndWait(Until.newWindow(), 500);
        SystemClock.sleep(mHelper.TIMEOUT);
    }

    // Open EditText Typing
    public void openEditTextTyping() {
        openTextComponents("EditText Typing");
    }

    // Measure jank metrics for EditText Typing
    @JankTest(beforeTest="openEditTextTyping", afterTest="goBackHome",
        expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testEditTextTyping() {
        SystemClock.sleep(mHelper.LONG_TIMEOUT);
    }

    // Open Layout Cache High Hitrate
    public void openLayoutCacheHighHitrate() {
        openTextComponents("Layout Cache High Hitrate");
    }

    // Test Layout Cache High Hitrate fling
    @JankTest(beforeTest="openLayoutCacheHighHitrate", afterTest="goBackHome",
        expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testLayoutCacheHighHitrateFling() {
        UiObject2 layoutCacheHighHitrateContents = mDevice.wait(Until.findObject(
                By.clazz(ListView.class)), mHelper.TIMEOUT);
        Assert.assertNotNull("Shadow Grid list isn't found", layoutCacheHighHitrateContents);

        for (int i = 0; i < mHelper.INNER_LOOP; i++) {
            layoutCacheHighHitrateContents.fling(Direction.DOWN);
            SystemClock.sleep(mHelper.TIMEOUT);
            layoutCacheHighHitrateContents.fling(Direction.UP);
            SystemClock.sleep(mHelper.TIMEOUT);
         }
    }

    // Open Layout Cache Low Hitrate
    public void openLayoutCacheLowHitrate() {
        openTextComponents("Layout Cache Low Hitrate");
    }

    // Test Layout Cache Low Hitrate fling
    @JankTest(beforeTest="openLayoutCacheLowHitrate", afterTest="goBackHome",
        expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testLayoutCacheLowHitrateFling() {
        UiObject2 layoutCacheLowHitrateContents = mDevice.wait(Until.findObject(
                By.clazz(ListView.class)), mHelper.TIMEOUT);
        Assert.assertNotNull("Shadow Grid list isn't found", layoutCacheLowHitrateContents);

        for (int i = 0; i < mHelper.INNER_LOOP; i++) {
            layoutCacheLowHitrateContents.fling(Direction.DOWN);
            SystemClock.sleep(mHelper.TIMEOUT);
            layoutCacheLowHitrateContents.fling(Direction.UP);
            SystemClock.sleep(mHelper.TIMEOUT);
         }
    }

    // Ensuring that we head back to the first screen before launching the app again
    public void goBackHome(Bundle metrics) throws UiObjectNotFoundException {
           mHelper.goBackHome();
           super.afterTest(metrics);
    }
}
