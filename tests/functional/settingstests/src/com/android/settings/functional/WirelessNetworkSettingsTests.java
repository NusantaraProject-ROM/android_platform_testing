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

package android.settings.functional;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.test.impls.SettingsAppHelper;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;


public class WirelessNetworkSettingsTests extends InstrumentationTestCase {

    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final int TIMEOUT = 2000;
    private UiDevice mDevice;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        try {
            mDevice.setOrientationNatural();
        } catch (RemoteException e) {
            throw new RuntimeException("failed to freeze device orientaion", e);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        // Need to finish settings activity
        mDevice.pressHome();
        super.tearDown();
    }

    @MediumTest
    public void testWiFiEnabled() throws Exception {
        verifyWiFiOnOrOff(true);
    }

    @MediumTest
    public void testWiFiDisabled() throws Exception {
        verifyWiFiOnOrOff(false);
    }

    @MediumTest
    public void testWifiMenuLoadConfigure() throws Exception {
        SettingsAppHelper.launchSettingsPage(getInstrumentation().getContext(),
                Settings.ACTION_WIFI_SETTINGS);
        mDevice.wait(Until.findObject(By.desc("Configure")), TIMEOUT).click();
        Thread.sleep(TIMEOUT);
        UiObject2 configureWiFiHeading = mDevice.wait(Until.findObject(By.text("Configure Wi‑Fi")),
                TIMEOUT);
        assertNotNull("Configure WiFi menu has not loaded correctly", configureWiFiHeading);
    }

    @MediumTest
    public void testNetworkNotificationsOn() throws Exception {
        verifyNetworkNotificationsOnOrOff(true);
    }

    @MediumTest
    public void testNetworkNotificationsOff() throws Exception {
        verifyNetworkNotificationsOnOrOff(false);
    }

    @MediumTest
    public void testKeepWiFiDuringSleepAlways() throws Exception {
        // Change the default and then change it back
        Settings.Global.putInt(getInstrumentation().getContext().getContentResolver(),
                Settings.Global.WIFI_SLEEP_POLICY, Settings.Global.WIFI_SLEEP_POLICY_NEVER);
        verifyKeepWiFiOnDuringSleep("Always", Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
    }

    @MediumTest
    public void testKeepWiFiDuringSleepOnlyWhenPluggedIn() throws Exception {
        verifyKeepWiFiOnDuringSleep("Only when plugged in",
                Settings.Global.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED);
    }

    @MediumTest
    public void testKeepWiFiDuringSleepNever() throws Exception {
        verifyKeepWiFiOnDuringSleep("Never", Settings.Global.WIFI_SLEEP_POLICY_NEVER);
    }

    private void verifyKeepWiFiOnDuringSleep(String settingToBeVerified, int settingValue)
            throws Exception {
        loadWiFiConfigureMenu();
        mDevice.wait(Until.findObject(By.text("Keep Wi‑Fi on during sleep")), TIMEOUT)
                .click();
        mDevice.wait(Until.findObject(By.clazz("android.widget.CheckedTextView")
                .text(settingToBeVerified)), TIMEOUT).click();
        Thread.sleep(TIMEOUT);
        int keepWiFiOnSetting =
                Settings.Global.getInt(getInstrumentation().getContext().getContentResolver(),
                Settings.Global.WIFI_SLEEP_POLICY);
        assertEquals(settingValue, keepWiFiOnSetting);
    }

    private void verifyNetworkNotificationsOnOrOff(boolean verifyOn)
            throws Exception {
        String switchText = "ON";
        if (verifyOn) {
            switchText = "OFF";
            Settings.Global.putString(getInstrumentation().getContext().getContentResolver(),
                    Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, "0");
        }
        else {
            Settings.Global.putString(getInstrumentation().getContext().getContentResolver(),
                    Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, "1");
        }
        loadWiFiConfigureMenu();
        mDevice.wait(Until.findObject(By.res("android:id/switch_widget").text(switchText)), TIMEOUT)
                .click();
        Thread.sleep(TIMEOUT);
        String wifiNotificationValue =
                Settings.Global.getString(getInstrumentation().getContext().getContentResolver(),
                Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON);
        if (verifyOn) {
            assertEquals("1", wifiNotificationValue);
        }
        else {
            assertEquals("0", wifiNotificationValue);
        }
    }

    private void verifyWiFiOnOrOff(boolean verifyOn) throws Exception {
         String switchText = "On";
         if (verifyOn) {
             switchText = "Off";
         }
         WifiManager wifiManager = (WifiManager)getInstrumentation().getContext()
                 .getSystemService(Context.WIFI_SERVICE);
         wifiManager.setWifiEnabled(!verifyOn);
         SettingsAppHelper.launchSettingsPage(getInstrumentation().getContext(),
                 Settings.ACTION_WIFI_SETTINGS);
         mDevice.wait(Until
                 .findObject(By.res(SETTINGS_PACKAGE, "switch_bar").text(switchText)), TIMEOUT)
                 .click();
         Thread.sleep(TIMEOUT);
         String wifiValue =
                 Settings.Global.getString(getInstrumentation().getContext().getContentResolver(),
                 Settings.Global.WIFI_ON);
         if (verifyOn) {
             assertEquals("1", wifiValue);
         }
         else {
             assertEquals("0", wifiValue);
         }
    }

    private void loadWiFiConfigureMenu() throws Exception {
        WifiManager wifiManager = (WifiManager)getInstrumentation().getContext()
                .getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        SettingsAppHelper.launchSettingsPage(getInstrumentation().getContext(),
                Settings.ACTION_WIFI_SETTINGS);
        mDevice.wait(Until.findObject(By.desc("Configure")), TIMEOUT).click();
    }
}
