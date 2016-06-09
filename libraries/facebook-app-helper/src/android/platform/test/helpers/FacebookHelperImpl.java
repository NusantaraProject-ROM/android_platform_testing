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

import junit.framework.Assert;

public class FacebookHelperImpl extends AbstractFacebookHelper {
    private static final String TAG = "android.platform.test.helpers.FacebookHelperImpl";

    private static final String UI_HOME_PAGE_CONTAINER_ID = "tab_content_viewpager";
    private static final String UI_LOADING_VIEW_ID = "loading_view";
    private static final String UI_LOGIN_BUTTON_ID = "login_login";
    private static final String UI_LOGIN_PASSWORD_ID = "login_password";
    private static final String UI_LOGIN_ROOT_ID = "login_root";
    private static final String UI_LOGIN_USERNAME_ID = "login_username";
    private static final String UI_NEWS_FEED_TAB_ID = "news_feed_tab";
    private static final String UI_NEWS_FEED_TAB_SELECTED_DESC = "Selected";
    private static final String UI_PACKAGE_NAME = "com.facebook.katana";
    private static final String UI_POST_BUTTON_ID = "primary_named_button";
    private static final String UI_STATUS_TEXT_ID = "status_text";
    private static final String UI_STATUS_UPDATE_BUTTON_ID = "feed_composer_status_button";

    private static final long UI_LOGIN_WAIT = 30000;
    private static final long UI_NAVIGATION_WAIT = 10000;

    public FacebookHelperImpl(Instrumentation instr) {
        super(instr);
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        super.open();
        mDevice.wait(Until.findObject(
                By.res(UI_PACKAGE_NAME, UI_HOME_PAGE_CONTAINER_ID)), UI_NAVIGATION_WAIT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return UI_PACKAGE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "Facebook";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {

    }

    private UiObject2 getHomePageContainer() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_HOME_PAGE_CONTAINER_ID));
    }

    private boolean isOnHomePage() {
        return (getHomePageContainer() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scrollHomePage(Direction dir) {
        UiObject2 scrollContainer = getHomePageContainer();
        Assert.assertNotNull("No valid scrolling mechanism found.", scrollContainer);

        scrollContainer.scroll(dir, 5.f);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToHomePage() {
        // Try to go to the home page by repeatedly pressing the back button
        for (int retriesRemaining = 5; retriesRemaining > 0 && !isOnHomePage();
                --retriesRemaining) {
            mDevice.pressBack();
            mDevice.waitForIdle();
        }
    }

    private UiObject2 getNewsFeedTab() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_NEWS_FEED_TAB_ID));
    }

    private boolean isOnNewsFeed() {
        UiObject2 newsFeedTab = getNewsFeedTab();
        if (newsFeedTab == null) {
            return false;
        }

        return newsFeedTab.getContentDescription().contains(UI_NEWS_FEED_TAB_SELECTED_DESC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToNewsFeed() {
        Assert.assertTrue("Not on home page", isOnHomePage());

        UiObject2 newsFeedTab = getNewsFeedTab();
        Assert.assertNotNull("Could not find news feed tab", newsFeedTab);

        newsFeedTab.click();
        mDevice.wait(Until.findObject(By.res(UI_PACKAGE_NAME, UI_NEWS_FEED_TAB_ID).descContains(
                UI_NEWS_FEED_TAB_SELECTED_DESC)), UI_NAVIGATION_WAIT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToStatusUpdate() {
        Assert.assertTrue("Not on News Feed", isOnNewsFeed());

        UiObject2 statusUpdateButton = null;
        for (int retriesRemaining = 50; retriesRemaining > 0 && statusUpdateButton == null;
                --retriesRemaining) {
            scrollHomePage(Direction.UP);
            statusUpdateButton = mDevice.findObject(
                    By.res(UI_PACKAGE_NAME, UI_STATUS_UPDATE_BUTTON_ID));
        }
        Assert.assertNotNull("Could not find status update button", statusUpdateButton);

        statusUpdateButton.click();
        mDevice.wait(Until.findObject(
                By.res(UI_PACKAGE_NAME, UI_STATUS_TEXT_ID)), UI_NAVIGATION_WAIT);
    }

    private UiObject2 getStatusTextField() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_STATUS_TEXT_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatusText(String statusText) {
        UiObject2 statusTextField = getStatusTextField();
        Assert.assertNotNull(statusTextField);

        statusTextField.setText(statusText);
    }

    private UiObject2 getPostButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_POST_BUTTON_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStatusUpdate() {
        UiObject2 postButton = getPostButton();
        Assert.assertNotNull(postButton);

        postButton.click();
        mDevice.wait(Until.findObject(
                By.res(UI_PACKAGE_NAME, UI_HOME_PAGE_CONTAINER_ID)), UI_NAVIGATION_WAIT);
    }

    private boolean isOnLoginPage() {
        return (mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_LOGIN_ROOT_ID)) != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void login(String username, String password) {
        if (!isOnLoginPage()) {
            return;
        }

        UiObject2 usernameTextField = mDevice.findObject(
                By.res(UI_PACKAGE_NAME, UI_LOGIN_USERNAME_ID));
        UiObject2 passwordTextField = mDevice.findObject(
                By.res(UI_PACKAGE_NAME, UI_LOGIN_PASSWORD_ID));
        UiObject2 loginButton = mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_LOGIN_BUTTON_ID));
        Assert.assertNotNull("Could not find username text field", usernameTextField);
        Assert.assertNotNull("Could not find password text field", passwordTextField);
        Assert.assertNotNull("Could not find login button", loginButton);

        usernameTextField.setText(username);
        passwordTextField.setText(password);
        loginButton.click();
        mDevice.wait(Until.findObject(
                By.res(UI_PACKAGE_NAME, UI_HOME_PAGE_CONTAINER_ID)), UI_NAVIGATION_WAIT);
        // Wait for user content to load after logging in
        mDevice.wait(Until.gone(By.res(UI_PACKAGE_NAME, UI_LOADING_VIEW_ID)), UI_LOGIN_WAIT);
    }
}
