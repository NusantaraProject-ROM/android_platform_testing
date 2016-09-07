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

package android.platform.test.helpers.tv;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.platform.test.helpers.AbstractLeanbackAppHelper;
import android.platform.test.helpers.exceptions.UiTimeoutException;
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

public class LeanbackDemoHelperImpl extends AbstractLeanbackAppHelper {

    private static final String TAG = LeanbackDemoHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.example.android.tvleanback";
    private static final String ACTIVITY_MAIN = "com.example.android.tvleanback.ui.MainActivity";
    private static final String RES_MAIN_ACTIVITY_ID = "main_frame";
    private static final long SHORT_SLEEP_MS = 5000;    // 5 seconds
    private static final long LONG_SLEEP_MS = 30000;    // 30 seconds

    private static final String TEXT_TOOLTIP = "Hold HOME to control PIP";


    public LeanbackDemoHelperImpl(Instrumentation instrumentation) {
        super(instrumentation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return UI_PACKAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "Videos by Google";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BySelector getMainActivitySelector() {
        return By.res(UI_PACKAGE, RES_MAIN_ACTIVITY_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BySelector getBrowseRowsSelector() {
        return By.focused(true).hasChild(By.res(UI_PACKAGE, "main_image"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        launchActivity();
        // Wait until the main activity is open.
        mDevice.wait(Until.hasObject(getMainActivitySelector()), SHORT_SLEEP_MS);
    }

    /**
     * Setup expectation: None
     *
     * Launches the demo main activity with an Intent.
     */
    private void launchActivity() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setComponent(new ComponentName(UI_PACKAGE, ACTIVITY_MAIN));
        // Launch the activity
        mInstrumentation.getContext().startActivity(intent);
    }

    /**
     * Setup expectation: On the main activity.
     * <p>
     * Selects the desired video in the row content and wait for it to open the details view.
     * </p>
     * @param sectionName the name of section that includes the desired video
     * @param videoName the name of video to select
     */
    public void selectVideoInRowContent(String sectionName, String videoName) {
        returnToMainActivity();
        openHeader(sectionName);
        UiObject2 container = getRowContent(sectionName);
        BySelector target = By.focused(true).hasDescendant(
                By.res(UI_PACKAGE, "title_text").text(videoName), 3);
        UiObject2 video = select(container, target, Direction.RIGHT);
        if (video == null) {
            throw new UnknownUiException(
                    String.format("The video %s not found in the %s section", videoName,
                            sectionName));
        }
        mDPadHelper.pressDPadCenter();
        mDevice.waitForIdle();
    }

    /**
     * Setup expectation: On the details view.
     * <p>
     * Selects the button of "WATCH TRAILER FREE".
     * </p>
     */
    public void selectWatchTrailer() {
        BySelector target = By.res(UI_PACKAGE, "lb_action_button").text("WATCH TRAILER\nFREE");
        UiObject2 trailer = mDevice.wait(Until.findObject(target), SHORT_SLEEP_MS);
        if (trailer == null) {
            throw new UnknownUiException("The watch trailer button not found");
        }
        mDPadHelper.pressDPadCenter();
        mDevice.waitForIdle();
    }

    /**
     * Setup expectation: On the media control card.
     *
     * @return a boolean of whether the media control card has a PIP button enabled.
     */
    public boolean hasPipButton() {
        // Pressing the key up brings up the media control card
        mDPadHelper.pressDPad(Direction.UP);
        if (!mDevice.wait(Until.hasObject(By.res(UI_PACKAGE, "controls_card")), SHORT_SLEEP_MS)) {
            throw new UiTimeoutException("No media control card is found");
        }
        return mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE, "button").desc("Enter Picture In Picture Mode")),
                SHORT_SLEEP_MS);
    }

    /**
     * Setup expectation: PIP window is being open.
     *
     * @return a boolean of whether the tooltip text is shown.
     */
    public boolean hasTooltipShown() {
        return mDevice.wait(Until.hasObject(By.text(TEXT_TOOLTIP)), SHORT_SLEEP_MS);
    }

    /**
     * Setup expectation: While playing a video in fullscreen.
     * <p>
     * Clicks on the PIP button and wait for it to be gone.
     * </p>
     */
    public void openMediaControlsAndClickPipButton() {
        // Pressing the key up brings up the media control card
        mDPadHelper.pressDPad(Direction.UP);
        if (!mDevice.wait(Until.hasObject(By.res(UI_PACKAGE, "controls_card")), SHORT_SLEEP_MS)) {
            throw new UiTimeoutException("No media control card is found");
        }
        BySelector target = By.res(UI_PACKAGE, "button").desc("Enter Picture In Picture Mode");
        UiObject2 pipButton = mDevice.wait(Until.findObject(target), SHORT_SLEEP_MS);
        if (pipButton == null) {
            throw new UiTimeoutException("PIP button not found");
        }
        pipButton.click();
        mDevice.waitForIdle();
        mDPadHelper.pressDPadCenter();
        mDevice.waitForIdle();
    }

    /**
     * Attempts to return to main activity with getMainActivitySelector()
     * by pressing the back button repeatedly and sleeping briefly to allow for UI slowness.
     */
    public void returnToMainActivity() {
        int maxBackAttempts = 10;
        BySelector selector = getMainActivitySelector();
        if (selector == null) {
            throw new IllegalStateException("getMainActivitySelector() should be overridden.");
        }
        while (!mDevice.wait(Until.hasObject(selector), SHORT_SLEEP_MS)
                && maxBackAttempts-- > 0) {
            mDevice.pressBack();
        }
    }

    private UiObject2 getRowContent(String rowName) {
        return mDevice.wait(Until.findObject(By.res(UI_PACKAGE, "row_content").desc(rowName)),
                LONG_SLEEP_MS);
    }
}

