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

package com.android.support.test.helpers;

import android.app.Instrumentation;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;

import java.util.regex.Pattern;

import junit.framework.Assert;

public class MapsHelperImpl extends AbstractMapsHelper {
    private static final String UI_PACKAGE = "com.google.android.apps.gmm";

    public MapsHelperImpl(Instrumentation instr) {
        super(instr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return "com.google.android.apps.maps";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "Maps";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
        String text = "ACCEPT & CONTINUE";
        Pattern pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE);

        // Accept terms
        UiObject2 terms = mDevice.wait(Until.findObject(By.text(pattern)), 5000);
        if (terms != null) {
            terms.click();
            mDevice.waitForIdle();
        }

        // Enable location services
        UiObject2 location = mDevice.wait(Until.findObject(By.text("Yes, I'm in")), 5000);
        if (location != null) {
            location.click();
            mDevice.waitForIdle();
        }

        // Dismiss cling
        UiObject2 cling = mDevice.wait(
                Until.findObject(By.res(UI_PACKAGE, "tapherehint_textbox")), 5000);
        if (cling != null) {
            cling.click();
            mDevice.waitForIdle();
        }

        // Reset map view
        UiObject2 resetView = mDevice.findObject(By.res(UI_PACKAGE, "mylocation_button"));
        if (resetView != null) {
            resetView.click();
            mDevice.waitForIdle();
        }

        // Dismiss side menu dialog
        BySelector gotIt = By.text(Pattern.compile("GOT IT", Pattern.CASE_INSENSITIVE));
        UiObject2 sideMenuTut = mDevice.findObject(gotIt);
        if (sideMenuTut != null) {
            sideMenuTut.click();
        }

        doSearch("Golden Gate Bridge");
        UiObject2 pullUpTut = mDevice.findObject(gotIt);
        if (pullUpTut != null) {
            pullUpTut.click();
        }

        mDevice.pressBack();

        // Dismiss pull up info dialog
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSearch(String query) {
        // Navigate if necessary
        goToQueryScreen();
        // Enter search query
        UiObject2 search = getSearchBar();
        Assert.assertNotNull("No search bar found.", search);
        search.click();
        search.setText(query);
        // Do search
        mDevice.pressEnter();
        // Wait for directions option
        mDevice.wait(Until.findObject(By.descContains("Directions")), 7500);
    }

    private void goToQueryScreen() {
        for (int backup = 2; backup > 0; backup--) {
            if (hasSearchBar()) {
                return;
            } else {
                mDevice.pressBack();
            }
        }
    }

    private UiObject2 getSearchBar() {
        UiObject2 search = mDevice.findObject(By.descContains("Search"));
        if (search == null) {
            search = mDevice.findObject(By.res(UI_PACKAGE, "search_omnibox_text_box"));
        }
        return search;
    }

    private boolean hasSearchBar() {
        return getSearchBar() != null;
    }
}
