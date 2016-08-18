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
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;
import android.widget.EditText;

public class PlayStoreHelperImpl extends AbstractPlayStoreHelper {
    private static final String LOG_TAG = PlayStoreHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.android.vending";

    private static final long LONG_TOS_DIALOG_WAIT = 20000;
    private static final long LOAD_RESULT_TRANSITION = 10000;
    private static final long INSTALL_DELAY = 5000;

    public PlayStoreHelperImpl(Instrumentation instr) {
        super(instr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return "com.android.vending";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "Play Store";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
        if (!isAppInForeground()) {
            throw new IllegalStateException("The Play Store app is not in the foreground.");
        }
        // Dismiss the ToS dialog by pressing accept
        if (mDevice.wait(Until.hasObject(
                By.textContains("Google Play Terms of Service")), LONG_TOS_DIALOG_WAIT)) {
            mDevice.findObject(getPositiveButtonSelector()).click();
            boolean home = mDevice.wait(Until.hasObject(getSearchBoxSelector()), 10000);
            if (!home) {
                throw new UnknownUiException("Failed to reach the home screen.");
            }
        } else {
            throw new UnknownUiException("Unable to find ToS");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSearch(String query) {
        if (!isAppInForeground()) {
            throw new IllegalStateException("The Play Store app is not in the foreground.");
        }
        // Scroll up or press back until searching is available, or fail
        UiObject2 search = null;
        for (int retries = 5; retries > 0; retries--) {
            // Search for the search box
            search = mDevice.findObject(getSearchBoxSelector());
            if (search != null) {
                break;
            }
            // Search for the search button
            search = mDevice.findObject(getSearchButtonSelector());
            if (search != null) {
                break;
            }
            // Scroll up or back out
            if (!scrollPage(Direction.UP, 100.0f)) {
                mDevice.pressBack();
            }
        }
        if (search == null) {
            throw new UnknownUiException("Failed to find a search method.");
        }
        search.click();
        // After pressing, the search element becomes the edit text box
        UiObject2 edit = mDevice.wait(
                Until.findObject(By.clazz(EditText.class)), 5000);
        if (edit == null) {
            throw new UnknownUiException("Failed to find an edit text.");
        }
        edit.setText(query);
        mDevice.pressEnter();
        // Validate the end criteria that the search list is visible
        if (!mDevice.wait(Until.hasObject(getSearchResultSelector()), 5000)) {
            throw new UnknownUiException("Failed to find the search results.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectFirstResult() {
        if (!mDevice.hasObject(getSearchResultSelector())) {
            throw new IllegalStateException("No available search result list.");
        }
        UiObject2 result = mDevice.findObject(By.res(UI_PACKAGE, "play_card"));
        if (result == null) {
            throw new UnknownUiException("Failed to find a search result card.");
        }
        result.click();
        if (!mDevice.wait(Until.hasObject(getAppTitleSelector()), LOAD_RESULT_TRANSITION)) {
            throw new UnknownUiException("Failed to find the app page open.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installApp() {
        // #isAppInstalled also verifies that this is an app page
        if (isAppInstalled()) {
            throw new IllegalStateException("This app is already installed.");
        }

        UiObject2 install = mDevice.findObject(getInstallButtonSelector());
        if (install == null) {
            throw new UnknownUiException("Could not find an install button.");
        }
        install.click();

        // Search for the Android 6.0 permission dialog
        if (mDevice.wait(Until.hasObject(getAndroid6DialogSelector()), 2500)) {
            mDevice.findObject(getPositiveButtonSelector()).click();
            mDevice.waitForIdle();
        } else {
            // If the install button is present, then downloading failed
            if (mDevice.wait(Until.findObject(
                    getInstallButtonSelector()), INSTALL_DELAY) != null) {
                throw new UnknownUiException("Did not detect that the installation started.");
            }
        }
    }

    private boolean scrollPage(Direction dir, float value) {
        UiObject2 scroller = mDevice.findObject(By.res(UI_PACKAGE, "recycler_view"));
        if (scroller == null) {
            scroller = mDevice.findObject(By.res(UI_PACKAGE, "viewpager"));
        }

        if (scroller != null) {
            return scroller.scroll(dir, value);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAppInstalled() {
        if (!isAppPage()) {
            throw new IllegalStateException("Play Store was not on the app's install page.");
        }
        return (mDevice.findObject(getInstallButtonSelector()) == null);
    }

    private boolean isAppPage () {
        // Warning: this can fail if the page is scrolled down
        return mDevice.hasObject(getAppTitleSelector());
    }

    private BySelector getPositiveButtonSelector() {
        return By.res(UI_PACKAGE, "positive_button");
    }

    private BySelector getSearchBoxSelector() {
        return By.res(UI_PACKAGE, "search_box_idle_text");
    }

    private BySelector getSearchButtonSelector() {
        return By.res(UI_PACKAGE, "search_button");
    }

    private BySelector getSearchResultSelector() {
        return By.res(UI_PACKAGE, "search_results_list");
    }

    private BySelector getAppTitleSelector() {
        return By.res(UI_PACKAGE, "title_background");
    }

    private BySelector getInstallButtonSelector() {
        return By.res(UI_PACKAGE, "buy_button");
    }

    private BySelector getAndroid6DialogSelector() {
        return By.res(UI_PACKAGE, "optional_permissions_help_screen");
    }
}

