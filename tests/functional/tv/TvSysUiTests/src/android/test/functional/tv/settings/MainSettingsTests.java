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

package android.test.functional.tv.settings;

import android.test.functional.tv.common.SysUiTestBase;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Functional verification tests for the main settings on TV.
 *
 * adb shell am instrument -w -r \
 * -e class android.test.functional.tv.settings.MainSettingsTests \
 * android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
public class MainSettingsTests extends SysUiTestBase {

    private static final String TAG = MainSettingsTests.class.getSimpleName();
    private static final long LOADING_TIMEOUT_MS = 5000;    // 5 seconds

    private static final Map<String, String[]> DEVICE_SETTINGS = new HashMap<>();
    private static final Map<String, String[]> PREFERENCES_SETTINGS = new HashMap<>();
    private static final Map<String, String[]> REMOTES_SETTINGS = new HashMap<>();
    private static final Map<String, String[]> PERSONAL_SETTINGS = new HashMap<>();
    private static final Map<String, String[]> ACCOUNTS_SETTINGS = new HashMap<>();
    private static final String DEVICE_CATEGORY = "Device";
    private static final String PREFERENCES_CATEGORY = "Preferences";
    private static final String REMOTES_CATEGORY = "Remotes & accessories";
    private static final String PERSONAL_CATEGORY = "Personal";
    private static final String ACCOUNTS_CATEGORY = "Accounts";

    private static final String DEVELOPER_OPTIONS_MENU = "Developer options";
    private static final String PROP_BUILD_DISPLAY = "ro.build.display.id";


    static {
        DEVICE_SETTINGS.put(DEVICE_CATEGORY,
                new String[]{"Network", "Google Cast", "Sound", "Apps", "Screen saver",
                        "Storage & reset", "About"});
        PREFERENCES_SETTINGS.put(PREFERENCES_CATEGORY,
                new String[]{"Date & time", "Language", "Keyboard", "Home screen", "Search",
                        "Speech", "Accessibility"});
        REMOTES_SETTINGS.put(REMOTES_CATEGORY,
                new String[]{"Add accessory"});
        PERSONAL_SETTINGS.put(PERSONAL_CATEGORY,
                new String[]{"Location", "Security & restrictions", "Usage & Diagnostics"});
        ACCOUNTS_SETTINGS.put(ACCOUNTS_CATEGORY,
                new String[]{"Add account"});

    }


    public MainSettingsTests() {
    }

    @Before
    public void setUp() {
        mLauncherStrategy.open();
        mSettingsHelper.open();
    }

    @After
    public void tearDown() {
        mSettingsHelper.exit();
    }

    /**
     * Objective: Verify the important Settings items are visible and accessible.
     */
    @Test
    public void testEnsureSettingsVisible() {
        for (String s : DEVICE_SETTINGS.get(DEVICE_CATEGORY)) {
            Assert.assertTrue(selectSettingsAndExit(s));
        }
        for (String s : PREFERENCES_SETTINGS.get(PREFERENCES_CATEGORY)) {
            Assert.assertTrue(selectSettingsAndExit(s));
        }
        // The Developer options may not appear
        if (mSettingsHelper.isDeveloperOptionsEnabled()) {
            Assert.assertTrue(selectSettingsAndExit(DEVELOPER_OPTIONS_MENU));
        }
        // Skipping "Remotes & accessories" that needs to be covered in pairing steps.
        //for (String s : REMOTES_SETTINGS.get(REMOTES_CATEGORY)) {
        //    Assert.assertTrue(selectSettingsAndExit(s));
        //}
        for (String s : PERSONAL_SETTINGS.get(PERSONAL_CATEGORY)) {
            Assert.assertTrue(selectSettingsAndExit(s));
        }
        for (String s : ACCOUNTS_SETTINGS.get(ACCOUNTS_CATEGORY)) {
            Assert.assertTrue(selectSettingsAndExit(s));
        }

    }

    /**
     * Objective: Verify the build version details match.
     */
    @Test
    public void testBuildVersion() {
        // Open "About"
        Assert.assertTrue(mSettingsHelper.clickSetting("About"));

        // Open "Build"
        // eg, fugu-userdebug 7.0 NRD90E 3040393 dev-keys
        String buildDisplay = mCmdHelper.executeGetProp(PROP_BUILD_DISPLAY);
        Assert.assertTrue(buildDisplay.equals(mSettingsHelper.getSummaryTextByTitle("Build")));
    }

    private boolean selectSettingsAndExit(String title) {
        Log.d(TAG, String.format("Checking %s ...", title));
        boolean ret = mSettingsHelper.clickSetting(title) &&
                mSettingsHelper.goBackGuidedSettings(1);
        if (!ret) {
            Log.e(TAG, String.format("Failed to find and exit the setting \"%s\"", title));
        }
        return ret;
    }
}

