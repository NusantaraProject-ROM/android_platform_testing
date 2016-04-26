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

import java.util.List;

import junit.framework.Assert;

public class GoogleMessengerHelperImpl extends AbstractGoogleMessengerHelper {
    private static final String TAG = GoogleMessengerHelperImpl.class.getSimpleName();

    private static final String UI_ATTACH_MEDIA_BUTTON_ID = "attach_media_button";
    private static final String UI_CHOOSE_PHOTO_TEXT = "Choose photo";
    private static final String UI_COMPOSE_MESSAGE_TEXT_ID = "compose_message_text";
    private static final String UI_CONTACT_NAME_ID = "contact_name";
    private static final String UI_MEDIA_FROM_DEVICE_DESC = "Choose images from this device";
    private static final String UI_MEDIA_GALLERY_GRID_VIEW_ID = "gallery_grid_view";
    private static final String UI_MEDIA_PICKER_TABSTRIP_ID = "mediapicker_tabstrip";
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

    private UiObject2 getAttachMediaButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_ATTACH_MEDIA_BUTTON_ID));
    }

    private UiObject2 getMediaFromDeviceTab() {
        return mDevice.findObject(By.pkg(UI_PACKAGE_NAME).desc(UI_MEDIA_FROM_DEVICE_DESC));
    }

    private UiObject2 getMediaGalleryGridView() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_MEDIA_GALLERY_GRID_VIEW_ID));
    }

    private boolean isOnHomePage() {
        return (getStartNewConversationButton() != null);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void goToHomePage() {
        for (int retriesRemaining = 5; retriesRemaining > 0 && !isOnHomePage();
                --retriesRemaining) {
            mDevice.pressBack();
            mDevice.waitForIdle();
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void goToNewConversationPage() {
        UiObject2 startNewConversationButton = getStartNewConversationButton();
        Assert.assertNotNull(
                "Could not find start new conversation button", startNewConversationButton);

        startNewConversationButton.click();
        Assert.assertTrue("Could not find recipient text view", mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE_NAME, UI_RECIPIENT_TEXT_VIEW_ID)), UI_DIALOG_WAIT));
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void goToMessagesPage() {
        UiObject2 contact = mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_CONTACT_NAME_ID));
        Assert.assertNotNull("Could not find first contact drop down menu item", contact);

        contact.click();
        Assert.assertTrue("Could not find compose message edit text", mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE_NAME, UI_COMPOSE_MESSAGE_TEXT_ID)), UI_DIALOG_WAIT));
    }

    private void goToFullscreenChooseMediaPage() {
        UiObject2 mediaGalleryGridView = getMediaGalleryGridView();
        Assert.assertNotNull("Could not find media gallery grid view", mediaGalleryGridView);

        mediaGalleryGridView.scroll(Direction.DOWN, 5.0f);
        Assert.assertTrue("Could not find full screen media gallery grid view", mDevice.wait(
                Until.hasObject(By.pkg(UI_PACKAGE_NAME).text(UI_CHOOSE_PHOTO_TEXT)),
                UI_DIALOG_WAIT));
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void scrollMessages(Direction direction) {
        Assert.assertTrue("Direction must be UP or DOWN",
                Direction.UP.equals(direction) || Direction.DOWN.equals(direction));

        UiObject2 messageRecyclerView = getMessageRecyclerView();
        Assert.assertNotNull("Could not find message recycler view", messageRecyclerView);

        messageRecyclerView.scroll(direction, 10.0f);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void clickComposeMessageText() {
        UiObject2 composeMessageEditText = getComposeMessageEditText();
        Assert.assertNotNull("Could not find compose message edit text", composeMessageEditText);

        composeMessageEditText.click();
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void clickSendMessageButton() {
        UiObject2 sendMessageButton = getSendMessageButton();
        Assert.assertNotNull("Could not find send message button", sendMessageButton);

        sendMessageButton.click();
    }

    private void clickAttachMediaButton() {
        UiObject2 attachMediaButton = getAttachMediaButton();
        Assert.assertNotNull("Could not find attach media button", attachMediaButton);

        attachMediaButton.click();
        Assert.assertTrue("Could not find media picker tabstrip", mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE_NAME, UI_MEDIA_PICKER_TABSTRIP_ID)), UI_DIALOG_WAIT));
    }

    private void clickMediaFromDeviceTab() {
        UiObject2 mediaFromDeviceTab = getMediaFromDeviceTab();
        Assert.assertNotNull("Could not find media from device tab", mediaFromDeviceTab);

        if (!mediaFromDeviceTab.isSelected()) {
            mediaFromDeviceTab.click();
            Assert.assertTrue("Media from device tab not selected", mDevice.wait(Until.hasObject(
                    By.pkg(UI_PACKAGE_NAME).desc(UI_MEDIA_FROM_DEVICE_DESC).selected(true)),
                    UI_DIALOG_WAIT));
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void attachMediaFromDevice(int index) {
        clickAttachMediaButton();
        clickMediaFromDeviceTab();
        goToFullscreenChooseMediaPage();

        UiObject2 mediaGalleryGridView = getMediaGalleryGridView();
        Assert.assertNotNull("Could not find media gallery grid view", mediaGalleryGridView);

        List<UiObject2> mediaGalleryChildren = mediaGalleryGridView.getChildren();
        Assert.assertTrue(index >= 0 && index < mediaGalleryChildren.size());

        int imageChildIndex = 1;
        UiObject2 imageView = mediaGalleryChildren.get(index).
                getChildren().get(imageChildIndex);
        while (getMediaGalleryGridView() != null) {
            imageView.click();
            // Needed to prevent StaleObjectException
            SystemClock.sleep(2000);
        }
    }
}
