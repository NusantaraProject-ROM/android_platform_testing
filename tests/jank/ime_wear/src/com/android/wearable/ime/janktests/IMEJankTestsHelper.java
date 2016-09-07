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

package com.android.wearable.ime.janktests;

import android.app.Instrumentation;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.support.wearable.input.RemoteInputIntent;
import android.util.Log;
import java.io.IOException;
import junit.framework.Assert;

/**
 * Helpers for Wear IME Jank Tests
 */

public class IMEJankTestsHelper {

    public static final int LONG_TIMEOUT = 5000;
    public static final int SHORT_TIMEOUT = 1500;
    public static final int WFM_EXPECTED_FRAMES = 10;
    public static final int GFX_EXPECTED_FRAMES = 10;
    public static final int KEYBOARD_CODE = 1;
    public static final int HANDWRITING_CODE = 2;
    public static final String KEYBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin";
    public static final String HANDWRITING_PACKAGE_NAME = "com.google.android.apps.handwriting.ime";
    private static final String LOG_TAG = IMEJankTestsHelper.class.getSimpleName();
    private static final String HOME_INDICATOR = "charging_icon";
    private static final String KEYBOARD_INDICATOR = "key_pos_del";
    private static final String HANDWRITING_INDICATOR = "sengine_container";
    private static final String IME_BUTTON_NAME = "ime_choice";
    private static final String KEY_QUICK_REPLY_TEXT = "quick_reply";
    private static final String REMOTE_INPUT_TEXT = "Quick reply";
    private static final String REMOTE_INPUT_PACKAGE_NAME = "com.google.android.wearable.app";
    private static final String KEYBOARD_ID =
            "com.google.android.inputmethod.latin/com.google.android.apps.inputmethod.wear.WearIME";
    private static final String HANDWRITING_ID = "com.google.android.apps.handwriting.ime/"
            + "com.google.android.wearable.input.handwriting.HandwriterInputMethodService";
    private static final String SET_IME_CMD = "ime set %s";
    private static final String ENABLE_IME_CMD = "ime enable %s";
    private static final String INPUT_BOX_PACKAGE_NAME =
            "com.google.android.wearable.input.latin.activity";
    private static IMEJankTestsHelper mInstance;
    private UiDevice mDevice;
    private Instrumentation mInstrumentation;

    private IMEJankTestsHelper(UiDevice device, Instrumentation instrumentation) {
        mDevice = device;
        mInstrumentation = instrumentation;
    }

    public static IMEJankTestsHelper getInstance(UiDevice device, Instrumentation instrumentation) {
        if (mInstance == null) {
            mInstance = new IMEJankTestsHelper(device, instrumentation);
        }
        return mInstance;
    }

    public void swipeRight(int heightOffset) {
        mDevice.swipe(50,
                mDevice.getDisplayHeight() / 2 + heightOffset, mDevice.getDisplayWidth() - 25,
                mDevice.getDisplayHeight() / 2 + heightOffset, 30); // slow speed
        SystemClock.sleep(SHORT_TIMEOUT);
    }

    // Helper function to go back to home screen
    public void goBackHome() {
        String launcherPackage = mDevice.getLauncherPackageName();
        UiObject2 homeScreen = mDevice.findObject(By.res(launcherPackage, HOME_INDICATOR));
        int count = 0;
        while (homeScreen == null && count < 5) {
            mDevice.pressBack();
            homeScreen = mDevice.findObject(By.res(launcherPackage, HOME_INDICATOR));
            count++;
        }
        Assert.assertNotNull("Still cannot find home screen", homeScreen);
        SystemClock.sleep(SHORT_TIMEOUT);
    }

    public void launchRemoteInputActivity() {
        RemoteInput[] remoteInputs = new RemoteInput[] {
            new RemoteInput.Builder(KEY_QUICK_REPLY_TEXT).setLabel(REMOTE_INPUT_TEXT).build()
        };
        Intent intent = new Intent(RemoteInputIntent.ACTION_REMOTE_INPUT);
        intent.putExtra(RemoteInputIntent.EXTRA_REMOTE_INPUTS, remoteInputs);
        mInstrumentation.getContext().startActivity(intent);

        // Make sure remote input activity launched
        UiObject2 replyText = mDevice
                .wait(Until.findObject(By.text(REMOTE_INPUT_TEXT)), LONG_TIMEOUT);
        Assert.assertNotNull(replyText);
    }

    public void launchInputBoxActivity() {
        Context context = mInstrumentation.getContext();
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(INPUT_BOX_PACKAGE_NAME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Assert.assertTrue("Input box activity not started",
                mDevice.wait(Until.hasObject(By.pkg(INPUT_BOX_PACKAGE_NAME).depth(0)),
                        LONG_TIMEOUT));
    }

    // Wait until keyboard or handwriting ui components show up
    public void tapIMEButton(int imeType) {
        UiObject2 imeButton = waitForSysAppUiObject2(REMOTE_INPUT_PACKAGE_NAME, IME_BUTTON_NAME);
        Assert.assertNotNull(imeButton);
        imeButton.click();
        waitForIMEOpen(imeType);
    }

    public void activateIMEKeyboard() {
        try {
            mDevice.executeShellCommand(String.format(ENABLE_IME_CMD, KEYBOARD_ID));
            mDevice.executeShellCommand(String.format(SET_IME_CMD, KEYBOARD_ID));
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
        Log.d(LOG_TAG, "Keyboard activated");
    }

    public void activateIMEHandwriting() {
        try {
            mDevice.executeShellCommand(String.format(ENABLE_IME_CMD, HANDWRITING_ID));
            mDevice.executeShellCommand(String.format(SET_IME_CMD, HANDWRITING_ID));
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
        Log.d(LOG_TAG, "Handwriting activated");
    }

    public void pressBack() {
        mDevice.pressBack();
        SystemClock.sleep(SHORT_TIMEOUT);
    }

    public void tapOnScreen(int imeType) {
        mDevice.click(mDevice.getDisplayHeight() / 2, mDevice.getDisplayWidth() / 2);
        waitForIMEOpen(imeType);
    }

    public void clickSoftKey(String softKeyDescription) {
        UiObject2 softKey =
                waitForSysAppUiObject2(KEYBOARD_PACKAGE_NAME, keyPosIdByDesc(softKeyDescription));
        Assert.assertNotNull("Soft Key " + softKeyDescription + " not found in UI", softKey);
        softKey.click();
    }

    private String keyPosIdByDesc(String desc) {
        return String.format("key_pos_%s", desc);
    }

    private void waitForIMEOpen(int imeType) {
        if (imeType == KEYBOARD_CODE) {
            Assert.assertNotNull("Cannot find keyboard",
                    waitForSysAppUiObject2(KEYBOARD_PACKAGE_NAME, KEYBOARD_INDICATOR));
        } else if (imeType == HANDWRITING_CODE) {
            Assert.assertNotNull("Cannot find handwriting",
                    waitForSysAppUiObject2(HANDWRITING_PACKAGE_NAME, HANDWRITING_INDICATOR));
        }
    }

    private UiObject2 waitForSysAppUiObject2(String pkgName, String resourceId) {
        return mDevice.wait(Until.findObject(By.res(pkgName, resourceId)), LONG_TIMEOUT);
    }
}