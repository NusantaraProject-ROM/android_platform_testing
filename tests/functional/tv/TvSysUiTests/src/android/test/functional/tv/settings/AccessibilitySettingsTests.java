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

import android.provider.Settings;
import android.test.functional.tv.common.SysUiTestBase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional verification tests for the caption display on TV.
 *
 * adb shell am instrument -w -r \
 * -e class android.test.functional.tv.settings.AccessibilitySettingsTests \
 * android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
public class AccessibilitySettingsTests extends SysUiTestBase {
    private static final String TAG = AccessibilitySettingsTests.class.getSimpleName();
    private static final long LOADING_TIMEOUT_MS = 5000;    // 5 seconds

    // The following constants are hidden in API 24.
    private static final String ACCESSIBILITY_CAPTIONING_ENABLED =
            "accessibility_captioning_enabled";
    private static final String ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED =
            "high_text_contrast_enabled";

    private static final String TEXT_ACCESSIBILITY = "Accessibility";
    private static final String TEXT_CAPTIONS = "Captions";
    private static final String TEXT_DISPLAY = "Display";
    private static final String TEXT_HIGHCONTRASTTEXT = "High contrast text";
    private static final int TIMEOUT_MS = 3000;

    private boolean mHighContrastTextOn = false;

    @Before
    public void setUp() {
        mLauncherStrategy.open();
    }

    @After
    public void tearDown() {
        // Clean up: Turn off High contrast text if on.
        if (mHighContrastTextOn) {
            mHighContrastTextOn = false;
            if (mSettingsHelper.isSwitchBarOn(TEXT_HIGHCONTRASTTEXT)) {
                mSettingsHelper.clickSetting(TEXT_HIGHCONTRASTTEXT);
            }
        }

        mSettingsHelper.exit();
    }

    /**
     * Objective: Verify the captioning display is enabled.
     */
    @Test
    public void testEnableCaptionDisplay() throws Exception {
        // Open captions menu
        mSettingsHelper.open(Settings.ACTION_CAPTIONING_SETTINGS, LOADING_TIMEOUT_MS);

        // Turn off if the option is already turned on
        if (mSettingsHelper.isSwitchBarOn(TEXT_DISPLAY)) {
            mSettingsHelper.clickSetting(TEXT_DISPLAY);
        }
        if (!mSettingsHelper.isSwitchBarOff(TEXT_DISPLAY)) {
            throw new IllegalStateException(
                    "The Display setting should be turned off before this test");
        }

        // Enable the caption display
        Assert.assertTrue(mSettingsHelper.clickSetting(TEXT_DISPLAY));
        Assert.assertTrue(mSettingsHelper.isSwitchBarOn(TEXT_DISPLAY));

        // Ensure that the sample text appears and the setting is configured correctly
        Assert.assertNotNull("Sample text not found!", mSettingsHelper.hasPreviewText());
        int value = Settings.Secure.getInt(mContext.getContentResolver(),
                ACCESSIBILITY_CAPTIONING_ENABLED);
        Assert.assertEquals("Error: Caption display not enabled!", value, 1);
    }

    /**
     * Objective: Verify the captioning display is disabled.
     */
    @Test
    public void testDisableCaptionDisplay() throws Exception {
        // Open captions menu from main Settings activity
        mSettingsHelper.open();
        mSettingsHelper.clickSetting(TEXT_ACCESSIBILITY);
        mSettingsHelper.clickSetting(TEXT_CAPTIONS);

        // Turn on if the option is already turned off
        if (mSettingsHelper.isSwitchBarOff(TEXT_DISPLAY)) {
            mSettingsHelper.clickSetting(TEXT_DISPLAY);
        }
        if (!mSettingsHelper.isSwitchBarOn(TEXT_DISPLAY)) {
            throw new IllegalStateException(
                    "The Display setting should be turned on before this test");
        }

        // Disable the caption display
        Assert.assertTrue(mSettingsHelper.clickSetting(TEXT_DISPLAY));
        Assert.assertTrue(mSettingsHelper.isSwitchBarOff(TEXT_DISPLAY));

        // Ensure that the setting is configured correctly
        int value = Settings.Secure.getInt(mContext.getContentResolver(),
                ACCESSIBILITY_CAPTIONING_ENABLED);
        Assert.assertEquals("Error: Caption display not disabled!", value, 0);
    }

    /**
     * Objective: Verify that the high contrast text is turned on.
     */
    @Test
    public void testHighContrastTextOn() throws Settings.SettingNotFoundException {
        // Launch accessibility settings
        mSettingsHelper.open();
        Assert.assertTrue(mSettingsHelper.clickSetting(TEXT_ACCESSIBILITY));

        // Turn on High contrast text
        if (mSettingsHelper.isSwitchBarOff(TEXT_HIGHCONTRASTTEXT)) {
            Assert.assertTrue(mSettingsHelper.clickSetting(TEXT_HIGHCONTRASTTEXT));
        }
        Assert.assertTrue(mSettingsHelper.isSwitchBarOn(TEXT_HIGHCONTRASTTEXT));

        // Ensure that the setting is configured correctly
        int value = Settings.Secure.getInt(mContext.getContentResolver(),
                ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED);
        Assert.assertEquals("Error: High contrast text not enabled!", value, 1);
        mHighContrastTextOn = true;
    }

    /**
     * Objective: Verify that the high contrast text is turned off.
     */
    @Test
    public void testHighContrastTextOff() throws Settings.SettingNotFoundException {
        // Launch accessibility settings
        mSettingsHelper.open();
        Assert.assertTrue(mSettingsHelper.clickSetting(TEXT_ACCESSIBILITY));

        // Turn off High contrast text
        if (mSettingsHelper.isSwitchBarOn(TEXT_HIGHCONTRASTTEXT)) {
            Assert.assertTrue(mSettingsHelper.clickSetting(TEXT_HIGHCONTRASTTEXT));
        }
        Assert.assertTrue(mSettingsHelper.isSwitchBarOff(TEXT_HIGHCONTRASTTEXT));

        // Ensure that the setting is configured correctly
        int value = Settings.Secure.getInt(mContext.getContentResolver(),
                ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED);
        Assert.assertEquals("Error: High contrast text not disabled!", value, 0);
    }
}

