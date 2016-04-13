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

public class GoogleMessengerHelperImpl extends AbstractGoogleMessengerHelper {
    private static final String TAG = GoogleMessengerHelperImpl.class.getCanonicalName();

    private static final String UI_COMPOSE_MESSAGE_TEXT_ID = "compose_message_text";
    private static final String UI_PACKAGE_NAME = "com.google.android.apps.messaging";
    private static final String UI_RECIPIENT_TEXT_VIEW_ID = "recipient_text_view";
    private static final String UI_SEND_MESSAGE_BUTTON_ID = "send_message_button";
    private static final String UI_START_NEW_CONVERSATION_BUTTON_ID =
            "start_new_conversation_button";

    private static final long UI_DIALOG_WAIT = 5000; // 5 sec

    public GoogleMessengerHelperImpl(Instrumentation instr) {
        super(instr);
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
        return "Messenger";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {

    }

    private UiObject2 getStartNewConversationButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_START_NEW_CONVERSATION_BUTTON_ID));
    }

    private UiObject2 getRecipientTextView() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_RECIPIENT_TEXT_VIEW_ID));
    }

    private UiObject2 getComposeMessageEditText() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_COMPOSE_MESSAGE_TEXT_ID));
    }

    private UiObject2 getSendMessageButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_SEND_MESSAGE_BUTTON_ID));
    }

    private UiObject2 getMessageRecyclerView() {
        return mDevice.findObject(By.pkg(UI_PACKAGE_NAME)
            .clazz("android.support.v7.widget.RecyclerView").res("android", "list"));
    }

    private boolean isOnHomePage() {
        return (getStartNewConversationButton() != null);
    }

    public void goToHomePage() {
        for (int retriesRemaining = 5; retriesRemaining > 0 && !isOnHomePage();
                --retriesRemaining) {
            mDevice.pressBack();
            mDevice.waitForIdle();
        }
    }

    public void goToNewConversationPage() {
        UiObject2 startNewConversationButton = getStartNewConversationButton();
        Assert.assertNotNull(
                "Could not find start new conversation button", startNewConversationButton);

        startNewConversationButton.click();
        Assert.assertTrue("Could not find recipient text view", mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE_NAME, UI_RECIPIENT_TEXT_VIEW_ID)), UI_DIALOG_WAIT));
    }

    public void goToMessagesPage() {
        UiObject2 recipientTextView = getRecipientTextView();
        Assert.assertNotNull("Could not find recipient text view", recipientTextView);

        recipientTextView.click();
        mDevice.pressEnter();
        Assert.assertTrue("Could not find compose message edit text", mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE_NAME, UI_COMPOSE_MESSAGE_TEXT_ID)), UI_DIALOG_WAIT));
    }

    public void scrollMessages(Direction direction) {
        Assert.assertTrue("Direction must be UP or DOWN",
                Direction.UP.equals(direction) || Direction.DOWN.equals(direction));

        UiObject2 messageRecyclerView = getMessageRecyclerView();
        Assert.assertNotNull("Could not find message recycler view", messageRecyclerView);

        messageRecyclerView.scroll(direction, 10.0f);
    }

    public void clickComposeMessageText() {
        UiObject2 composeMessageEditText = getComposeMessageEditText();
        Assert.assertNotNull("Could not find compose message edit text", composeMessageEditText);

        composeMessageEditText.click();
    }

    public void clickSendMessageButton() {
        UiObject2 sendMessageButton = getSendMessageButton();
        Assert.assertNotNull("Could not find send message button", sendMessageButton);

        sendMessageButton.click();
    }
}
