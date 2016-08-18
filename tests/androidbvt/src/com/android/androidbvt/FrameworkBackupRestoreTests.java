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

package com.android.androidbvt;

import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import java.util.List;
import junit.framework.TestCase;

/**
 * Class contains tests to verify an account can be enabled as backup account 'bmgr' service backsup
 * and restore FYI: 'bmgr' svc doesn't verify what has been stored in cloud e2e, rather relies on
 * the fact if backed up contents can be restored, it works e2e
 */
public class FrameworkBackupRestoreTests extends TestCase {
    private UiDevice mDevice;
    private Context mContext = null;
    private AndroidBvtHelper mABvtHelper = null;
    private WifiConfiguration mOriginalConfig = null;
    private ContentResolver mResolver = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.setOrientationNatural();
        mContext = InstrumentationRegistry.getTargetContext();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext,
                InstrumentationRegistry.getInstrumentation().getUiAutomation());
        mResolver = mContext.getContentResolver();

    }

    @Override
    public void tearDown() throws Exception {
        mDevice.wakeUp();
        mDevice.unfreezeRotation();
        mDevice.pressHome();
        mDevice.waitForIdle();
        super.tearDown();
    }

    /**
     * Ensure an account can be set as backup account
     * @throws InterruptedException
     */
    @LargeTest
    public void testAddBackupAccount() throws InterruptedException {
        // Enable back manager
        mABvtHelper.enableBmgr(true);
        Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
        enableAccountAsBackup();
    }

    /**
     * Test to ensure whether bmgr can backup and restore data. It doesn't test integration with
     * GMSCore and storing data in the cloud rather that is implied
     * 1. Enable test account as backup account
     * 2 .Make some setting changes and backup the data
     * 3. Change settings to default value
     * 4. Restore data and verify that it restores to the state 2
     * @throws InterruptedException
     */
    @LargeTest
    public void testBmgrBacksUpAndRestore() throws InterruptedException {
        // Enable back manager
        mABvtHelper.enableBmgr(true);
        Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
        enableAccountAsBackup();
        try {
            // Change Display -> Adaptive Brightness 0 - 1
            mABvtHelper.setSettingStringValue(AndroidBvtHelper.SettingType.SYSTEM,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, "1");
            Boolean backUpSuccess = false;
            List<String> cmdOut = mABvtHelper
                    .executeShellCommand(
                            String.format("bmgr backupnow %s", mABvtHelper.SETTINGS_PACKAGE));
            for (String str : cmdOut) {
                if (str.equalsIgnoreCase("Backup finished with result: Success")) {
                    backUpSuccess = true;
                    continue;
                }
            }
            assertTrue("bmgr failed in backup data now", backUpSuccess);
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            // Set Display -> Adaptive Brightness 1 - 0
            mABvtHelper.setSettingStringValue(AndroidBvtHelper.SettingType.SYSTEM,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, "0");
            int counter = 10;
            while (--counter > 0 && Settings.System
                    .getString(mResolver, Settings.System.SCREEN_BRIGHTNESS_MODE).equals("1")) {
                mABvtHelper.executeShellCommand(
                        String.format("bmgr restore %s", mABvtHelper.SETTINGS_PACKAGE));
            }
        } finally {
            // Set Display -> Adaptive Brightness 1 - 0
            mABvtHelper.setSettingStringValue(AndroidBvtHelper.SettingType.SYSTEM,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, "0");
            // Disable back manager
            mABvtHelper.enableBmgr(false);
        }
    }

    private void enableAccountAsBackup() throws InterruptedException {
        if (mABvtHelper.hasDeviceBackupAccount())
            return;
        // Get device google account
        String gAccount = mABvtHelper.getRegisteredGoogleAccountOnDevice();
        mABvtHelper.launchIntent(android.provider.Settings.ACTION_PRIVACY_SETTINGS);
        mDevice.wait(Until.hasObject(By.text("Device backup")), mABvtHelper.LONG_TIMEOUT);
        UiObject2 noAcctBackUp = mDevice.wait(
                Until.findObject(By.textStartsWith("No account")),
                mABvtHelper.LONG_TIMEOUT);
        if (noAcctBackUp != null) {
            noAcctBackUp.clickAndWait(Until.newWindow(), mABvtHelper.LONG_TIMEOUT);
            UiObject2 backUpAcct = mDevice.wait(Until.findObject(By.text("Backup account")),
                    mABvtHelper.LONG_TIMEOUT);
            backUpAcct.clickAndWait(Until.newWindow(), mABvtHelper.LONG_TIMEOUT);
            backUpAcct = mDevice.wait(
                    Until.findObject(By.text(gAccount)),
                    mABvtHelper.LONG_TIMEOUT);
            assertNotNull("Backup account is still not set", backUpAcct);
            backUpAcct.getText().equalsIgnoreCase(gAccount);
            backUpAcct.click();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            backUpAcct = mDevice.wait(
                    Until.findObject(By.text(gAccount)),
                    mABvtHelper.LONG_TIMEOUT);
            assertNotNull("Backup account isn't set to gmail account yet", backUpAcct);
            mDevice.pressHome();
        }
    }
}