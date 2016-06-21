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

package android.tvsettings.functional;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.platform.test.helpers.SettingsHelperImpl;
import android.provider.Settings;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import java.lang.Exception;
import java.lang.Override;

public class CaptionDisplaySettingsTests extends InstrumentationTestCase {
    private static final String SETTINGS_PKG = "com.android.tv.settings";
    private static final String CAPTIONING_SETTING = "accessibility_captioning_enabled";
    private static final String ACCESSIBILITY_TXT = "Accessibility";
    private static final String CAPTIONS_TXT = "Captions";
    private static final String DISPLAY_TXT = "Display";
    private static final String ENABLED_TXT = "ON";
    private static final String DISABLED_TXT = "OFF";
    private static final String CAPTION_SAMPLE_RES = "com.android.tv.settings:id/preview_text";
    private static final int TIMEOUT = 3000;

    private UiDevice mDevice;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Launch the settings app
        Context context = getInstrumentation().getContext();
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        UiObject2 view =
                mDevice.wait(
                        Until.findObject(
                                By.res(SETTINGS_PKG + ":id/settings_dialog_container")),
                        TIMEOUT * 3);
        assertNotNull("Could not find main Settings screen", view);

        // Launch accessibility page
        UiObject2 accessibilityMenu = findMenuItemOnScreen(ACCESSIBILITY_TXT);
        //accessibilityMenu.click();
        assertNotNull("Could not find Accessibility Menu", accessibilityMenu);
        mDevice.pressEnter();
        Thread.sleep(TIMEOUT);

        // Open captions menu
        UiObject2 captionsMenu = findMenuItemOnScreen(CAPTIONS_TXT);
        //captionsMenu.click();
        assertNotNull("Could not find Captions Menu", captionsMenu);
        mDevice.pressEnter();
        Thread.sleep(TIMEOUT);

        assertNotNull("Could not find Display option", findMenuItemOnScreen(DISPLAY_TXT));
    }

    @Override
    public void tearDown() throws Exception {
        // Finish settings activity
        mDevice.pressBack();
        mDevice.pressHome();
        super.tearDown();
    }

    @MediumTest
    public void testEnableCaptionDisplay() throws Exception {
        // Check is captions are disabled; if they are, enabled them
        if (mDevice.wait(Until.findObject(
                By.res("android:id/switch_widget").checked(false)), TIMEOUT) != null) {
            mDevice.pressEnter();
            Thread.sleep(TIMEOUT);
        }
        assertNotNull("Display toggle not set to ON!", mDevice.wait(
                Until.findObject(By.res("android:id/switch_widget").checked(true)), TIMEOUT));
        assertNotNull("Sample text not found!", mDevice.wait(
                Until.findObject(By.res(CAPTION_SAMPLE_RES)), TIMEOUT));
        int settingValue = Settings.Secure.getInt(
                getInstrumentation().getContext().getContentResolver(),
                CAPTIONING_SETTING);
        assertEquals("Error: Caption display not enabled!", settingValue, 1);
    }

    @MediumTest
    public void testDisableCaptionDisplay() throws Exception {
        // Check is captions are enabled; if they are, disable them
        if (mDevice.wait(
                Until.findObject(
                        By.res("android:id/switch_widget").checked(true)), TIMEOUT) != null) {
            mDevice.pressEnter();
            Thread.sleep(TIMEOUT);
        }
        assertNotNull("Display toggle not set to OFF!", mDevice.wait(
                Until.findObject(By.res("android:id/switch_widget").checked(false)), TIMEOUT));
        assertNull("Sample text found!", mDevice.wait(
                Until.findObject(By.res(CAPTION_SAMPLE_RES)), TIMEOUT));
        int settingValue = Settings.Secure.getInt(
                getInstrumentation().getContext().getContentResolver(),
                CAPTIONING_SETTING);
        assertEquals("Error: Caption display not enabled!", settingValue, 0);
    }

    /* This helper function iterates through menu items in the Settings menu until finding the entry
     * matching "item".  Please note that the function currently iterates through a maximum of
     * 20 items, so this may need to be adjusted in the event of sweeping changes to the Settings
     * menu. */
    private UiObject2 findMenuItemOnScreen(String item) throws Exception {
        int count = 0;
        UiObject2 setting = null;
        while (count < 20 && setting == null) {
            setting = mDevice.wait(
                    Until.findObject(By.hasDescendant(By.text(item)).focused(true)), TIMEOUT);
            if (setting != null)
                break;
            mDevice.pressDPadDown();
            count++;
        }
        return setting;
    }
}