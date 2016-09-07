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

import android.os.SystemClock;
import android.support.test.uiautomator.Direction;
import android.test.functional.tv.common.SysUiTestBase;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Functional verification tests for multi users not in the restricted profile
 *
 * adb shell am instrument -w -r \
 * -e class android.test.functional.tv.settings.MultiUserTests \
 * android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
public class MultiUserTests extends SysUiTestBase {
    private static final String TAG = MultiUserTests.class.getSimpleName();
    private static final String PIN_CODE = "1010";
    private static final String TEXT_CREATE_RESTRICTED_PROFILE = "Create restricted profile";
    private static final String TEXT_DELETE_RESTRICTED_PROFILE = "Delete restricted profile";
    private static final String TITLE_RESTRICTIONS = "Security & restrictions";

    private static final long SHORT_SLEEP_MS = 3000;    // 3 seconds


    @Before
    public void setUp() {
        mLauncherStrategy.open();
        mSettingsHelper.open();
    }

    @After
    public void tearDown() {
        mSettingsHelper.exit();
        forceRemoveRestrictedProfile();
    }

    /**
     * Objective: Able to create a Restricted Profile and delete
     */
    @Test
    public void testCreateRestrictedProfileAndDelete() {
        // Create the Restricted Profile with the PIN code
        Assert.assertTrue(mSettingsHelper.clickSetting(TITLE_RESTRICTIONS));
        Assert.assertTrue(mSettingsHelper.clickSetting(TEXT_CREATE_RESTRICTED_PROFILE));
        Assert.assertTrue(mSettingsHelper.setNewPinCode(PIN_CODE));
        Assert.assertTrue(mSettingsHelper.reenterPinCode(PIN_CODE));

        // Pick a few apps in Allowed apps
        Assert.assertTrue(mSettingsHelper.waitForOpenGuidedSetting("Allowed apps", SHORT_SLEEP_MS));
        String title = mSettingsHelper.getCurrentFocusedSettingTitle();
        String titlePrev = "";
        while (!title.equals(titlePrev)) {
            // Verify that all apps but "Location" are disabled by default
            if ("Location".equals(title)) {
                Assert.assertTrue(mSettingsHelper.isSwitchBarOn(title));
            } else {
                Assert.assertTrue(mSettingsHelper.isSwitchBarOff(title));
            }
            mDPadHelper.pressDPad(Direction.DOWN);
            titlePrev = title;
            title = mSettingsHelper.getCurrentFocusedSettingTitle();
        }

        final String[] WHITELISTED_APPS = {"YouTube"};
        for (String appName : WHITELISTED_APPS) {
            if (mSettingsHelper.hasSettingByTitle(appName)) {
                mDPadHelper.pressDPadCenter();
                Assert.assertTrue(mSettingsHelper.isSwitchBarOn(appName));
            }
        }

        // Then, go back and delete the profile.
        mSettingsHelper.goBackGuidedSettings(1);
        SystemClock.sleep(SHORT_SLEEP_MS);  // Wait a little until it creates the profile
        mSettingsHelper.exit();
        deleteRestrictedProfileFromLauncher();
        Assert.assertFalse(hasRestrictedUser(mContext));
    }


    /**
     * Objective: Verify that entering wrong password 5 times keeps user waiting
     * for 60 seconds for retry
     */
    @Ignore
    @Test
    public void testEnterWrongPassword5Times() {

    }

    private void deleteRestrictedProfileFromLauncher() {
        if (!hasRestrictedUser(mContext)) {
            Log.d(TAG, "No-op if no restricted profile created");
            return;
        }
        mLauncherStrategy.selectRestrictedProfile();
        mSettingsHelper.clickSetting(TEXT_DELETE_RESTRICTED_PROFILE);
        mSettingsHelper.enterPinCode(PIN_CODE);
        SystemClock.sleep(SHORT_SLEEP_MS);
        if (TEXT_DELETE_RESTRICTED_PROFILE.equals(
                mSettingsHelper.getCurrentFocusedSettingTitle())) {
            throw new IllegalStateException("Failed to delete the Restricted Profile");
        }
    }

    // Force remove a restricted user in the tearDown. Avoid calling this method in tests
    // because it is different from the way user uses.
    // TODO Move this to CommandHelper if necessary
    private void forceRemoveRestrictedProfile() {
        if (!hasRestrictedUser(mContext)) {
            Log.d(TAG, "No-op if no restricted profile created");
            return;
        }
        // Retrieve the ID of a restricted user from the pm command.
        // Example :
        // Users:
        //         UserInfo{0:Owner:13} running
        //         UserInfo{18:Restricted Profile:8}
        String output = mCmdHelper.executeShellCommand("pm list users");
        final Pattern USERS_REGEX = Pattern.compile("UserInfo\\{(\\d+):Restricted Profile:");
        Matcher matcher = USERS_REGEX.matcher(output);
        int userId = 0;
        if (matcher.find()) {
            userId = Integer.parseInt(matcher.group(1));
            Log.i(TAG, String.format("The ID of restricted user is %d", userId));
        }

        if (userId > 0) {
            mCmdHelper.executeShellCommand(String.format("pm remove-user %d", userId));
        }
    }
}
