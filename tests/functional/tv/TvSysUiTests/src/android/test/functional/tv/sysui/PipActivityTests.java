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

import static android.view.KeyEvent.KEYCODE_HOME;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;

import android.media.session.PlaybackState;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.test.functional.tv.common.SysUiTestBase;
import android.test.functional.tv.common.UiWatchers;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Functional verification tests for Picture-in-picture feature. This test requires to install
 * the demo apk of Android TV Leanback Support Library (com.example.android.tvleanback).
 *
 * adb shell am instrument -w -r \
 *    -e class android.test.functional.tv.sysui.PipActivityTests \
 *    android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
public class PipActivityTests extends SysUiTestBase {

    private static final String TAG = PipActivityTests.class.getSimpleName();
    private static final String WATCHER_ONBOARDING = "PipOnboardingWatcher";
    private static final String PACKAGE_TVLEANBACK = "com.example.android.tvleanback";
    private static final String ACTIVITY_PLAYBACKOVERLAY =
            "com.example.android.tvleanback.ui.PlaybackOverlayActivity";
    private static final long SHORT_SLEEP_MS = 3000;
    private static final long LONG_SLEEP_MS = 10000;
    private static final long SHORT_TIMEOUT_MS = 2000;

    private boolean shouldStopPipPlayback = true;

    private UiWatchers mWatchers;


    @Before
    public void setUp() {
        shouldStopPipPlayback = true;
        // Register the watcher to dismiss the onboarding activity
        mWatchers = new UiWatchers(getInstrumentation());
        mWatchers.registerDismissWatcher(WATCHER_ONBOARDING,
                By.res("com.android.systemui", "pip_onboarding"),
                By.res("com.android.systemui", "button").text("GOT IT"));
        // clear all recent items before test run
        openAndClearAllRecents(SHORT_TIMEOUT_MS);
        mLauncherStrategy.open();
    }

    @After
    public void tearDown() {
        mWatchers.unregisterDismissWatcher(WATCHER_ONBOARDING);
        if (shouldStopPipPlayback) {
            stopPipPlayback();
        }
    }

    /**
     * Objective: Verify that PIP window is open by pressing a button given that
     * the application supports the feature.
     */
    @Test
    public void testOpenPipWindow() {
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        // TODO making hasTooltipShown less flaky
        if (!mLeanbackDemoHelper.hasTooltipShown()) {
            Log.d(TAG, "The tooltip text is not detected when opening PIP window. Test is flaky?");
        }
        Assert.assertTrue(isDemoActivityInPip());
    }

    /**
     * Objective: Verify that PIP window is open by pressing KEYCODE_WINDOW.
     */
    @Test
    public void testOpenPipWindowOnKeySent() throws InterruptedException {
        startPipPlayback("Google+", "Instant Upload");
        SystemClock.sleep(SHORT_SLEEP_MS);

        // Press the PIP key
        mDPadHelper.pressPipKey();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue(isDemoActivityInPip());
    }

    /**
     * Objective: Verify that PIP window is located in center and focused when PIP overlay
     * receives the KEYCODE_WINDOW key.
     */
    @Test
    public void testMovePipMenuToCenterOnKeySent() throws InterruptedException {
        startPipPlayback("Google+", "Instant Upload");
        SystemClock.sleep(SHORT_SLEEP_MS);

        // Press the PIP key
        mDPadHelper.pressPipKey();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue(isDemoActivityInPip());
        // Press the PIP key code again to move PIP window to center
        mDPadHelper.pressPipKey();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue("The PIP menu should be shown in center",
                mPipHelper.isPipStateMenu(PACKAGE_TVLEANBACK, ACTIVITY_PLAYBACKOVERLAY));
    }

    /**
     * Objective: Able to move PIP window to Recents by pressing the Home key
     */
    @Test
    public void testMovePipToRecentsFocused() {
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        SystemClock.sleep(SHORT_SLEEP_MS);
        if (!isDemoActivityInPip()) {
            throw new IllegalStateException("PIP playback required for this test");
        }

        // Long press HOME key to move PIP to Recents
        mDPadHelper.longPressKeyCode(KEYCODE_HOME);
        SystemClock.sleep(LONG_SLEEP_MS);
        Assert.assertTrue("The PIP should be shown in Recents and focused",
                mPipHelper.isPipStateRecentsFocused(PACKAGE_TVLEANBACK, ACTIVITY_PLAYBACKOVERLAY));
    }

    /**
     * Objective: Able to control media playback by media keys (play/pause)
     */
    @Test
    public void testPipPlaybackStateByMediaKeys() {
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        SystemClock.sleep(SHORT_SLEEP_MS);

        // Press the PAUSE key
        mDPadHelper.pressKeyCode(KEYCODE_MEDIA_PAUSE);
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue("The PAUSE key should pause PIP playback",
                mPipHelper.getPlaybackState(PACKAGE_TVLEANBACK) == PlaybackState.STATE_PAUSED);

        // Press the PLAY key
        mDPadHelper.pressKeyCode(KEYCODE_MEDIA_PLAY);
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue("The PLAY key should resume PIP playback",
                mPipHelper.getPlaybackState(PACKAGE_TVLEANBACK) == PlaybackState.STATE_PLAYING);
    }

    /**
     * Objective: Verify that the PIP controls is functional in Recents.
     * - Able to control media playback by UI buttons in Recents
     * - Able to send PIP window to the full screen in Recents
     * - Able to close PIP window in Recents
     */
    @Test
    public void testPipControlsInRecents() {
        // Start PIP and move it to Recents
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue(isDemoActivityInPip());
        mDPadHelper.longPressKeyCode(KEYCODE_HOME);
        SystemClock.sleep(SHORT_SLEEP_MS);

        // Control media playback by UI buttons in Recents. Pause first.
        mPipHelper.togglePipMediaControls();
        Assert.assertTrue(
                mPipHelper.getPlaybackState(PACKAGE_TVLEANBACK) == PlaybackState.STATE_PAUSED);
        SystemClock.sleep(SHORT_SLEEP_MS);
        mPipHelper.togglePipMediaControls();
        Assert.assertTrue(
                mPipHelper.getPlaybackState(PACKAGE_TVLEANBACK) == PlaybackState.STATE_PLAYING);
        SystemClock.sleep(SHORT_SLEEP_MS);

        // Move PIP to full screen
        mPipHelper.selectPipToFullScreenButton();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue("The activity should be in full screen", isDemoActivityInFullScreen());

        // Going back to PIP in Recents
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        Assert.assertTrue(isDemoActivityInPip());
        SystemClock.sleep(SHORT_SLEEP_MS);
        mDPadHelper.longPressKeyCode(KEYCODE_HOME);
        Assert.assertTrue(isDemoActivityInPip());

        // Close PIP in Recents
        mPipHelper.selectPipCloseButton();
        Assert.assertFalse("The PIP should be closed", isDemoActivityInPip());
    }

    /**
     * Objective: Verify that Recents is functional with PIP on screen.
     * - Select/Deselect PIP window by going up/down
     * - Dismiss an app in Recents
     * - Select an app in Recents
     */
    @Test
    public void testRecentsBehaviorWithPipOn() {
        // Clear all in Recents
        mRecentsHelper.open(SHORT_TIMEOUT_MS);
        mRecentsHelper.clearAll();
        mRecentsHelper.exit();

        // Open two apps - Play store, YouTube
        final String APP_NAME_PLAYSTORE = "Play Store";
        final String PACKAGE_PLAYSTORE = "com.android.vending";
        mLauncherStrategy.open();
        mLauncherStrategy.launch(APP_NAME_PLAYSTORE, PACKAGE_PLAYSTORE);
        mLauncherStrategy.launch(mYouTubeHelper.getLauncherName(), mYouTubeHelper.getPackage());

        // Open PIP in Recents
        mLauncherStrategy.open();
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue(isDemoActivityInPip());
        mDPadHelper.longPressKeyCode(KEYCODE_HOME);
        SystemClock.sleep(SHORT_SLEEP_MS);

        // Going up and down between PIP and Recents
        mDPadHelper.pressDPad(Direction.DOWN);
        Assert.assertTrue(
                mPipHelper.isPipStateRecents(PACKAGE_TVLEANBACK, ACTIVITY_PLAYBACKOVERLAY));
        mDPadHelper.pressDPad(Direction.UP);
        Assert.assertTrue(
                mPipHelper.isPipStateRecentsFocused(PACKAGE_TVLEANBACK, ACTIVITY_PLAYBACKOVERLAY));

        // Focus on tasks in Recents, move left and dismiss the app - Play store
        mDPadHelper.pressDPad(Direction.DOWN);
        mDPadHelper.pressDPad(Direction.LEFT);
        mRecentsHelper.dismissTask();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertEquals("The task should be gone after dismissed in Recents",
                mRecentsHelper.getTaskCountOnScreen(), 2);

        // Open another app - YouTube
        mDPadHelper.pressDPadCenter();
        SystemClock.sleep(SHORT_SLEEP_MS);
        // Verify that both YouTube is open and PIP overlay is presented.
        Assert.assertTrue(mYouTubeHelper.isAppInForeground());
        Assert.assertTrue(isDemoActivityInPip());
    }

    /**
     * Objective: Able to send PIP window to the full screen from Now Playing card.
     */
    @Test
    public void testPipToFullScreenOnNowPlaying() {
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        SystemClock.sleep(SHORT_SLEEP_MS);
        if (!isDemoActivityInPip()) {
            throw new IllegalStateException("PIP playback required for this test");
        }

        // Back to Home screen, find the leftmost Now Playing card
        mLauncherStrategy.open();
        final int MAX_ATTEMPTS = 5;
        // Note that Now Playing card seems to make it hard for UiAutomator to detect idle state.
        // It takes about a minute to complete this test.
        mDPadHelper.pressDPad(Direction.LEFT, MAX_ATTEMPTS);
        mDPadHelper.pressDPadCenter();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue("The activity should be in full screen", isDemoActivityInFullScreen());
    }

    /**
     * Objective: Verify that PIP playback won't be interrupted by global search.
     */
    @Test
    public void testVoiceSearchWithPipOn() {
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        SystemClock.sleep(SHORT_SLEEP_MS);
        if (!isDemoActivityInPip()) {
            throw new IllegalStateException("PIP playback required for this test");
        }

        // Launch TV search app with a query
        mSearchHelper.launchActivityAndQuery(mSearchHelper.KEYBOARD_SEARCH, "android tv");
        // Ensure that PIP video keeps playing
        Assert.assertTrue("The PIP video should keep playing.",
                mPipHelper.getPlaybackState(PACKAGE_TVLEANBACK) == PlaybackState.STATE_PLAYING);
    }

    /**
     * Objective: Verify that the Settings moves PIP to the left of the side panel.
     */
    @Test
    public void testMovePipToSettingsBound() {
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        SystemClock.sleep(SHORT_SLEEP_MS);
        if (!isDemoActivityInPip()) {
            throw new IllegalStateException("PIP playback required for this test");
        }

        // Open Settings
        mSettingsHelper.open();
        SystemClock.sleep(SHORT_SLEEP_MS);
        Assert.assertTrue("The PIP should be shown on the left of Settings",
                mPipHelper.isPipStateSettings(PACKAGE_TVLEANBACK, ACTIVITY_PLAYBACKOVERLAY));
    }

    /**
     * Objective: Verify that the video playback from other apps dismisses the PIP playback.
     */
    @Test
    public void testPlayOtherVideoWhilePipPlaying() {
        startPipPlayback("Google+", "Instant Upload");
        mLeanbackDemoHelper.openMediaControlsAndClickPipButton();
        SystemClock.sleep(SHORT_SLEEP_MS);
        if (!isDemoActivityInPip()) {
            throw new IllegalStateException("PIP playback is required for this test");
        }

        // Play other video from YouTube for 5 seconds
        // Do not use open() since UiAutomator is likely to be stuck to find UI elements while
        // playing PIP on the background.
        mYouTubeHelper.launchActivity();
        if (!mYouTubeHelper.waitForOpen(SHORT_SLEEP_MS)) {
            throw new IllegalStateException("YouTube should be open for this test");
        }
        mYouTubeHelper.openHome();
        final int PLAYBACK_DURATION_MS = 5000;
        mYouTubeHelper.playFocusedVideo(PLAYBACK_DURATION_MS);

        // Verify that PIP playback stopped when another video starts
        int playbackState = mPipHelper.getPlaybackState(PACKAGE_TVLEANBACK);
        Assert.assertTrue("The PIP should be either stopped or gone when another video starts.",
                playbackState == PlaybackState.STATE_STOPPED ||
                playbackState == PlaybackState.STATE_NONE);
        mYouTubeHelper.exit();
    }

    /**
     * Objective: Verify that the onboarding screen appears on the first launch of PIP window.
     */
    @Ignore("This requires 'setprop debug.tv.pip_force_onboarding true'")
    @Test
    public void testPipOnboardSeenOnFirstLaunch() {
        // TODO
    }

    private void startPipPlayback(String sectionName, String videoName) {
        mLeanbackDemoHelper.open();
        mLeanbackDemoHelper.selectVideoInRowContent(sectionName, videoName);
        mLeanbackDemoHelper.selectWatchTrailer();
    }

    private void stopPipPlayback() {
        // Close the current playback in PIP
        mPipHelper.executeCommandPipToFullscreen(PACKAGE_TVLEANBACK, ACTIVITY_PLAYBACKOVERLAY,
                false);
        // Press the BACK key to stop the playback
        final int MAX_DEPTH = 2;
        for (int i = 0; i < MAX_DEPTH; ++i) {
            SystemClock.sleep(SHORT_SLEEP_MS);
            mDevice.pressBack();
        }
    }

    private boolean isDemoActivityInPip() {
        return mPipHelper.isPipOnScreen(ACTIVITY_PLAYBACKOVERLAY);
    }

    private boolean isDemoActivityInFullScreen() {
        return mPipHelper.isInFullscreen(ACTIVITY_PLAYBACKOVERLAY);
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

