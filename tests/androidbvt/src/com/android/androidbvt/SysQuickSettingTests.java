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
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.MediumTest;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/*
 * Basic Quick Setting Tests
 * 1. Check Quick Setting tiles
 * 2. 
 */
public class SysQuickSettingTests extends TestCase {
    private Context mContext = null;
    private AndroidBvtHelper mABvtHelper = null;
    private ContentResolver mResolver = null;
    private UiDevice mDevice = null;

    private Map<String, List<String>> QuickSettingTiles = new HashMap<String, List<String>>();
    {
        List<String> tiles = Arrays.asList("Wi-Fi","Do not disturb","Battery","Flashlight","screen","Bluetooth","Airplane mode","Location");
        QuickSettingTiles.put("Tablet", tiles);
        tiles.add("SIM");
        QuickSettingTiles.put("Phone", tiles);
        List<String> tilesMr1 = Arrays.asList("Wi-Fi","SIM", "Do not disturb","Battery","Flashlight","screen","Bluetooth","Airplane mode","Nearby");
        QuickSettingTiles.put("MR1", tilesMr1);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mContext = InstrumentationRegistry.getTargetContext();
        mResolver = mContext.getContentResolver();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Following test will open Quick Setting shade, and verify icons in the shade
     */
    @MediumTest
    public void testQuickSettings() throws Exception {
        mDevice.openQuickSettings();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
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
        // Verify tapping on Settings icon in Quick settings launches Settings.
        mDevice.wait(Until.findObject(By.descContains("Open settings.")), mABvtHelper.LONG_TIMEOUT)
                .click();
        UiObject2 settingHeading = mDevice.wait(Until.findObject(By.text("Settings")),
                mABvtHelper.LONG_TIMEOUT);
        assertNotNull("Setting menu has not loaded correctly", settingHeading);
    }

    /**
     * Verify User can change settings from quick setting shade
     * can check these setting values: Wifi, Do not disturb, flashlight,Orientation,Bluetooth, Airplane mode
     */
    @MediumTest
    public void testQuickSettingValues() throws Exception{
        mDevice.openQuickSettings();
        //test Airplane mode
        airplaneModeTest();
        //test wifi
        String beforeWifiValue = Settings.Global.getString(mResolver, Settings.Global.WIFI_ON);
        
        //test Do not disturb
        //test FlashLight
        //test Orientation
        //test BlueTooth
        //test Airplane mode
        mDevice.pressBack();
    }

    public void airplaneModeTest() throws Exception{
        int onSetting = Integer.parseInt(Settings.Global.getString(
                mResolver,
                Settings.Global.AIRPLANE_MODE_ON));
        ChangeQuickSetting("Airplane");
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        int changedSetting = Integer.parseInt(Settings.Global.getString(
                mResolver,
                Settings.Global.AIRPLANE_MODE_ON));
        assertTrue("Airplane can't be changed!" , onSetting != changedSetting);
        ChangeQuickSetting("Airplane");
    }

    public void wifiTest(){
        
    }

    public void ChangeQuickSetting(String aSettingDesc){
        UiObject2 aSetting = mDevice.wait(Until.findObject(By.descContains(aSettingDesc)),
                mABvtHelper.LONG_TIMEOUT);
        if (aSetting != null){
            aSetting.click();
        }
    }
}
