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

import static org.junit.Assert.fail;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.platform.test.helpers.DPadHelper;
import android.platform.test.helpers.tv.SysUiSettingsHelperImpl;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.test.functional.tv.common.SysUiTestBase;
import android.test.functional.tv.common.TestSetupInstrumentation;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;


/**
 * Functional verification tests for multi users in the restricted profile
 *
 * Note that this test requires that the primary user created restricted profile and switched to
 * the restricted profile.
 * The test harness needs to set this up before test starts using {@link TestSetupInstrumentation}
 * that provides a way of creating/deleting profiles.
 *
 * adb shell am instrument -w -r \
 * -e class android.test.functional.tv.settings.MultiUserInRestrictedProfileTests \
 * android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
public class MultiUserInRestrictedProfileTests extends SysUiTestBase {
    private static final String TAG = MultiUserInRestrictedProfileTests.class.getSimpleName();
    private static final String TEXT_CREATE_RESTRICTED_PROFILE = "Create restricted profile";
    private static final String TEXT_DELETE_RESTRICTED_PROFILE = "Delete restricted profile";
    private static final String TEXT_ENTER_RESTRICTED_PROFILE = "Enter restricted profile";
    private static final String TEXT_EXIT_RESTRICTED_PROFILE = "Exit restricted profile";
    private static final String TITLE_RESTRICTIONS = "Security & restrictions";
    private static final long SHORT_SLEEP_MS = 3000;    // 3 seconds

    private static final List<String> ALLOWED_SETTINGS = Arrays.asList(
            "Network", "About", "Accessibility", "Location", "Security & restrictions");
    private static final List<String> DISALLOWED_SETTINGS = Arrays.asList(
            // Device category
            "Google Cast", "Sound", "Apps", "Screen saver", "Storage & reset",
            // Preferences category
            "Date & time", "Language", "Keyboard", "Home screen", "Search", "Speech",
            // Personal category
            "Usage & Diagnostics",
            // Accounts category
            "Add account");

    private static final String PIN_CODE = "1010";


    public MultiUserInRestrictedProfileTests() {
    }

    private MultiUserInRestrictedProfileTests(Instrumentation instrumentation) {
        super(instrumentation);
    }

    @Before
    public void setUp() {
        mLauncherStrategy.open();
    }

    @After
    public void tearDown() {
    }

    /**
     * Objective: Verify that the restricted user could see only allowed apps and limited settings.
     */
    @Test
    public void testEnsureSettingsAsRestrictedUser() {
        // TODO Verify that only the selected apps are shown up on the launcher

        // Verify that the Settings has only limited items:
        // Network, About, Accessibility, Add accessory, Location, Security & restrictions
        mSettingsHelper.open();
        for (int count = ALLOWED_SETTINGS.size(); count > 0; count--) {
            String setting = mSettingsHelper.getCurrentFocusedSettingTitle();
            if (DISALLOWED_SETTINGS.contains(setting)) {
                fail(String.format(
                        "This setting is disallowed in the Restricted Profile: " + setting));
            }
            if (ALLOWED_SETTINGS.contains(setting)) {
                Assert.assertTrue(selectSettingsAndExit(setting));
            }
            mDPadHelper.pressDPad(Direction.DOWN);
        }
        mSettingsHelper.exit();
    }

    /**
     * Objective: Verify that the restricted profile doesn't have permission to use Play store
     */
    @Test
    public void testDisallowPlaystoreAsRestrictedUser() {
        long timestamp = mLauncherStrategy.launch("Play Store", "com.android.vending");
        if (timestamp == ILauncherStrategy.LAUNCH_FAILED_TIMESTAMP) {
            throw new IllegalStateException("Failed to launch Play store");
        }
        // TODO Move this to Play store app helper
        Assert.assertTrue(mDevice.hasObject(
                By.res("android", "message").textStartsWith("You don't have permission to use")));
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

    /**
     * Instrumentation setup class for {@link MultiUserInRestrictedProfileTests}
     */
    public static class Setup {

        public static boolean createRestrictedProfile(Instrumentation instrumentation,
                String pinCode, boolean switchUser) {
            if (isRestrictedUser(instrumentation.getContext())) {
                Log.e(TAG, "Already in the restricted mode. The test setup has stopped.");
                return false;
            }
            if (hasRestrictedUser(instrumentation.getContext())) {
                Log.e(TAG, "Already has a restricted user. The test setup has stopped.");
                return false;
            }
            SysUiSettingsHelperImpl settingsHelper = new MultiUserInRestrictedProfileTests(
                    instrumentation).mSettingsHelper;
            // Start from Home screen
            DPadHelper.getInstance(instrumentation).pressHome();

            // Create the restricted profile
            settingsHelper.open();
            settingsHelper.clickSetting(TITLE_RESTRICTIONS);
            settingsHelper.clickSetting(TEXT_CREATE_RESTRICTED_PROFILE);
            settingsHelper.setNewPinCode(pinCode);
            settingsHelper.reenterPinCode(pinCode);

            // Pick a few apps in Allowed apps
            settingsHelper.waitForOpenGuidedSetting("Allowed apps", SHORT_SLEEP_MS);

            // Then, go back and enter the restricted profile.
            settingsHelper.goBackGuidedSettings(1);
            if (switchUser) {
                settingsHelper.clickSetting(TEXT_ENTER_RESTRICTED_PROFILE);
            }
            return hasRestrictedUser(instrumentation.getContext());
        }

        public static boolean deleteRestrictedProfile(Instrumentation instrumentation,
                String pinCode) {
            if (isRestrictedUser(instrumentation.getContext())) {
                Log.e(TAG,
                        "A restricted user cannot delete the profile. The test setup has stopped.");
                return false;
            }
            if (!hasRestrictedUser(instrumentation.getContext())) {
                Log.e(TAG, "There is no restricted user to delete. The test setup has stopped.");
                return false;
            }
            SysUiSettingsHelperImpl settingsHelper = new MultiUserInRestrictedProfileTests(
                    instrumentation).mSettingsHelper;
            // Start from Home screen
            DPadHelper.getInstance(instrumentation).pressHome();

            // Delete the restricted profile
            settingsHelper.open();
            settingsHelper.clickSetting(TITLE_RESTRICTIONS);
            settingsHelper.clickSetting(TEXT_DELETE_RESTRICTED_PROFILE);
            settingsHelper.enterPinCode(pinCode);
            SystemClock.sleep(SHORT_SLEEP_MS);
            if (TEXT_DELETE_RESTRICTED_PROFILE.equals(
                    settingsHelper.getCurrentFocusedSettingTitle())) {
                Log.e(TAG, "The setting to delete the restricted profile should be"
                        + "gone. The test setup has stopped.");
                return false;
            }
            return !hasRestrictedUser(instrumentation.getContext());
        }

        public static boolean exitRestrictedProfile(Instrumentation instrumentation) {
            if (!isRestrictedUser(instrumentation.getContext())) {
                Log.e(TAG, "Not in the restricted mode. The test setup has stopped.");
                return false;
            }
            SysUiSettingsHelperImpl settingsHelper = new MultiUserInRestrictedProfileTests(
                    instrumentation).mSettingsHelper;
            // Start from Home screen
            DPadHelper.getInstance(instrumentation).pressHome();

            // Exit the restricted profile
            settingsHelper.open();
            settingsHelper.clickSetting(TITLE_RESTRICTIONS);
            settingsHelper.clickSetting(TEXT_EXIT_RESTRICTED_PROFILE);
            settingsHelper.enterPinCode(PIN_CODE);
            return true;
        }
    }
}

