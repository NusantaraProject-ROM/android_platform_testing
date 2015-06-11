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
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import junit.framework.Assert;

/**
 * Jank test for Map
 * click on search box to bring up ime window
 * click on back to go back to map
 */

public class MapsJankTests extends JankTestBase {
    private static final int SHORT_TIMEOUT = 1000;
    private static final int LONG_TIMEOUT = 30000;
    private static final int INNER_LOOP = 5;
    private static final int EXPECTED_FRAMES = 100;
    private static final String PACKAGE_NAME = "com.google.android.apps.maps";
    public static final String RES_PACKAGE = "com.google.android.apps.gmm";
    private static final String APP_NAME = "Maps";
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

    public void launchMaps () throws UiObjectNotFoundException {
        mLauncherStrategy.launch(APP_NAME, PACKAGE_NAME);
        mDevice.waitForIdle();
        dismissCling();
        // To infer that test is ready to be executed
        int counter = 5;
        while (getSearchBox() == null && counter > 0){
            SystemClock.sleep(1000);
            --counter;
        }
        Assert.assertNotNull("Failed to find 'Search'", getSearchBox());
    }

    @JankTest(beforeTest="launchMaps", expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testMapsOverflowMenuTap() {
        for (int i = 0; i < INNER_LOOP; i++) {
            try {
                getSearchBox().click();
            } catch (StaleObjectException se) {
                getSearchBox().click();
            }
            SystemClock.sleep(100);
            // to go away input kb
            mDevice.pressBack();
            // to go back to map search
            mDevice.pressBack();
        }
    }

    private UiObject2 getSearchBox(){
        mDevice.wait(Until.findObject(By.desc("Search")), SHORT_TIMEOUT).click();
        UiObject2 search = mDevice.wait(Until.findObject(
            By.res(RES_PACKAGE, "search_omnibox_edit_text")), SHORT_TIMEOUT);
        if (search == null ) {
            search = mDevice.wait(Until.findObject(
                    By.res(RES_PACKAGE, "search_omnibox_text_box")), SHORT_TIMEOUT);
        }
        Assert.assertNotNull("Search box is null", search);
        return search;
    }

    private void dismissCling(){
        // Accept terms
        UiObject2 terms = mDevice.wait(Until.findObject(By.text("Accept & continue")), 5000);
        if (terms != null) {
            terms.click();
        }
        // Enable location services
        UiObject2 location = mDevice.wait(Until.findObject(By.text("Yes, I'm in")), 5000);
        if (location != null) {
            location.click();
        }
        // Dismiss cling
        UiObject2 cling = mDevice.wait(
                Until.findObject(By.res(PACKAGE_NAME, "tapherehint_textbox")), 500);
        if (cling != null) {
            cling.click();
        }
        // Reset map view
        UiObject2 resetView = mDevice.findObject(By.res(PACKAGE_NAME, "mylocation_button"));
        if (resetView != null) {
            resetView.click();
            mDevice.waitForIdle(5000);
        }
        // dismiss yet another tutorial
        UiObject2 tutorial = mDevice.findObject(By.res(PACKAGE_NAME, "tutorial_side_menu_got_it"));
        if (tutorial != null) {
            tutorial.click();
        }
    }
}
