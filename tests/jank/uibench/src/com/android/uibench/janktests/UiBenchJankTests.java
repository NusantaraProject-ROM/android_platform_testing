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

import junit.framework.Assert;

/**
 * Jank benchmark tests for UiBench app
 */

public class UiBenchJankTests extends JankTestBase {
    private static final int LONG_TIMEOUT = 5000;
    private static final int TIMEOUT = 250;
    private static final int INNER_LOOP = 3;
    private static final int EXPECTED_FRAMES = 100;
    private static final String PACKAGE_NAME = "com.android.test.uibench";
    private static final String RES_PACKAGE_NAME = "android";
    private UiDevice mDevice;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.setOrientationNatural();
    }

    @Override
    protected void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        super.tearDown();
    }

    // Launch UiBench app
    public void launchUiBench() {
        Intent intent = getInstrumentation().getContext().getPackageManager()
                .getLaunchIntentForPackage(PACKAGE_NAME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getInstrumentation().getContext().startActivity(intent);
        mDevice.waitForIdle();
    }

    // Open General Components
    public void openGeneralComponents(String componentName) {
        launchUiBench();
        UiObject2 general = mDevice.wait(Until.findObject(
               By.res(RES_PACKAGE_NAME, "text1").text("General")), TIMEOUT);
        Assert.assertNotNull("General isn't found in UiBench", general);
        general.click();
        SystemClock.sleep(TIMEOUT);
        UiObject2 component = mDevice.wait(Until.findObject(
                By.res(RES_PACKAGE_NAME, "text1").text(componentName)), TIMEOUT);
        Assert.assertNotNull(componentName + ": isn't found in General", component);
        component.click();
        SystemClock.sleep(TIMEOUT);
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
                By.clazz(ListView.class)), TIMEOUT); // this is the only listview
        Assert.assertNotNull("Dialog List View isn't found", dialogListContents);

        for (int i = 0; i < INNER_LOOP; i++) {
            dialogListContents.fling(Direction.DOWN);
            SystemClock.sleep(TIMEOUT);
            dialogListContents.fling(Direction.UP);
            SystemClock.sleep(TIMEOUT);
         }
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
                By.res("android", "content")), TIMEOUT);
        Assert.assertNotNull("Trivial ListView isn't found in General", trivialListViewContents);

        for (int i = 0; i < INNER_LOOP; i++) {
            trivialListViewContents.fling(Direction.DOWN);
            SystemClock.sleep(TIMEOUT);
            trivialListViewContents.fling(Direction.UP);
            SystemClock.sleep(TIMEOUT);
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
                By.res("android", "content")), TIMEOUT);
        Assert.assertNotNull("Trivial Recycler ListView isn't found in General",
             trivialRecyclerViewContents);

        for (int i = 0; i < INNER_LOOP; i++) {
            trivialRecyclerViewContents.fling(Direction.DOWN);
            SystemClock.sleep(TIMEOUT);
            trivialRecyclerViewContents.fling(Direction.UP);
            SystemClock.sleep(TIMEOUT);
         }
     }

    // Ensuring that we head back to the first screen before launching the app again
    public void goBackHome(Bundle metrics) throws UiObjectNotFoundException {
        String launcherPackage = mDevice.getLauncherPackageName();
        UiObject2 homeScreen = mDevice.findObject(By.res(launcherPackage,"workspace"));
        while (homeScreen == null) {
            mDevice.pressBack();
            homeScreen = mDevice.findObject(By.res(launcherPackage,"workspace"));
        }
        super.afterTest(metrics);
    }
}
