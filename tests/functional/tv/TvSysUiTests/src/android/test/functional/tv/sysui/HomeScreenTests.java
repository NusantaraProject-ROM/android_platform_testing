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

package android.test.functional.tv.sysui;

import android.test.functional.tv.common.SysUiTestBase;
import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional verification tests for leanback launcher.
 *
 * adb shell am instrument -w -r \
 *    -e class android.test.functional.tv.sysui.HomeScreenTests \
 *    android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
public class HomeScreenTests extends SysUiTestBase {

    private static final String TAG = HomeScreenTests.class.getSimpleName();

    @Before
    public void setUp() {
        mLauncherStrategy.open();
    }

    /**
     * Objective: Verify the rows on Home screen
     * - Search orbs & Clock widget
     * - Recommendations row (Google + 3rd party apps)
     * - Apps row
     * - Games row (optional: In case of installed the game apps)
     * - Settings & Network
     */
    @Test
    public void testSelectHomeScreenRows() {
        Log.d(TAG, "testSelectHomeScreenRows");
        Assert.assertNotNull("Failed to select the Recommendations row",
                mLauncherStrategy.selectNotificationRow());
        Assert.assertNotNull("Failed to select the Search orbs",
                mLauncherStrategy.selectSearchRow());
        Assert.assertNotNull("Failed to find the app widget",
                mLauncherStrategy.hasAppWidgetSelector());
        Assert.assertNotNull("Failed to select the Apps row",
                mLauncherStrategy.selectAppsRow());

        Assert.assertNotNull("Failed to select the Settings row",
                mLauncherStrategy.selectSettingsRow());
    }
}

