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

import android.os.SystemClock;
import android.support.test.uiautomator.Direction;
import android.test.functional.tv.common.SysUiTestBase;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional verification tests for Recents on TV.
 *
 * adb shell am instrument -w -r \
 *    -e class android.test.functional.tv.sysui.RecentActivityTests \
 *    android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
public class RecentActivityTests extends SysUiTestBase {

    private static final String TAG = RecentActivityTests.class.getSimpleName();
    private static final long SHORT_TIMEOUT_MS = 2000;
    // TODO Use app helpers instead of constants once the helpers are moved in platform_testing
    private static final String APP_NAME_PLAYSTORE = "Play Store";
    private static final String PACKAGE_PLAYSTORE = "com.android.vending";
    private static final String APP_NAME_PLAYMUSIC = "Play Music";
    private static final String PACKAGE_PLAYMUSIC = "com.android.google.music";
    private static String sYouTubeAppName;
    private static String sYouTubePackage;


    public RecentActivityTests() {
        sYouTubeAppName = mYouTubeHelper.getLauncherName();
        sYouTubePackage = mYouTubeHelper.getPackage();
    }

    @Before
    public void setUp() {
        // clear all recent items before test run
        openAndClearAllRecents(SHORT_TIMEOUT_MS);
        mLauncherStrategy.open();
    }

    @After
    public void tearDown() {
        mRecentsHelper.exit();
    }

    /**
     * Objective: Able to bring up Recent tasks, dismiss them, and exit.
     */
    @Test
    public void testAddDismissRecents() {
        // Open two apps
        mLauncherStrategy.launch(sYouTubeAppName, sYouTubePackage);
        mLauncherStrategy.launch(APP_NAME_PLAYSTORE, PACKAGE_PLAYSTORE);
        mLauncherStrategy.open();

        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        // Dismiss the last open application, which is Play Store in this case
        mRecentsHelper.dismissTask();
        // Verify that the Play Store app is gone, and YouTube app is still in Recents.
        Assert.assertFalse("The task should be gone after dismissed in Recents",
                mRecentsHelper.selectTask(APP_NAME_PLAYSTORE));
        Assert.assertTrue(mRecentsHelper.selectTask(sYouTubeAppName));
        // Verify that it exits by pressing a Home key
        mRecentsHelper.exit();
        Assert.assertFalse(mRecentsHelper.isAppInForeground());
    }

    /**
     * Objective: Open an app in Recents
     */
    @Test
    public void testOpenAppOnRecents() {
        // Open an app
        mLauncherStrategy.launch(sYouTubeAppName, sYouTubePackage);
        mLauncherStrategy.open();

        // Reopen the app in Recents
        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        Assert.assertTrue(mRecentsHelper.selectTask(sYouTubeAppName));
        mDPadHelper.pressDPadCenter();
        SystemClock.sleep(SHORT_TIMEOUT_MS);
        // Verify that the application is open from Recents
        Assert.assertTrue(mYouTubeHelper.isAppInForeground());
    }

    /**
     * Objective: "No recent items" is presented when no app has been launched
     */
    @Test
    public void testNoRecentItems() {
        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        Assert.assertTrue("'No recent items' message is presented when no app has been launched",
                mRecentsHelper.hasNoRecentItems());
    }

    /**
     * Objective: Focus should be on the second right when it enters Recent from an app activity
     * Otherwise, the focus is on the right end (the latest item).
     */
    @Test
    public void testFocusOnMostRecent() {
        // Open two apps
        mLauncherStrategy.launch(APP_NAME_PLAYSTORE, PACKAGE_PLAYSTORE);
        mLauncherStrategy.launch(sYouTubeAppName, sYouTubePackage);

        // Verify that the focus should be on the right end when opening Recents on the Home screen
        mLauncherStrategy.open();
        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        Assert.assertEquals("Focus should be on the right end when opening Recents on " +
                "the Home screen", mRecentsHelper.getFocusedTaskName(), sYouTubeAppName);

        // Verify that the focus should be on the second right when opening Recents on app activity
        mDPadHelper.pressDPadCenter();
        SystemClock.sleep(SHORT_TIMEOUT_MS);
        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        Assert.assertEquals("Focus should be on the second right when opening Recents on " +
                "app activity", mRecentsHelper.getFocusedTaskName(), APP_NAME_PLAYSTORE);
    }

    /**
     * Objective: The most Recent task goes to the right
     */
    @Test
    public void testOrderMostRecentToRight() {
        mLauncherStrategy.launch(sYouTubeAppName, sYouTubePackage);
        mLauncherStrategy.launch(APP_NAME_PLAYSTORE, PACKAGE_PLAYSTORE);
        mLauncherStrategy.launch(APP_NAME_PLAYMUSIC, PACKAGE_PLAYMUSIC);
        mLauncherStrategy.open();

        // Verify that the previously open task goes to the left
        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        Assert.assertEquals(mRecentsHelper.getFocusedTaskName(), APP_NAME_PLAYMUSIC);
        mDPadHelper.pressDPad(Direction.LEFT);
        Assert.assertEquals(mRecentsHelper.getFocusedTaskName(), APP_NAME_PLAYSTORE);
        mDPadHelper.pressDPad(Direction.LEFT);
        Assert.assertEquals(mRecentsHelper.getFocusedTaskName(), sYouTubeAppName);
    }

    /**
     * Objective: Only one Recent task is allowed for an each app on Recent activity.
     */
    @Test
    public void testAllowOnlyOneRecentPerApp() {
        // Open an app
        mLauncherStrategy.launch(sYouTubeAppName, sYouTubePackage);
        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        Assert.assertTrue(mRecentsHelper.selectTask(sYouTubeAppName));
        mRecentsHelper.exit();

        // Reopen the same app
        mLauncherStrategy.launch(sYouTubeAppName, sYouTubePackage);
        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        Assert.assertTrue("Allow only one task per each app in Recents",
                mRecentsHelper.getTaskCountOnScreen() == 1);
    }

    private void openAndClearAllRecents(long timeoutMs) {
        try {
            mRecentsHelper.open(timeoutMs);
            mRecentsHelper.clearAll();
            mRecentsHelper.exit();
        } catch (Exception e) {
            // Ignore
            Log.w(TAG, "Failed to clear all in Recents. " + e.getMessage());
        }
    }
}
