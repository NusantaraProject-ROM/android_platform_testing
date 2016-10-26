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
 * limitations under the License
 */

package android.support.test.systemuihelper;

import android.os.storage.StorageManager;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import java.io.IOException;

/**
 * Implement common helper methods for Lockscreen.
 */
public class LockscreenHelper {
    public static final int LONG_TIMEOUT = 2000;
    public static final String EDIT_TEXT_CLASS_NAME = "android.widget.EditText";
    public static final String MODE_PIN = "PIN";

    private static LockscreenHelper sInstance = null;
    private UiDevice mDevice = null;

    private LockscreenHelper() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    public static LockscreenHelper getInstance() {
        if (sInstance == null) {
            sInstance = new LockscreenHelper();
        }
        return sInstance;
    }

     /**
     * Sets the screen lock pin or password
     * @param pwd text of Password or Pin for lockscreen
     * @param mode indicate if its password or PIN
     * @throws InterruptedException
     */
    public void setScreenLock(String pwd, String mode) throws Exception {
        navigateToScreenLock();
        mDevice.wait(Until.findObject(By.text(mode)), LONG_TIMEOUT).click();
        // set up Secure start-up page
        if (!StorageManager.isFileEncryptedNativeOrEmulated()) {
            mDevice.wait(Until.findObject(By.text("No thanks")), LONG_TIMEOUT).click();
        }
        UiObject2 pinField = mDevice.wait(Until.findObject(By.clazz(EDIT_TEXT_CLASS_NAME)),
                LONG_TIMEOUT);
        pinField.setText(pwd);
        // enter and verify password
        mDevice.pressEnter();
        pinField.setText(pwd);
        mDevice.pressEnter();
        mDevice.wait(Until.findObject(By.text("DONE")), LONG_TIMEOUT).click();
    }

    /**
     * remove Screen Lock
     * @throws InterruptedException
     */
    public void removeScreenLock(String pwd) throws Exception {
        navigateToScreenLock();
        UiObject2 pinField = mDevice.wait(Until.findObject(By.clazz(EDIT_TEXT_CLASS_NAME)),
                LONG_TIMEOUT);
        pinField.setText(pwd);
        mDevice.pressEnter();
        mDevice.wait(Until.findObject(By.text("Swipe")), LONG_TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("YES, REMOVE")), LONG_TIMEOUT).click();
    }

    /**
     * navigate to screen lock setting page
     * @throws InterruptedException
     */
    public void navigateToScreenLock() throws Exception {
        mDevice.pressHome();
        mDevice.executeShellCommand("am start -a " + Settings.ACTION_SECURITY_SETTINGS);
        mDevice.waitForIdle();
        mDevice.wait(Until.findObject(By.text("Screen lock")), LONG_TIMEOUT).click();
    }

    /**
     * Unlock the lockscreen with {@param pwd}.
     */
    public void unlockScreen(String pwd) throws InterruptedException, IOException {
        mDevice.swipe(mDevice.getDisplayWidth() / 2, mDevice.getDisplayHeight(),
                mDevice.getDisplayWidth() / 2, 0, 30);
        mDevice.waitForIdle();
        // enter password to unlock screen
        String command = String.format("%s %s %s", "input", "text", pwd);
        mDevice.executeShellCommand(command);
        mDevice.waitForIdle();
        mDevice.pressEnter();
    }
}