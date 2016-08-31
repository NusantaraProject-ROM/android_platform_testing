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

package android.platform.test.helpers.tv;

import static android.view.KeyEvent.KEYCODE_WINDOW;

import android.app.Instrumentation;
import android.graphics.Rect;
import android.platform.test.helpers.DPadHelper;
import android.platform.test.helpers.CommandHelper;
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * App helper implementation class for TV's picture-in-picture in System UI
 */
public class SysUiPipHelperImpl {
    private static final String LOG_TAG = SysUiPipHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.android.systemui";
    private static final int FULLSCREEN_WORKSPACE_STACK_ID = 1;
    private static final int PINNED_STACK_ID = 4;   // ID of stack for a PIP window

    private static final int INVALID_TASK_ID = -1;
    private static final String ACTIVITY_PIPOVERLAY =
            "com.android.systemui.tv.pip.PipOverlayActivity";
    // Bounds [left top right bottom] on screen for picture-in-picture (PIP) windows
    private static final Rect PIP_MENU_BOUNDS = new Rect(596, 280, 1324, 690);
    private static final Rect PIP_RECENTS_BOUNDS = new Rect(800, 54, 1120, 234);
    private static final Rect PIP_RECENTS_FOCUSED_BOUNDS = new Rect(775, 54, 1145, 262);
    private static final Rect PIP_SETTINGS_BOUNDS = new Rect(662, 54, 1142, 324);

    private static final long SHORT_SLEEP_MS = 3000;    // 3 seconds

    private DPadHelper mDPadHelper;
    private CommandHelper mCmdHelper;
    private UiDevice mDevice;


    public SysUiPipHelperImpl(Instrumentation instrumentation) {
        mDPadHelper = DPadHelper.getInstance(instrumentation);
        mCmdHelper = new CommandHelper(instrumentation);
        mDevice = UiDevice.getInstance(instrumentation);
    }

    /**
     * @return the package name for this helper's application.
     */
    public String getPackage() {
        return UI_PACKAGE;
    }

    /**
     * Setup expectations: None
     * <p>
     * Checks if a PIP window is shown on screen.
     * </p>
     * @param activityName the activity of the application that implements PIP playback
     */
    public boolean isPipOnScreen(String activityName) {
        String output = mCmdHelper.executeAmStackInfo(PINNED_STACK_ID);
        Log.i(LOG_TAG, "isPipOnScreen: " + output);
        if (null == output || "null".equalsIgnoreCase(output)) {
            Log.i(LOG_TAG, "No PIP window is found");
            return false;
        }
        // Note that ACTIVITY_PIPOVERLAY would disappear in seconds, afterwards the app playback
        // overlay comes in. It's just safe to think of that whatever activity in pinned stack is
        // PIP window.
        return output.contains(activityName);
    }

    /**
     * Setup expectations: None
     * <p>
     * Checks if a PIP window is shown on screen.
     * </p>
     * @param activityName the activity of the application that implements PIP playback
     */
    public boolean isInFullscreen(String activityName) {
        String output = mCmdHelper.executeAmStackInfo(FULLSCREEN_WORKSPACE_STACK_ID);
        Log.i(LOG_TAG, "isInFullscreen: " + output);
        if (null == output || "null".equalsIgnoreCase(output)) {
            Log.i(LOG_TAG, "Activity is not found in full screen: " + activityName);
            return false;
        }
        return output.contains(activityName);
    }

    /**
     * Setup expectations: None
     * <p>
     * Returns the bounds on screen for a PIP window.
     * </p>
     */
    private Rect getPipBounds(String packageName, String activityName) {
        return getBounds(PINNED_STACK_ID, packageName, activityName);
    }

    private Rect getBounds(int stackId, String packageName, String activityName) {
        // Format:
        // taskId=216: com.example.android.tvleanback/com.example.android.tvleanback.ui.PlaybackOverlayActivity bounds=[775,54][1145,262] ...
        final Pattern BOUNDS_REGEX = Pattern.compile(
                String.format("taskId=\\d+: %s/%s bounds=\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]",
                        packageName, activityName));
        String output = mCmdHelper.executeAmStackInfo(stackId);
        Log.d(LOG_TAG, "getBounds output=" + output);
        Matcher matcher = BOUNDS_REGEX.matcher(output);
        if (matcher.find()) {
            int left = Integer.parseInt(matcher.group(1));
            int top = Integer.parseInt(matcher.group(2));
            int right = Integer.parseInt(matcher.group(3));
            int bottom = Integer.parseInt(matcher.group(4));
            Log.i(LOG_TAG, String.format("Bounds found: [%d,%d][%d,%d] for %s/%s",
                    left, top, right, bottom, packageName, activityName));
            return new Rect(left, top, right, bottom);
        }
        Log.w(LOG_TAG, "getBounds returns null");
        return null;
    }

    /**
     * Setup expectations: PIP is open.
     * <p>
     * Moves the PIP window to full screen.
     * </p>
     */
    public void executeCommandPipToFullscreen(String packageName, String activityName,
            boolean throwIfFail) {
        int taskId = getTaskId(packageName, activityName);
        if (taskId != INVALID_TASK_ID) {
            mCmdHelper.executeAmStackMovetask(taskId,
                    FULLSCREEN_WORKSPACE_STACK_ID);
        }
        if (throwIfFail && isPipOnScreen(activityName)) {
            throw new UnknownUiException("Failed to move a PIP window to fullscreen");
        }
    }

    /**
     * Get the current playback state for a given package that owns the media session.
     * @param packageName the package name of media session owner
     * @return
     * 0 = PlaybackState.STATE_NONE
     * 1 = PlaybackState.STATE_STOPPED
     * 2 = PlaybackState.STATE_PAUSED
     * 3 = PlaybackState.STATE_PLAYING
     */
    public int getPlaybackState(String packageName) {
        String output = mCmdHelper.executeDumpsysMediaSession();
        // Parse the output of dumpsys media_session.
        // Example :
        // LeanbackSampleApp com.example.android.tvleanback/LeanbackSampleApp
        //   package=com.example.android.tvleanback
        //   ...
        //   state=PlaybackState {state=3, position=0, buffered position=0, speed=1.0, updated=...}
        int playbackState = 0;
        int index = output.indexOf(String.format("package=%s", packageName));
        if (index == -1) {
            Log.w(LOG_TAG, String.format("No media session found for the package: %s", packageName));
            return playbackState;
        }
        final Pattern PLAYBACKSTATE_REGEX = Pattern.compile(
                "\\s*state=PlaybackState \\{state=(\\d+),.*");
        Matcher matcher = PLAYBACKSTATE_REGEX.matcher(output.substring(index));
        if (matcher.find()) {
            playbackState = Integer.parseInt(matcher.group(1));
            Log.i(LOG_TAG, String.format("PlaybackState=%s package=%s", playbackState, packageName));
        }
        return playbackState;
    }

    /**
     * Setup expectation: None. Check if PIP overlay is shown and focused.
     */
    private boolean isPipStateOverlay() {
        // TODO
        throw new UnsupportedOperationException("This method is not yet implemented.");
    }

    /**
     * Setup expectation: None. Check if PIP menu is shown in center.
     */
    public boolean isPipStateMenu(String packageName, String activityName) {
        return PIP_MENU_BOUNDS.equals(getPipBounds(packageName, activityName));
    }

    /**
     * Setup expectation: None. Check if the PIP is shown in Recents with focus.
     */
    public boolean isPipStateRecentsFocused(String packageName, String activityName) {
        return PIP_RECENTS_FOCUSED_BOUNDS.equals(getPipBounds(packageName, activityName));
    }

    /**
     * Setup expectation: None. Check if the PIP is shown with Settings.
     */
    public boolean isPipStateSettings(String packageName, String activityName) {
        return PIP_SETTINGS_BOUNDS.equals(getPipBounds(packageName, activityName));
    }

    /**
     * Setup expectation: When the PIP is shown in Recents with focus.
     * <p>
     * Toggles the media play/pause button on screen.
     * </p>
     */
    public void togglePipMediaControls() {
        UiObject2 pause = mDevice.findObject(By.res(UI_PACKAGE, "button").desc("Pause"));
        UiObject2 play = mDevice.findObject(By.res(UI_PACKAGE, "button").desc("Play"));
        if (pause != null) {
            pause.click();
        } else if (play != null) {
            play.click();
        } else {
            throw new UnknownUiException("No Play/Pause button found in PIP in Recents");
        }
        mDevice.waitForIdle();
        mDevice.pressDPadCenter();
    }

    /**
     * Setup expectation: When the PIP is shown in Recents with focus.
     * <p>
     * Clicks the full screen button on screen.
     * </p>
     */
    public void selectPipToFullScreenButton() {
        UiObject2 button = mDevice.wait(
                Until.findObject(By.res(UI_PACKAGE, "button").desc("Full screen")),
                SHORT_SLEEP_MS);
        button.click();
        mDevice.waitForIdle();
        mDevice.pressDPadCenter();
    }

    /**
     * Setup expectation: When the PIP is shown in Recents with focus.
     * <p>
     * Clicks the Close button on screen.
     * </p>
     */
    public void selectPipCloseButton() {
        UiObject2 button = mDevice.wait(
                Until.findObject(By.res(UI_PACKAGE, "button").desc("Close PIP")),
                SHORT_SLEEP_MS);
        button.click();
        mDevice.waitForIdle();
        mDevice.pressDPadCenter();

    }

    /**
     * Setup expectation: None.
     * <p>
     * Check if the PIP is shown in Recents without focus.
     * </p>
     */
    public boolean isPipStateRecents(String packageName, String activityName) {
        return PIP_RECENTS_BOUNDS.equals(getPipBounds(packageName, activityName));
    }

    private int getTaskId(String packageName, String activityName) {
        int taskId = INVALID_TASK_ID;
        final Pattern TASK_REGEX = Pattern.compile(
                String.format("taskId=(\\d+): %s/%s", packageName, activityName));
        Matcher matcher = TASK_REGEX.matcher(mCmdHelper.executeAmStackList());
        if (matcher.find()) {
            taskId = Integer.parseInt(matcher.group(1));
            Log.i(LOG_TAG, String.format("TaskId found: %d for %s/%s",
                    taskId, packageName, activityName));
        }
        return taskId;
    }
}

