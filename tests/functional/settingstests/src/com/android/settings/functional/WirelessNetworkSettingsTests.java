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
import android.platform.test.helpers.SettingsHelperImpl;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;


public class WirelessNetworkSettingsTests extends InstrumentationTestCase {
    // These back button presses are performed in tearDown() to exit Wifi
    // Settings sub-menus that a test might finish in. This number should be
    // high enough to account for the deepest sub-menu a test might enter.
    private static final int NUM_BACK_BUTTON_PRESSES = 5;
    private static final int TIMEOUT = 2000;

    // Note: The values of these variables might affect flakiness in tests that involve
    // scrolling. Adjust where necessary.
    private static final float SCROLL_UP_PERCENT = 10.0f;
    private static final float SCROLL_DOWN_PERCENT = 0.5f;
    private static final int MAX_SCROLL_ATTEMPTS = 10;
    private static final int MAX_ADD_NETWORK_BUTTON_ATTEMPTS = 3;
    private static final int SCROLL_SPEED = 2000;

    private static final String SETTINGS_PACKAGE = "com.android.settings";

    private static final String CHECKBOX_CLASS = "android.widget.CheckBox";
    private static final String SPINNER_CLASS = "android.widget.Spinner";
    private static final String EDIT_TEXT_CLASS = "android.widget.EditText";
    private static final String SCROLLVIEW_CLASS = "android.widget.ScrollView";

    private static final String ADD_NETWORK_MENU_CANCEL_BUTTON_TEXT = "CANCEL";
    private static final String ADD_NETWORK_MENU_SAVE_BUTTON_TEXT = "SAVE";
    private static final String ADD_NETWORK_PREFERENCE_TEXT = "Add network";

    private static final String ADD_NETWORK_MENU_ADV_TOGGLE_RES_ID = "wifi_advanced_togglebox";
    private static final String ADD_NETWORK_MENU_IP_SETTINGS_RES_ID = "ip_settings";
    private static final String ADD_NETWORK_MENU_PROXY_SETTINGS_RES_ID = "proxy_settings";
    private static final String ADD_NETWORK_MENU_SECURITY_OPTION_RES_ID = "security";
    private static final String ADD_NETWORK_MENU_SSID_RES_ID = "ssid";

    private static final BySelector ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR =
            By.scrollable(true).clazz(SCROLLVIEW_CLASS);

    private UiDevice mDevice;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        try {
            mDevice.setOrientationNatural();
        } catch (RemoteException e) {
            throw new RuntimeException("failed to freeze device orientation", e);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        // Exit all settings sub-menus.
        for (int i = 0; i < NUM_BACK_BUTTON_PRESSES; ++i) {
            mDevice.pressBack();
        }
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
        loadWiFiConfigureMenu();
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

    @MediumTest
    public void testAddNetworkMenu_Default() throws Exception {
        loadAddNetworkMenu();

        // Submit button should be disabled by default, while cancel button should be enabled.
        assertFalse(mDevice.wait(Until.findObject(
                By.text(ADD_NETWORK_MENU_SAVE_BUTTON_TEXT)), TIMEOUT).isEnabled());
        assertTrue(mDevice.wait(Until.findObject(
                By.text(ADD_NETWORK_MENU_CANCEL_BUTTON_TEXT)), TIMEOUT).isEnabled());

        // Check that the SSID field is defaults to the hint.
        assertEquals("Enter the SSID", mDevice.wait(Until.findObject(By
                .res(SETTINGS_PACKAGE, ADD_NETWORK_MENU_SSID_RES_ID)
                .clazz(EDIT_TEXT_CLASS)), TIMEOUT)
                .getText());

        // Check Security defaults to None.
        assertEquals("None", mDevice.wait(Until.findObject(By
                .res(SETTINGS_PACKAGE, ADD_NETWORK_MENU_SECURITY_OPTION_RES_ID)
                .clazz(SPINNER_CLASS)), TIMEOUT)
                .getChildren().get(0).getText());

        // Check advanced options are collapsed by default.
        assertFalse(mDevice.wait(Until.findObject(By
                .res(SETTINGS_PACKAGE, ADD_NETWORK_MENU_ADV_TOGGLE_RES_ID)
                .clazz(CHECKBOX_CLASS)), TIMEOUT).isChecked());

    }

    @MediumTest
    public void testAddNetworkMenu_Proxy() throws Exception {
        loadAddNetworkMenu();

        // Toggle advanced options.
        mDevice.wait(Until.findObject(By
                .res(SETTINGS_PACKAGE, ADD_NETWORK_MENU_ADV_TOGGLE_RES_ID)
                .clazz(CHECKBOX_CLASS)), TIMEOUT).click();

        // Verify Proxy defaults to None.
        BySelector proxySettingsBySelector =
                By.res(SETTINGS_PACKAGE, ADD_NETWORK_MENU_PROXY_SETTINGS_RES_ID)
                .clazz(SPINNER_CLASS);
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR, proxySettingsBySelector);
        assertEquals("None", mDevice.wait(Until.findObject(proxySettingsBySelector), TIMEOUT)
                .getChildren().get(0).getText());

        // Verify that Proxy Manual fields appear.
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR, proxySettingsBySelector);
        mDevice.wait(Until.findObject(proxySettingsBySelector), TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("Manual")), TIMEOUT).click();
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR,
                By.res(SETTINGS_PACKAGE, "proxy_warning_limited_support"));
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR,
                By.res(SETTINGS_PACKAGE, "proxy_hostname"));
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR,
                By.res(SETTINGS_PACKAGE, "proxy_exclusionlist"));

        // Verify that Proxy Auto-Config options appear.
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR, proxySettingsBySelector);
        mDevice.wait(Until.findObject(proxySettingsBySelector), TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("Proxy Auto-Config")), TIMEOUT).click();
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR,
                By.res(SETTINGS_PACKAGE, "proxy_pac"));
    }

    @MediumTest
    public void testAddNetworkMenu_IpSettings() throws Exception {
        loadAddNetworkMenu();

        // Toggle advanced options.
        mDevice.wait(Until.findObject(By
                .res(SETTINGS_PACKAGE, ADD_NETWORK_MENU_ADV_TOGGLE_RES_ID)
                .clazz(CHECKBOX_CLASS)), TIMEOUT).click();

        // Verify IP settings defaults to DHCP.
        BySelector ipSettingsBySelector =
                By.res(SETTINGS_PACKAGE, ADD_NETWORK_MENU_IP_SETTINGS_RES_ID).clazz(SPINNER_CLASS);
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR, ipSettingsBySelector);
        assertEquals("DHCP", mDevice.wait(Until.findObject(ipSettingsBySelector), TIMEOUT)
                .getChildren().get(0).getText());

        // Verify that Static IP settings options appear.
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR, ipSettingsBySelector).click();
        mDevice.wait(Until.findObject(By.text("Static")), TIMEOUT).click();
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR,
                By.res(SETTINGS_PACKAGE, "ipaddress"));
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR,
                By.res(SETTINGS_PACKAGE, "gateway"));
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR,
                By.res(SETTINGS_PACKAGE, "network_prefix_length"));
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR, By.res(SETTINGS_PACKAGE, "dns1"));
        scrollToObject(ADD_NETWORK_MENU_SCROLLABLE_BY_SELECTOR, By.res(SETTINGS_PACKAGE, "dns2"));
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
         loadWiFiSettingsPage(!verifyOn);
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

    private void loadWiFiSettingsPage(boolean wifiEnabled) throws Exception {
        WifiManager wifiManager = (WifiManager)getInstrumentation().getContext()
                .getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(wifiEnabled);
        SettingsHelper.launchSettingsPage(getInstrumentation().getContext(),
                Settings.ACTION_WIFI_SETTINGS);
    }

    private void loadWiFiConfigureMenu() throws Exception {
        loadWiFiSettingsPage(true);
        mDevice.wait(Until.findObject(By.desc("Configure")), TIMEOUT).click();
    }

    private void loadAddNetworkMenu() throws Exception {
        loadWiFiSettingsPage(true);
        for (int attempts = 0; attempts < MAX_ADD_NETWORK_BUTTON_ATTEMPTS; ++attempts) {
            UiObject2 found = null;
            try {
                scrollToObject(By.scrollable(true), By.text(ADD_NETWORK_PREFERENCE_TEXT)).click();
            } catch (StaleObjectException e) {
                // The network list might have been updated between when the Add network button was
                // found, and when it UI automator attempted to click on it. Retry.
                continue;
            }
            // If we get here, we successfully clicked on the Add network button, so we are done.
            return;
        }

        fail("Failed to load Add Network Menu after " + MAX_ADD_NETWORK_BUTTON_ATTEMPTS
                + " retries");
    }

    private UiObject2 scrollToObject(BySelector scrollabelSelector, BySelector objectSelector)
            throws Exception {
        int attempts = 0;
        UiObject2 scrollable = mDevice.wait(Until.findObject(scrollabelSelector), TIMEOUT);
        if (scrollable == null) {
            fail("Could not find scrollable UI object identified by " + scrollabelSelector);
        }
        UiObject2 found = null;
        // Scroll all the way up first, then all the way down.
        while (true) {
            // Optimization: terminate if we find the object while scrolling up to reset, so
            // we save the time spent scrolling down again.
            boolean canScrollAgain = scrollable.scroll(Direction.UP, SCROLL_UP_PERCENT,
                    SCROLL_SPEED);
            found = mDevice.findObject(objectSelector);
            if (found != null || !canScrollAgain) break;
        }
        for (attempts = 0; found == null && attempts < MAX_SCROLL_ATTEMPTS; ++attempts) {
            // Return value of UiObject2.scroll() is not reliable, so do not use it in loop
            // condition, in case it causes this loop to terminate prematurely.
            scrollable.scroll(Direction.DOWN, SCROLL_DOWN_PERCENT, SCROLL_SPEED);
            found = mDevice.findObject(objectSelector);
        }
        if (found == null) {
            fail("Could not scroll to UI object identified by " + objectSelector);
        }
        return found;
    }
}
