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
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Assert;

public class GmailHelperImpl extends AbstractGmailHelper {
    private static final String LOG_TAG = GmailHelperImpl.class.getSimpleName();

    private static final long DIALOG_TIMEOUT = 2500;
    private static final long POPUP_TIMEOUT = 5000;
    private static final long COMPOSE_TIMEOUT = 5000;
    private static final long SEND_TIMEOUT = 5000;
    private static final long LOADING_TIMEOUT = 20000;

    private static final String UI_PACKAGE_NAME = "com.google.android.gm";
    private static final String UI_PROMO_ACTION_NEG_RES = "promo_action_negative_single_line";
    private static final String UI_CONVERSATIONS_LIST_ID = "conversation_list_view";
    private static final String UI_CONVERSATION_CONTAINER_ID = "conversation_container";
    private static final BySelector PRIMARY_SELECTOR =
            By.res(UI_PACKAGE_NAME, "name").text("Primary");
    private static final BySelector INBOX_SELECTOR = By.res(UI_PACKAGE_NAME, "name").text("Inbox");
    private static final BySelector NAV_DRAWER_SELECTOR = By.res("android", "list").focused(true);

    public GmailHelperImpl(Instrumentation instr) {
        super(instr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return "com.google.android.gm";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "Gmail";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
        // Dismiss initial splash pages
        UiObject2 welcomeScreenGotIt = mDevice.findObject(
                By.res(UI_PACKAGE_NAME, "welcome_tour_got_it"));
        if (welcomeScreenGotIt != null) {
            welcomeScreenGotIt.clickAndWait(Until.newWindow(), DIALOG_TIMEOUT);
            mDevice.waitForIdle();
        }
        UiObject2 tutorialDone = mDevice.findObject(
                By.res(UI_PACKAGE_NAME, "action_done"));
        if (tutorialDone != null) {
            tutorialDone.clickAndWait(Until.newWindow(), DIALOG_TIMEOUT);
            mDevice.wait(Until.findObject(By.text("CONFIDENTIAL")), POPUP_TIMEOUT);
        }
        Pattern gotItWord = Pattern.compile("OK, GOT IT", Pattern.CASE_INSENSITIVE);
        UiObject2 splash = mDevice.findObject(By.text(gotItWord));
        if (splash != null) {
            splash.clickAndWait(Until.newWindow(), DIALOG_TIMEOUT);
        }

        if (mDevice.findObject(By.text("Waiting for sync")) != null) {
            mDevice.wait(Until.hasObject(By.text("Waiting for sync")), 2000);
            Assert.assertTrue("'Waiting for sync' timed out",
                    mDevice.wait(Until.gone(By.text("Waiting for sync")), 30000));
            Assert.assertTrue("'Loading' timed out",
                    mDevice.wait(Until.gone(By.text("Loading")), 30000));
        }

        // Dismiss Navigation-to-Inbox tips
        goToInbox();

        UiObject2 welcomBox = mDevice.findObject(By.res(UI_PACKAGE_NAME, "welcome_box"));
        if (welcomBox != null) {
            welcomBox.swipe(Direction.LEFT, 1.0f);
        }
        UiObject2 dismiss = mDevice.findObject(By.text("Dismiss tip"));
        if (dismiss != null) {
            dismiss.clickAndWait(Until.newWindow(), DIALOG_TIMEOUT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToInbox() {
        // Simply press back if in a conversation
        if (isInEmail()) {
            mDevice.pressBack();
            waitForConversationsList();
        }

        // Check if already in Inbox or Primary
        if (isInPrimaryOrInbox()) {
            return;
        }

        // Search with the navigation drawer
        UiObject2 backBtn = mDevice.findObject(By.desc("Open navigation drawer"));
        if (backBtn != null) {
            backBtn.click();
            // Select for "Primary" and for "Inbox"
            UiObject2 primaryInboxSelector = mDevice.findObject(PRIMARY_SELECTOR);
            if (primaryInboxSelector == null) {
                primaryInboxSelector = mDevice.findObject(INBOX_SELECTOR);
            }

            primaryInboxSelector.click();
            waitForConversationsList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToComposeEmail() {
        UiObject2 compose = mDevice.findObject(By.desc("Compose"));
        Assert.assertNotNull("No compose button found", compose);
        compose.clickAndWait(Until.newWindow(), COMPOSE_TIMEOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openEmailByIndex(int index) {
        if (!isInPrimaryOrInbox()) {
            throw new IllegalStateException(
                    "Must be in Primary or Inbox to open an e-mail by index.");
        }

        if (index >= getVisibleEmailCount()) {
            throw new IllegalArgumentException(String.format("Cannot select %s'th message of %s",
                    (index + 1), getVisibleEmailCount()));
        }

        // Select an e-mail by index
        UiObject2 conversationList = getConversationList();
        List<UiObject2> emails = conversationList.findObjects(
                By.clazz(android.widget.FrameLayout.class));
        Assert.assertNotNull("No e-mails found.", emails);
        emails.get(index).click();

        // Wait until the e-mail is open
        UiObject2 loadMsg = mDevice.findObject(By.res(UI_PACKAGE_NAME, "loading_progress"));
        if (loadMsg != null) {
            if (!mDevice.wait(Until.gone(
                    By.res(UI_PACKAGE_NAME, "loading_progress")), LOADING_TIMEOUT)) {
                throw new RuntimeException("Loading message timed out after 20s");
            }
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int getVisibleEmailCount() {
        if (!isInPrimaryOrInbox()) {
            throw new IllegalStateException(
                    "Must be in Primary or Inbox to open an e-mail by index.");
        }

        return getConversationList().getChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendReplyEmail(String address, String body) {
        if (!isInEmail()) {
            Assert.fail("Must have an e-mail open to send a reply.");
        }

        // Scroll to the e-mail bottom and press reply.
        UiObject2 convScroll = getConversationScrollContainer();
        convScroll.scroll(Direction.DOWN, 100.0f);
        mDevice.findObject(By.text("Reply")).click();

        // Set the necessary fields (address and body)
        setEmailToAddress(address);
        setEmailBody(body);

        // Send the reply e-mail and wait for a new window.
        mDevice.findObject(By.desc("Send")).clickAndWait(Until.newWindow(), SEND_TIMEOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEmailToAddress(String address) {
        UiObject2 toField = getToField();
        toField.setText(address);
        // Hide suggested e-mail addresses by clicked the "To" field again
        toField.click();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEmailBody(String body) {
        UiObject2 bodyField = getBodyField();
        bodyField.setText(body);
        // Make sure to leave focus on the "Body" field
        bodyField.click();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComposeEmailBody(){
        UiObject2 bodyField = getBodyField();
        return bodyField.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openNavigationDrawer() {
        for (int retries = 3; retries > 0; retries--) {
            if (isNavDrawerOpen()) {
                return;
            }

            UiObject2 nav = mDevice.findObject(By.desc("Navigate up"));
            Assert.assertNotNull("'Navigate up' object not found.", nav);
            nav.click();
            mDevice.waitForIdle();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scrollNavigationDrawer(Direction dir) {
        if (dir == Direction.RIGHT || dir == Direction.LEFT) {
            throw new IllegalArgumentException("Can only scroll navigation drawer up and down.");
        }

        UiObject2 scroll = getNavDrawerContainer();
        Assert.assertNotNull("No navigation drawer found to scroll", scroll);
        scroll.scroll(dir, 1.0f);
    }

    /**
     * {@inheritDoc}
     */
    public boolean closeNavigationDrawer() {
        UiObject2 navDrawer = mDevice.wait(Until.findObject(
                By.clazz(ImageButton.class).desc("Close navigation drawer")), 1000);
        if (navDrawer != null) {
            navDrawer.click();
            return true;
        }
        return false;
    }

    private UiObject2 getToField() {
        UiObject2 toField = null;
        for (int retries = 3; retries > 0; retries--) {
            toField = mDevice.findObject(By.res(UI_PACKAGE_NAME, "to"));
            if (toField != null) {
                break;
            } else {
                UiObject2 scroller = getComposeScrollContainer();
                Assert.assertNotNull("No valid scrolling mechanism found.", scroller);
                scroller.scroll(Direction.UP, 1.0f);
            }
        }

        Assert.assertNotNull("No 'to' field found.", toField);
        return toField;
    }

    private UiObject2 getBodyField() {
        UiObject2 bodyField = null;
        for (int retries = 3; retries > 0; retries--) {
            bodyField = mDevice.findObject(By.res(UI_PACKAGE_NAME, "body"));
            if (bodyField != null) {
                break;
            } else {
                UiObject2 scroller = getComposeScrollContainer();
                Assert.assertNotNull("No valid scrolling mechanism found.", scroller);
                scroller.scroll(Direction.DOWN, 1.0f);
            }
        }

        Assert.assertNotNull("No 'body' field found.", bodyField);
        return bodyField;
    }

    private UiObject2 getComposeScrollContainer() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, "compose"));
    }

    private UiObject2 getConversationScrollContainer() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, "conversation_pager"));
    }

    private UiObject2 getNavDrawerContainer() {
        return mDevice.findObject(NAV_DRAWER_SELECTOR);
    }

    private UiObject2 getConversationList() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_CONVERSATIONS_LIST_ID));
    }

    private boolean isInEmail() {
        return mDevice.hasObject(By.res(UI_PACKAGE_NAME, UI_CONVERSATION_CONTAINER_ID));
    }

    private boolean isInPrimaryOrInbox() {
        return getConversationList() != null &&
                (mDevice.hasObject(By.text("Primary")) || mDevice.hasObject(By.text("Inbox")));
    }

    private boolean isNavDrawerOpen() {
        return mDevice.hasObject(NAV_DRAWER_SELECTOR);
    }

    /**
     * Wait for the conversations list to be visible.
     */
    private void waitForConversationsList () {
        mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE_NAME, UI_CONVERSATIONS_LIST_ID)), 3500);
    }
}
