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

package android.platform.test.helpers;

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
    private static final String LOG_TAG = MapsHelperImpl.class.getSimpleName();

    private static final String UI_CLOSE_NAVIGATION_DESC = "Close navigation";
    private static final String UI_DIRECTIONS_BUTTON_ID = "placepage_directions_button";
    private static String UI_PACKAGE;
    private static final String UI_START_NAVIGATION_BUTTON_ID = "start_button";
    private static final String UI_TEXTVIEW_CLASS = "android.widget.TextView";

    private static final long UI_RELATED_WAIT = 5000;
    private static final long WIFI_RELATED_WAIT = 25000;

    private boolean mIsVersion9p30;

    public MapsHelperImpl(Instrumentation instr) {
        super(instr);

        try {
            mIsVersion9p30 = getVersion().startsWith("9.30.");
            if (mIsVersion9p30) {
                UI_PACKAGE = "com.google.android.apps.maps";
            } else {
                UI_PACKAGE = "com.google.android.apps.gmm";
            }
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, String.format("Unable to find package by name, %s", getPackage()));
        }
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
        // ToS welcome dialog
        boolean successTosDismiss = false;
        int tryCounter = 0;

        String text = "ACCEPT & CONTINUE";
        Pattern pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE);

        while (!successTosDismiss) {
            if (++tryCounter > 3) {
                throw new IllegalStateException("Unable to dismiss Maps dialog due to poor WiFi");
            }

            UiObject2 tryAgainWifiButton = mDevice.findObject(By.text("TRY AGAIN"));
            if (tryAgainWifiButton != null) {
                tryAgainWifiButton.click();
                mDevice.wait(Until.gone(By.text("TRY AGAIN")), 2500);
            }

            UiObject2 terms = mDevice.wait(Until.findObject(By.text(pattern)), 10000);
            if (terms != null) {
                terms.click();
                successTosDismiss = mDevice.wait(Until.hasObject(
                        By.res(UI_PACKAGE, "search_omnibox_text_box")), WIFI_RELATED_WAIT);
            } else {
                Log.e(LOG_TAG, "Did not find a ToS dialog.");
            }
        }

        if (mIsVersion9p30) {
            exit();
            open();
        }

        // Location services dialog
        text = "YES, I'M IN";
        pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE);
        UiObject2 location = mDevice.wait(Until.findObject(By.text(pattern)), 5000);
        if (location != null) {
            location.click();
            mDevice.waitForIdle();
        } else {
            Log.e(LOG_TAG, "Did not find a location services dialog.");
        }

        if (!mIsVersion9p30) {
            // Tap here dialog
            UiObject2 cling = mDevice.wait(
                    Until.findObject(By.res(UI_PACKAGE, "tapherehint_textbox")), 5000);
            if (cling != null) {
                cling.click();
                mDevice.waitForIdle();
            } else {
                Log.e(LOG_TAG, "Did not find 'tap here' dialog");
            }

            // Reset map dialog
            UiObject2 resetView = mDevice.wait(
                    Until.findObject(By.res(UI_PACKAGE, "mylocation_button")), 5000);
            if (resetView != null) {
                resetView.click();
                mDevice.waitForIdle();
            } else {
                Log.e(LOG_TAG, "Did not find 'reset map' dialog.");
            }
        }

        // 'Side menu' dialog
        text = "GOT IT";
        pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE);
        BySelector gotIt = By.text(Pattern.compile("GOT IT", Pattern.CASE_INSENSITIVE));
        UiObject2 sideMenuTut = mDevice.wait(Until.findObject(gotIt), 5000);
        if (sideMenuTut != null) {
            sideMenuTut.click();
        } else {
            Log.e(LOG_TAG, "Did not find any 'side menu' dialog.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSearch(String query) {
        // Navigate if necessary
        goToQueryScreen();
        // Select search bar
        UiObject2 searchSelect = getSelectableSearchBar();
        Assert.assertNotNull("No selectable search bar found.", searchSelect);
        searchSelect.click();
        mDevice.wait(Until.findObject(By.res(UI_PACKAGE, "search_omnibox_edit_text")),
                UI_RELATED_WAIT);
        // Edit search query
        UiObject2 searchEdit = getEditableSearchBar();
        Assert.assertNotNull("Not editable search bar found.", searchEdit);
        searchEdit.clear();
        searchEdit.setText(query);
        // Search and wait for the directions option
        UiObject2 firstAddressResult = mDevice.wait(Until.findObject(By.pkg(UI_PACKAGE).clazz(
            UI_TEXTVIEW_CLASS)), WIFI_RELATED_WAIT);
        Assert.assertNotNull(String.format("Did not detect address result after %d seconds",
                (int)Math.floor(WIFI_RELATED_WAIT / 1000)), firstAddressResult);
        firstAddressResult.click();
        Assert.assertTrue("Could not find directions button", mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE, UI_DIRECTIONS_BUTTON_ID)), WIFI_RELATED_WAIT));
    }

    private void goToQueryScreen() {
        for (int backup = 5; backup > 0; backup--) {
            if (hasSearchBar()) {
                return;
            } else {
                mDevice.pressBack();
                mDevice.waitForIdle();
            }
        }
    }

    private UiObject2 getSelectableSearchBar() {
        return mDevice.findObject(By.res(UI_PACKAGE, "search_omnibox_text_box"));
    }

    private UiObject2 getEditableSearchBar() {
        return mDevice.findObject(By.res(UI_PACKAGE, "search_omnibox_edit_text"));
    }

    private UiObject2 getStartNavigationButton() {
        return mDevice.findObject(By.res(UI_PACKAGE, UI_START_NAVIGATION_BUTTON_ID));
    }

    private boolean hasSearchBar() {
        return getSelectableSearchBar() != null || getEditableSearchBar() != null;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void getDirections() {
        UiObject2 directionsButton = mDevice.findObject(By.res(UI_PACKAGE, UI_DIRECTIONS_BUTTON_ID));
        Assert.assertNotNull("Could not find directions button", directionsButton);

        directionsButton.click();
        Assert.assertTrue("Could not find start navigation button", mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE, UI_START_NAVIGATION_BUTTON_ID)), WIFI_RELATED_WAIT));
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void startNavigation() {
        UiObject2 startNavigationButton = getStartNavigationButton();
        Assert.assertNotNull("Could not find start navigation button", startNavigationButton);

        startNavigationButton.click();
        Assert.assertTrue(mDevice.wait(Until.hasObject(
                By.pkg(UI_PACKAGE).desc(UI_CLOSE_NAVIGATION_DESC)), UI_RELATED_WAIT));
    }
}
