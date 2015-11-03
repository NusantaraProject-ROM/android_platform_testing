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
 * Jank benchmark General tests for UiBench app
 */

public class UiBenchJankTests extends JankTestBase {

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

    // Open General Components
    public void openGeneralComponents(String componentName) {
        mHelper.launchUiBench();
        UiObject2 general = mDevice.wait(Until.findObject(
               By.res(mHelper.RES_PACKAGE_NAME, "text1").text("General")), mHelper.TIMEOUT);
        Assert.assertNotNull("General isn't found in UiBench", general);
        general.click();
        SystemClock.sleep(mHelper.TIMEOUT);
        UiObject2 component = mDevice.wait(Until.findObject(
                By.res(mHelper.RES_PACKAGE_NAME, "text1").text(componentName)), mHelper.TIMEOUT);
        Assert.assertNotNull(componentName + ": isn't found in General", component);
        component.clickAndWait(Until.newWindow(), 500);
        SystemClock.sleep(mHelper.TIMEOUT);
    }

    // Open dialog list from General
    public void openDialogList() {
        openGeneralComponents("Dialog List");
    }

    // Test dialoglist fling
    @JankTest(beforeTest="openDialogList", afterTest="goBackHome", expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testDialogListFling() {
        UiObject2 dialogListContents = mDevice.wait(Until.findObject(
                By.clazz(ListView.class)), mHelper.TIMEOUT); // this is the only listview
        Assert.assertNotNull("Dialog List View isn't found", dialogListContents);

        for (int i = 0; i < mHelper.INNER_LOOP; i++) {
            dialogListContents.fling(Direction.DOWN);
            SystemClock.sleep(mHelper.SHORT_TIMEOUT);
            dialogListContents.fling(Direction.UP);
            SystemClock.sleep(mHelper.SHORT_TIMEOUT);
         }
    }

    // Open Fullscreen Overdraw from General
    public void openFullscreenOverdraw() {
        openGeneralComponents("Fullscreen Overdraw");
    }

    // Measure fullscreen overdraw jank
    @JankTest(beforeTest="openFullscreenOverdraw", afterTest="goBackHome",
            expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testFullscreenOverdraw() {
        UiObject2 fullscreenOverdrawScreen = mDevice.wait(Until.findObject(
                By.res("android", "content")), mHelper.TIMEOUT);
        Assert.assertNotNull("Fullscreen Overdraw isn't found", fullscreenOverdrawScreen);
        SystemClock.sleep(mHelper.LONG_TIMEOUT);
    }

    // Open GL TextureView from General
    public void openGLTextureView() {
        openGeneralComponents("GL TextureView");
    }

    // Measure GL TextureView jank metrics
    @JankTest(beforeTest="openGLTextureView", afterTest="goBackHome",
            expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testGLTextureView() {
        SystemClock.sleep(mHelper.LONG_TIMEOUT);
    }

    // Open Invalidate from General
    public void openInvalidate() {
        openGeneralComponents("Invalidate");
    }

    // Measure Invalidate jank metrics
    @JankTest(beforeTest="openInvalidate", afterTest="goBackHome", expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testInvalidate() {
        UiObject2 invalidateScreen = mDevice.wait(Until.findObject(
                By.res("android", "content")), mHelper.TIMEOUT);
        Assert.assertNotNull("Invalidate screen isn't found", invalidateScreen);
        SystemClock.sleep(mHelper.LONG_TIMEOUT);
    }

    // Open Trivial Animation from General
    public void openTrivialAnimation() {
        openGeneralComponents("Trivial Animation");
    }

    // Measure TrivialAnimation jank metrics
    @JankTest(beforeTest="openTrivialAnimation", afterTest="goBackHome",
            expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testTrivialAnimation() {
        UiObject2 trivialAnimationScreen = mDevice.wait(Until.findObject(
                By.res("android", "content")), mHelper.TIMEOUT);
        Assert.assertNotNull("Trivial Animation isn't found", trivialAnimationScreen);
        SystemClock.sleep(mHelper.LONG_TIMEOUT);
    }

    // Open Trivial listview from General
    public void openTrivialListView() {
        openGeneralComponents("Trivial ListView");
    }

    // Test trivialListView fling
    @JankTest(beforeTest="openTrivialListView", afterTest="goBackHome",
           expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testTrivialListViewFling() {
        UiObject2 trivialListViewContents = mDevice.wait(Until.findObject(
                By.res("android", "content")), mHelper.TIMEOUT);
        Assert.assertNotNull("Trivial ListView isn't found in General", trivialListViewContents);

        for (int i = 0; i < mHelper.INNER_LOOP; i++) {
            trivialListViewContents.fling(Direction.DOWN);
            SystemClock.sleep(mHelper.SHORT_TIMEOUT);
            trivialListViewContents.fling(Direction.UP);
            SystemClock.sleep(mHelper.SHORT_TIMEOUT);
         }
    }

    //Open Trivial Recycler List View from General
    public void openTrivialRecyclerListView() {
        openGeneralComponents("Trivial Recycler ListView");
    }

    // Test trivialRecyclerListView fling
    @JankTest(beforeTest="openTrivialRecyclerListView", afterTest="goBackHome",
        expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testTrivialRecyclerListViewFling() {
        UiObject2 trivialRecyclerViewContents = mDevice.wait(Until.findObject(
                By.res("android", "content")), mHelper.TIMEOUT);
        Assert.assertNotNull("Trivial Recycler ListView isn't found in General",
             trivialRecyclerViewContents);

        for (int i = 0; i < mHelper.INNER_LOOP; i++) {
            trivialRecyclerViewContents.fling(Direction.DOWN);
            SystemClock.sleep(mHelper.SHORT_TIMEOUT);
            trivialRecyclerViewContents.fling(Direction.UP);
            SystemClock.sleep(mHelper.SHORT_TIMEOUT);
         }
    }

    // Open Inflation Listview contents
    public void openInflatingListView() {
        mHelper.launchUiBench();
        UiObject2 inflation = mDevice.wait(Until.findObject(
               By.res(mHelper.RES_PACKAGE_NAME, "text1").text("Inflation")), mHelper.TIMEOUT);
        Assert.assertNotNull("Inflation isn't found in UiBench", inflation);
        inflation.click();
        SystemClock.sleep(mHelper.TIMEOUT);
        UiObject2 inflatingListView = mDevice.wait(Until.findObject(
                By.res(mHelper.RES_PACKAGE_NAME, "text1").text("Inflating ListView")), mHelper.TIMEOUT);
        Assert.assertNotNull("Inflating ListView Contents isn't found in Inflation", inflatingListView);
        inflatingListView.click();
        SystemClock.sleep(mHelper.TIMEOUT);
    }

    // Test Inflating List View fling
    @JankTest(beforeTest="openInflatingListView", afterTest="goBackHome",
        expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testInflatingListViewFling() {
        UiObject2 inflatingListViewContents = mDevice.wait(Until.findObject(
                By.res("android", "content")), mHelper.TIMEOUT);
        Assert.assertNotNull("Inflating ListView isn't found in Inflation",
             inflatingListViewContents);

        for (int i = 0; i < mHelper.INNER_LOOP; i++) {
            inflatingListViewContents.fling(Direction.DOWN);
            SystemClock.sleep(mHelper.SHORT_TIMEOUT);
            inflatingListViewContents.fling(Direction.UP);
            SystemClock.sleep(mHelper.SHORT_TIMEOUT);
         }
    }

    // Ensuring that we head back to the first screen before launching the app again
    public void goBackHome(Bundle metrics) throws UiObjectNotFoundException {
        mHelper.goBackHome();
        super.afterTest(metrics);
    }
}
