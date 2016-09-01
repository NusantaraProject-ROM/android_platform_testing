/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.androidbvt.sim;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.telecom.TelecomManager;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.KeyEvent;
import com.android.androidbvt.AndroidBvtHelper;
import junit.framework.TestCase;

public class ConnectivityWithSimTests extends TestCase {
    private UiDevice mDevice;
    private Context mContext = null;
    private AndroidBvtHelper mABvtHelper = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.setOrientationNatural();
        mContext = InstrumentationRegistry.getTargetContext();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext,
                InstrumentationRegistry.getInstrumentation().getUiAutomation());
        if (!mABvtHelper.hasDeviceSim()) {
            fail("Device has no sim");
        }
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext,
                InstrumentationRegistry.getInstrumentation().getUiAutomation());
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
     * check wifi calling option is displayed and by default off
     */
    @MediumTest
    public void testWifiCallingNotDefaultEnabled() throws InterruptedException {
        mABvtHelper.launchIntent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        assertNotNull("Wifi Calling status should be off by default",
                getWifiStatusObjectByStatus("Off"));
    }

    /**
     * tests while wifi is off, device can connect to data using mobile data service
     * @throws InterruptedException
     */
    @LargeTest
    public void testMobileDataOnlyConnection() throws InterruptedException {
        int counter;
        int netId = -1;
        try {
            // disconnect wifi if it is connected and save the netid to re-connect in future
            if (mABvtHelper.hasWifiData()) {
                netId = mABvtHelper.disconnectWifi();
            }
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            // ensure device has mobile data service
            assertTrue(mABvtHelper.hasMobileData());
            // verify device is connected to web
            counter = 5;
            while (--counter > 0 && !mABvtHelper.isConnected()) {
                Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            }
            assertTrue("Device has no data connection over mobile data", counter != 0);
        } finally {
            // finally re-connect wifi
            if (netId != -1) {
                mABvtHelper.getWifiManager().enableNetwork(netId, true);
                counter = 5;
                while (--counter > 0 && !mABvtHelper.isConnected()) {
                    Thread.sleep(mABvtHelper.LONG_TIMEOUT);
                }
            }
        }
    }

    /**
     * Ensures device can't connect to any data while device is put on Airplane mode
     * @throws InterruptedException
     */
    @LargeTest
    public void testNoDataConnectionWithAirPlaneModeOn() throws InterruptedException {
        assertTrue("Device should be connected via either wifi or mobile data",
                mABvtHelper.isConnected());
        try {
            toggleAirplaneSettingsViaUI("Off");
            int counter = 5;
            while (--counter > 0 && mABvtHelper.isConnected()) {
                Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            }
            assertTrue("Device is still connected to data", counter != 0);
        } finally {
            toggleAirplaneSettingsViaUI("On");
            int counter = 5;
            while (--counter > 0 && !mABvtHelper.isConnected()) {
                Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            }
            assertTrue("Device is not connected to data yet", counter != 0);
        }
    }

    /**
     * Ensure wifi calling has wifi preferred mode
     * @throws InterruptedException
     */
    @LargeTest
    public void testWifiCallingHasPreferredMode() throws InterruptedException {
        assertTrue("Wifi isn't enabled", mABvtHelper.getWifiManager().isWifiEnabled());
        try {
            turnWiFiCallSettingOnViaUI(true);
            Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
            assertTrue("Wifi calling doesn't have wifi preferred mode", mDevice.wait(
                    Until.hasObject(By.text("Wi-Fi preferred")), mABvtHelper.LONG_TIMEOUT));
        } finally {
            turnWiFiCallSettingOnViaUI(false);
            mDevice.pressHome();
        }
    }

    /**
     * Make a call when wifi calling is setup
     * @throws InterruptedException
     */
    @LargeTest
    public void testWifiOutgoingCall() throws InterruptedException {
        assertTrue("Wifi isn't enabled", mABvtHelper.getWifiManager().isWifiEnabled());
        try {
            turnWiFiCallSettingOnViaUI(true);
            mDevice.pressHome();
            Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
            // Make a call
            Uri uri = Uri.fromParts("tel", "2468", null);
            Bundle extras = new Bundle();
            TelecomManager telecomManager = (TelecomManager) mContext
                    .getSystemService(Context.TELECOM_SERVICE);
            mABvtHelper.getTelecomManager().placeCall(uri, extras);
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            if (mABvtHelper.getTelecomManager().isInCall()) {
                mDevice.pressKeyCode(KeyEvent.KEYCODE_ENDCALL);
            } else {
                fail("Phone call wasn't successful");
            }
        } finally {
            turnWiFiCallSettingOnViaUI(false);
            mDevice.pressHome();
        }
    }

    private UiObject2 getWifiCallingObject() {
        return mDevice.wait(Until.findObject(By.text("Wi-Fi calling")), mABvtHelper.LONG_TIMEOUT);
    }

    private UiObject2 getWifiStatusObjectByStatus(String status) {
        return mDevice.wait(Until.findObject(By.res("android:id/summary").text(status)),
                mABvtHelper.LONG_TIMEOUT);
    }

    private void toggleAirplaneSettingsViaUI(String expected) throws InterruptedException {
        mDevice.openQuickSettings();
        UiObject2 airplaneObject = mDevice.wait(Until.findObject(By.desc("Airplane mode")),
                mABvtHelper.LONG_TIMEOUT);
        assertTrue("Airplane mode isn't set to expected val",
                airplaneObject.getText().equalsIgnoreCase(expected));
        airplaneObject.click();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        mDevice.pressHome();
    }

    private void turnWiFiCallSettingOnViaUI(boolean turnOn) throws InterruptedException {
        String stateNow = (turnOn ? "Off" : "On");
        String stateToBe = (turnOn ? "On" : "Off");
        Intent intent = new Intent(
                android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        mContext.startActivity(intent);
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        getWifiCallingObject().clickAndWait(Until.newWindow(), mABvtHelper.SHORT_TIMEOUT);
        mDevice.wait(
                Until.findObject(By.res("com.android.settings:id/switch_text")),
                mABvtHelper.LONG_TIMEOUT).click();
    }
}
