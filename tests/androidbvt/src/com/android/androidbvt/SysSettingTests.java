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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import com.android.androidbvt.AndroidBvtHelper.SettingType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import junit.framework.TestCase;
/**
 * Contain following tests for setting tests:
 * -Verify that common settings are set to default value
 * -Verify that user can change common settings using UI
 * -Verify quick settings are displayed on the phone screen
 * -Verify that user can change settings from quick settings panel
 */
public class SysSettingTests extends TestCase {
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final String SWITCH_WIDGET = "switch_widget";
    private static final String WIFI = "Wi-Fi";
    private static final String BLUETOOTH = "Bluetooth";
    private static final String AIRPLANE = "Airplane mode";
    private static final String LOCATION = "Location";
    private static final String DND = "Do not disturb";
    private static final String ZEN_MODE = "zen_mode";
    private static final String FLASHLIGHT="Flashlight";
    private static final String AUTO_ROTATE_SCREEN = "Auto-rotate screen";

    private HashMap<String, String> mGlobalSettings = new HashMap<String, String>();
    {
        // Bluetooth is by default OFF.
        mGlobalSettings.put("bluetooth_on", "0");
        // Airplane mode is by default OFF
        mGlobalSettings.put("airplane_mode_on", "0");
        // Wifi is by default on for testing
        mGlobalSettings.put("wifi_on", "1");
        // Data roaming is by default OFF
        mGlobalSettings.put("data_roaming", "0");
        // Do not Disturb mode is by default OFF
        mGlobalSettings.put("zen_mode", "0");
    }

    private HashMap<String, String> mSystemSettings = new HashMap<String, String>();
    {
        // Automatic Brightness mode is by default on
        mSystemSettings.put("screen_brightness_mode", "1");
        // By default 30 sec before the device goes to sleep after inactivity
        mSystemSettings.put("screen_off_timeout", "30000");
        // By default Font is 1.0
        mSystemSettings.put("font_scale", "1.0");
    }

    private HashMap<String, String> mSecureSettings = new HashMap<String, String>();
    {
        // By default screensaver is enabled
        mSecureSettings.put("screensaver_enabled", "1");
    }

    private Map<SettingType, HashMap<String, String>> mSettings =
            new HashMap<SettingType, HashMap<String, String>>();
    {
        mSettings.put(SettingType.GLOBAL, mGlobalSettings);
        mSettings.put(SettingType.SYSTEM, mSystemSettings);
        mSettings.put(SettingType.SECURE, mSecureSettings);
    }

    private Map<String, List<String>> QuickSettingTiles = new HashMap<String, List<String>>();
    {
        List<String> tiles = Arrays.asList("Wi-Fi", "Do not disturb", "Battery", "Flashlight",
                "screen", "Bluetooth", "Airplane mode", "Location");
        QuickSettingTiles.put("Tablet", tiles);
        List<String> tiles1 = Arrays.asList("Wi-Fi", "SIM", "Do not disturb", "Battery",
                "Flashlight", "screen", "Bluetooth", "Airplane mode", "Location");
        QuickSettingTiles.put("Phone", tiles1);
        List<String> tilesMr1 = Arrays.asList("Wi-Fi", "SIM", "Do not disturb", "Battery",
                "Flashlight", "screen", "Bluetooth", "Airplane mode", "Nearby");
        QuickSettingTiles.put("MR1", tilesMr1);
    }

    private Context mContext = null;
    private AndroidBvtHelper mABvtHelper = null;
    private ContentResolver mResolver = null;
    private UiDevice mDevice = null;
    private boolean isMr1Device = false;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = InstrumentationRegistry.getTargetContext();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.freezeRotation();
        mResolver = mContext.getContentResolver();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext,
                InstrumentationRegistry.getInstrumentation().getUiAutomation());
        mDevice.setOrientationNatural();
        mDevice.pressHome();
        isMr1Device = mABvtHelper.isNexusExperienceDevice();
    }

    @Override
    public void tearDown() throws Exception {
        mDevice.pressHome();
        mDevice.unfreezeRotation();
        mDevice.waitForIdle();
        super.tearDown();
    }

    /**
     * Verify that common settings are set to default value
     */
    @MediumTest
    public void testSettingDefaultValues() {
        for(Entry<SettingType, HashMap<String, String>> settingsByType : mSettings.entrySet()){
            SettingType settingType = settingsByType.getKey();
            if (!(settingType.equals(SettingType.SYSTEM) && mABvtHelper.isTablet())) {
                HashMap<String, String> settings = settingsByType.getValue();
                for (Entry<String, String> settingPair : settings.entrySet()) {
                    assertTrue(
                            String.format("%s does not have default value: %s",
                                    settingPair.getKey(),
                                    settingPair.getValue()),
                            mABvtHelper.getStringSetting(settingType, settingPair.getKey())
                                    .equals(settingPair.getValue()));
                }
            }
        }
    }

    @MediumTest
    public void testNavigationToNOESettings() {
        mABvtHelper.launchApp("com.android.settings", "Settings");
        Pattern pattern = Pattern.compile("Support", Pattern.CASE_INSENSITIVE);
        mDevice.wait(Until.findObject(By.text(pattern)), mABvtHelper.LONG_TIMEOUT).click();
        assertTrue("", mDevice.wait(Until.hasObject(By.text("We're here to help")),
                mABvtHelper.LONG_TIMEOUT));
        assertTrue("", mDevice.wait(Until.hasObject(By.text("Search help & send feedback")),
                mABvtHelper.LONG_TIMEOUT));
        assertTrue("", mDevice.wait(Until.hasObject(By.text("Explore tips & tricks")),
                mABvtHelper.LONG_TIMEOUT));
    }

    /**
     * Verify that user can change common settings using UI
     */
    @LargeTest
    public void testSettingValues() throws Exception {
        //Check wifi setting can be toggled
        verifyWiFiOnOrOff(true, false);
        // check bluetooth setting can be toggled
        verifyBluetoothOnOrOff(true, false);
        // check airplane mode can be toggled
        verifyAirplaneModeOnOrOff(false, false);
        // check Location can be toggled
        verifyLocationSettingsOnOrOff(false, false);
    }

    /**
     * Verify that user can open Quick Setting shade and icons can be displayed in the shade
     */
    @LargeTest
    public void testQuickSettingsOpenAndIconsDisplayed() throws Exception {
        mABvtHelper.launchQuickSettingsAndWait();;
        List<String> tiles = null;
        // Verify quick settings are displayed on the phone screen.
        if (mABvtHelper.isTablet()){
            tiles = QuickSettingTiles.get("Tablet");
        }else if (mABvtHelper.isNexusExperienceDevice()){
            tiles = QuickSettingTiles.get("MR1");
        }else{
            tiles = QuickSettingTiles.get("Phone");
        }
        for (String tile : tiles) {
            UiObject2 quickSettingTile = mDevice.wait(
                    Until.findObject(By.descContains(tile)),
                    mABvtHelper.SHORT_TIMEOUT);
            assertNotNull(String.format("%s did not load correctly", tile),
                    quickSettingTile);
        }
    }

    /**
     * Verify tapping on Settings icon in Quick settings launches Settings.
     */
    public void testLaunchSettingFromQuickSetting() throws Exception {
        mABvtHelper.launchQuickSettingsAndWait();;
        mDevice.wait(Until.findObject(By.descContains("Open settings.")), mABvtHelper.LONG_TIMEOUT)
                .click();
        UiObject2 settingHeading = mDevice.wait(Until.findObject(By.text("Settings")),
                mABvtHelper.LONG_TIMEOUT);
        assertNotNull("Setting menu has not loaded correctly", settingHeading);
    }

    /**
     * Verify User can change settings from quick setting shade: Wifi, Bluetooth, Airplane mode,
     * Location, Do not disturb, flashlight
     */
    @LargeTest
    public void testQuickSettingsChangeFromQuickSettingsPanel() throws Exception {
        mDevice.openQuickSettings();
        // Check wifi setting can be toggled
        verifyWiFiOnOrOff(true, true);
        // check bluetooth setting can be toggled
        verifyBluetoothOnOrOff(true, true);
        // check airplane mode can be toggled
        verifyAirplaneModeOnOrOff(false, true);
        // check DND can be toggled
        verifyQuickSettingDND();
        // check FlashLight can be toggled
        verifyQuickSettingFlashLight();
        // check Orientation can be toggled
        verifyQuickSettingOrientation();
        // check Location can be toggled for legacy devices
        if (!isMr1Device) {
            verifyLocationSettingsOnOrOff(false, true);
        }
    }

    private void verifyWiFiOnOrOff(boolean verifyOn, boolean isQuickSettings) throws Exception {
        String switchText = "ON";
        if (verifyOn) {
            switchText = "OFF";
        }
        WifiManager wifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(!verifyOn);
        if (isQuickSettings){
            mABvtHelper.launchQuickSettingsAndWait();;
            mDevice.wait(Until.findObject(By.descContains(WIFI)),
                    mABvtHelper.LONG_TIMEOUT).click();
        }else{
            mABvtHelper.launchIntent(Settings.ACTION_WIFI_SETTINGS);
            mDevice.wait(Until
                    .findObject(By.res(SETTINGS_PACKAGE, SWITCH_WIDGET).text(switchText)),
                    mABvtHelper.LONG_TIMEOUT)
                    .click();
        }
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        String wifiValue = Settings.Global.getString(mResolver,
                Settings.Global.WIFI_ON);
        if (verifyOn) {
            assertEquals("1", wifiValue);
        } else {
            assertEquals("0", wifiValue);
        }
        mDevice.pressHome();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
    }

    private void verifyBluetoothOnOrOff(boolean verifyOn, boolean isQuickSettings)
            throws Exception {
        String switchText = "ON";
        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (verifyOn) {
            switchText = "OFF";
            bluetoothAdapter.disable();
        } else {
            bluetoothAdapter.enable();
        }
        if (isQuickSettings) {
            mABvtHelper.launchQuickSettingsAndWait();;
            mDevice.wait(Until.findObject(By.descContains(BLUETOOTH)),
                    mABvtHelper.LONG_TIMEOUT).click();
        } else {
            mABvtHelper.launchIntent(Settings.ACTION_BLUETOOTH_SETTINGS);
            mDevice.wait(Until
                    .findObject(By.res(SETTINGS_PACKAGE, SWITCH_WIDGET).text(switchText)),
                    mABvtHelper.LONG_TIMEOUT)
                    .click();
        }
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        String bluetoothValue = Settings.Global.getString(
                mResolver,
                Settings.Global.BLUETOOTH_ON);
        if (verifyOn) {
            assertEquals("1", bluetoothValue);
        } else {
            assertEquals("0", bluetoothValue);
        }
        bluetoothAdapter.disable();
        mDevice.pressHome();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
    }

    private void verifyAirplaneModeOnOrOff(boolean verifyOn, boolean isQuickSettings)
            throws Exception {
        if (verifyOn) {
            Settings.Global.putString(mResolver,
                    Settings.Global.AIRPLANE_MODE_ON, "0");
        }else {
            Settings.Global.putString(mResolver,
                    Settings.Global.AIRPLANE_MODE_ON, "1");
        }
        if (isQuickSettings) {
            mABvtHelper.launchQuickSettingsAndWait();
            mDevice.wait(Until.findObject(By.descContains(AIRPLANE)),
                    mABvtHelper.LONG_TIMEOUT).click();
        } else {
            mABvtHelper.launchIntent(Settings.ACTION_WIRELESS_SETTINGS);
            mDevice.wait(Until
                    .findObject(By.text(AIRPLANE)), mABvtHelper.LONG_TIMEOUT)
                    .click();
        }
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        String airplaneModeValue = Settings.Global
                .getString(mResolver,
                Settings.Global.AIRPLANE_MODE_ON);
        if (verifyOn) {
            assertEquals("1", airplaneModeValue);
        }
        else {
            assertEquals("0", airplaneModeValue);
        }
        mDevice.pressHome();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
    }

    private void verifyLocationSettingsOnOrOff(boolean verifyOn, boolean isQuickSettings)
            throws Exception {
        // Set location flag
        if (verifyOn) {
            Settings.Secure.putInt(mResolver,
                    Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        }
        else {
            Settings.Secure.putInt(mResolver,
                    Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
        }
        // Load location settings
        if (isQuickSettings) {
            mABvtHelper.launchQuickSettingsAndWait();
            mDevice.wait(Until.findObject(By.descContains(LOCATION)),
                    mABvtHelper.LONG_TIMEOUT).click();
        } else {
            mABvtHelper.launchIntent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            // Toggle UI
            mDevice.wait(Until.findObject(By.res(SETTINGS_PACKAGE, SWITCH_WIDGET)),
                    mABvtHelper.LONG_TIMEOUT).click();
        }
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        // Verify change in setting
        int locationEnabled = Settings.Secure.getInt(mResolver,
                 Settings.Secure.LOCATION_MODE);
        if (verifyOn) {
            assertFalse("Location not enabled correctly", locationEnabled == 0);
        }else {
            assertEquals("Location not disabled correctly", 0, locationEnabled);
        }
        mDevice.pressHome();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
    }

    /**
     * Verify Quick Setting DND can be toggled
     * DND default value is OFF
     * @throws Exception
     */
    private void verifyQuickSettingDND() throws Exception {
        try{
            int onSetting = Settings.Global.getInt(mResolver, ZEN_MODE);
            mABvtHelper.launchQuickSettingsAndWait();
            mDevice.wait(Until.findObject(By.descContains(DND)),
                    mABvtHelper.LONG_TIMEOUT).click();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            int changedSetting = Settings.Global.getInt(mResolver, ZEN_MODE);
            assertFalse(onSetting == changedSetting);
            mDevice.pressHome();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        }finally{
            //change to DND default value
            int setting = Settings.Global.getInt(mResolver, ZEN_MODE);
            if (setting > 0){
                mABvtHelper.launchQuickSettingsAndWait();;
                mDevice.wait(Until.findObject(By.descContains(DND)),
                        mABvtHelper.LONG_TIMEOUT).click();
                Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            }
        }
    }

    private void verifyQuickSettingFlashLight() throws Exception {
        String lightOn = "On";
        String lightOff = "Off";
        boolean verifyOn = false;
        mABvtHelper.launchQuickSettingsAndWait();
        UiObject2 flashLight = mDevice.wait(
                Until.findObject(By.descContains(FLASHLIGHT)),
                mABvtHelper.LONG_TIMEOUT);
        if (flashLight.getText().equals(lightOn)) {
            verifyOn = true;
        }
        mDevice.wait(Until.findObject(By.textContains(FLASHLIGHT)),
                mABvtHelper.LONG_TIMEOUT).click();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        flashLight = mDevice.wait(
                Until.findObject(By.descContains(FLASHLIGHT)),
                mABvtHelper.LONG_TIMEOUT);
        if (verifyOn) {
            assertTrue(flashLight.getText().equals(lightOff));
        } else {
            assertTrue(flashLight.getText().equals(lightOn));
            mDevice.wait(Until.findObject(By.textContains(FLASHLIGHT)),
                    mABvtHelper.LONG_TIMEOUT).click();
        }
        mDevice.pressHome();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
    }

    private void verifyQuickSettingOrientation() throws Exception {
        mABvtHelper.launchQuickSettingsAndWait();
        mDevice.wait(Until.findObject(By.descContains(AUTO_ROTATE_SCREEN)),
                mABvtHelper.LONG_TIMEOUT).click();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        String rotation = Settings.System.getString(mResolver,
                Settings.System.ACCELEROMETER_ROTATION);
        assertEquals("1", rotation);
        mDevice.setOrientationNatural();
        mDevice.pressHome();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
    }
}
