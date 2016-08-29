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

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import junit.framework.TestCase;

public class ConnectivityWifiTests extends TestCase {
    private final String NETWORK_ID = "AndroidAP";
    private final String PASSWD = "androidwifi";
    private UiDevice mDevice;
    private WifiManager mWifiManager = null;
    private Context mContext = null;
    private AndroidBvtHelper mABvtHelper = null;
    private WifiConfiguration mOriginalConfig = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.setOrientationNatural();
        mContext = InstrumentationRegistry.getTargetContext();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext,
                InstrumentationRegistry.getInstrumentation().getUiAutomation());
        mWifiManager = mABvtHelper.getWifiManager();
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
     * Test verifies wifi can be disconnected, disabled followed by enable and reconnect. As part of
     * connection check, it pings a site and ensures HTTP_OK return
     */
    @LargeTest
    public void testWifiConnection() throws InterruptedException {
        // Wifi is already connected as part of tradefed device setup, assert that
        assertTrue("Wifi should be connected", mABvtHelper.isWifiConnected());
        assertNotNull("Wifi manager is null", mWifiManager);
        assertTrue("Wifi isn't enabled", mWifiManager.isWifiEnabled());
        // Disconnect wifi and disable network, save NetId to be used for re-enabling network
        int netId = mWifiManager.getConnectionInfo().getNetworkId();
        mABvtHelper.disconnectWifi();
        Log.d("MyTestTag", "before sleep");
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        Log.d("MyTestTag", "after sleep");
        assertFalse("Wifi shouldn't be connected", mABvtHelper.isWifiConnected());
        // Network enabled successfully
        assertTrue("Network isn't enabled", mWifiManager.enableNetwork(netId, true));
        // Allow time to settle down
        Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
        assertTrue("Wifi should be connected", mABvtHelper.isWifiConnected());
    }

    /**
     * Test verifies from UI that bunch of AP are listed on enabling Wifi
     */
    @LargeTest
    public void testWifiDiscoveredAPShownUI() throws InterruptedException {
        Intent intent_as = new Intent(
                android.provider.Settings.ACTION_WIFI_SETTINGS);
        mContext.startActivity(intent_as);
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        assertNotNull("AP list shouldn't be null",
                mDevice.wait(Until.findObject(By.res(mABvtHelper.SETTINGS_PACKAGE, "list")),
                        mABvtHelper.LONG_TIMEOUT));
        assertTrue("At least 1 AP should be visible",
                mDevice.wait(Until.findObject(By.res(mABvtHelper.SETTINGS_PACKAGE, "list")),
                        mABvtHelper.LONG_TIMEOUT)
                        .getChildren().size() > 0);
    }

    /**
     * Verifies WifiAp is by default disabled Then enable adn disable it
     */
    @LargeTest
    @Suppress
    public void testWifiTetheringDisableEnable() throws InterruptedException {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = NETWORK_ID;
        config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
        config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
        config.preSharedKey = PASSWD;
        int counter;
        try {
            // disable wifiap
            assertTrue("wifi hotspot not disabled by default",
                    mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_DISABLED);
            // Enable wifiap
            assertTrue("failed to disable wifi hotspot",
                    mWifiManager.setWifiApEnabled(config, true));
            Log.d("MyTestTag", "Now checkign wifi ap");
            counter = 10;
            while (--counter > 0
                    && mWifiManager.getWifiApState() != WifiManager.WIFI_AP_STATE_ENABLED) {
                Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
            }
            assertTrue("wifi hotspot not enabled",
                    mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED);
            // Navigate to Wireless Settings page and verify Wifi AP setting is on
            Intent intent_as = new Intent(
                    android.provider.Settings.ACTION_WIRELESS_SETTINGS);
            mContext.startActivity(intent_as);
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            mDevice.wait(Until.findObject(By.text("Tethering & portable hotspot")),
                    mABvtHelper.LONG_TIMEOUT).click();
            Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
            assertTrue("Settings UI for Wifi AP is not ON",
                    mDevice.wait(Until.hasObject(By.text("Portable hotspot AndroidAP active")),
                            mABvtHelper.LONG_TIMEOUT));

            mDevice.wait(Until.findObject(By.text("Portable Wi‑Fi hotspot")),
                    mABvtHelper.LONG_TIMEOUT).click();
            assertTrue("Wifi ap disable call fails", mWifiManager.setWifiApEnabled(config,
                    false));
            counter = 5;
            while (--counter > 0
                    && mWifiManager.getWifiApState() != WifiManager.WIFI_AP_STATE_DISABLED) {
                Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            }
            assertTrue("wifi hotspot not enabled",
                    mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_DISABLED);
            Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
        } finally {
            assertTrue("Wifi enable call fails", mWifiManager
                    .enableNetwork(mWifiManager.getConnectionInfo().getNetworkId(), false));
            counter = 10;
            while (--counter > 0 && !mWifiManager.isWifiEnabled()) {
                Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            }
            assertTrue("Wifi isn't enabled", mWifiManager.isWifiEnabled());
        }
    }
}
