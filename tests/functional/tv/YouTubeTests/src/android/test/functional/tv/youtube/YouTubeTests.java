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

package android.test.functional.tv.youtube;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.platform.test.helpers.tv.YouTubeHelperImpl;
import android.support.test.InstrumentationRegistry;
import android.support.test.launcherhelper.ILeanbackLauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.launcherhelper.LeanbackLauncherStrategy;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Functional verification tests for YouTube on TV.
 *
 * adb shell am instrument -w -r \
 *    -e class android.test.functional.tv.youtube.YouTubeTests \
 *    android.test.functional.tv.youtube/android.support.test.runner.AndroidJUnitRunner
 */
@RunWith(AndroidJUnit4.class)
public class YouTubeTests {

    private static final String TAG = YouTubeTests.class.getSimpleName();
    private static final String DEFAULT_SEARCH_QUERY = "never gonna give you up";
    private static final long DEFAULT_SEARCH_PLAY_DURATION_MS = 30 * 1000;  // 30 seconds

    private UiDevice mDevice;
    private Instrumentation mInstrumentation;
    private Context mContext;
    private Bundle mArguments;

    private LeanbackLauncherStrategy mLauncherStrategy;
    private YouTubeHelperImpl mYouTubeHelper;


    public YouTubeTests() {
        initialize(InstrumentationRegistry.getInstrumentation());
    }

    private void initialize(Instrumentation instrumentation) {
        // Initialize instances of testing support library
        mInstrumentation = instrumentation;
        mContext = getInstrumentation().getContext();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mArguments = InstrumentationRegistry.getArguments();

        // Initialize instances of leanback and app helpers
        ILeanbackLauncherStrategy launcherStrategy = LauncherStrategyFactory.getInstance(
                mDevice).getLeanbackLauncherStrategy();
        if (launcherStrategy instanceof LeanbackLauncherStrategy) {
            mLauncherStrategy = (LeanbackLauncherStrategy) launcherStrategy;
        }
        mYouTubeHelper = new YouTubeHelperImpl(getInstrumentation());
    }

    protected Instrumentation getInstrumentation() {
        return mInstrumentation;
    }

    @Before
    public void setUp() {
        mLauncherStrategy.open();
    }

    @After
    public void tearDown() {
        mYouTubeHelper.exit();
    }


    /**
     * Objective: Able to play the first video on Home section.
     */
    @Test
    public void testPlayVideoAtHome() {
        mYouTubeHelper.open();
        mYouTubeHelper.openHome();

        Log.i(TAG, "found a video: " + mYouTubeHelper.getFocusedVideoTitleText());
        long durationMs = mYouTubeHelper.getFocusedVideoDuration();
        if (durationMs > DEFAULT_SEARCH_PLAY_DURATION_MS) {
            durationMs = DEFAULT_SEARCH_PLAY_DURATION_MS;
        }
        Assert.assertTrue(mYouTubeHelper.playFocusedVideo(durationMs));
    }

    /**
     * Objective: Able to search for videos and play.
     */
    @Test
    public void testSearchVideoAndPlay() {
        // Search for a video
        mYouTubeHelper.open();
        mYouTubeHelper.search(DEFAULT_SEARCH_QUERY);

        long durationMs = mYouTubeHelper.getFocusedVideoDuration();
        if (durationMs > DEFAULT_SEARCH_PLAY_DURATION_MS) {
            durationMs = DEFAULT_SEARCH_PLAY_DURATION_MS;
        }

        // Select the first video in the search results and play
        mYouTubeHelper.openFirstSearchResult();

        // Play the video for a given period of time
        SystemClock.sleep(durationMs);
        Assert.assertTrue(mYouTubeHelper.isInVideoPlayback());
    }

    /**
     * Objective: Able to launch YouTube video in the Notification row
     */
    @Test
    public void testLaunchVideoInNotificationRow() {
        Assert.assertTrue(mLauncherStrategy.launchNotification(mYouTubeHelper.getLauncherName()));

        // Play the video for a given period of time
        SystemClock.sleep(5000);
        Assert.assertTrue(mYouTubeHelper.isInVideoPlayback());
    }
}

