/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.platform.test.helpers;

import android.app.Instrumentation;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Configurator;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiWatcher;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import junit.framework.Assert;

public class GoogleCameraHelperImpl extends AbstractGoogleCameraHelper {
    private static final String LOG_TAG = GoogleCameraHelperImpl.class.getSimpleName();
    private static final String UI_ACTIVITY_VIEW_ID = "activity_root_view";
    private static final String UI_MENU_ID = "menuButton";
    private static final String UI_PACKAGE_NAME = "com.android.camera2";
    private static final String UI_RECORDING_TIME_ID = "recording_time";
    private static final String UI_SHUTTER_DESC_CAM_3X = "Capture photo";
    private static final String UI_SHUTTER_DESC_CAM_2X = "Shutter";
    private static final String UI_SHUTTER_DESC_VID_3X = "Capture video";
    private static final String UI_SHUTTER_DESC_VID_2X = "Shutter";
    private static final String UI_TOGGLE_BUTTON_ID = "photo_video_paginator";
    private static final String UI_BACK_FRONT_TOGGLE_BUTTON_ID = "camera_toggle_button";
    private static final String UI_MODE_OPTION_TOGGLE_BUTTON_ID = "mode_options_toggle";
    private static final String UI_SHUTTER_BUTTON_ID_3X = "photo_video_button";
    private static final String UI_SHUTTER_BUTTON_ID_2X = "shutter_button";
    private static final String UI_SETTINGS_BUTTON_ID = "settings_button";
    private static final String UI_MENU_BUTTON_ID = "menuButton";

    private static final String DESC_HDR_AUTO = "HDR Plus auto";
    private static final String DESC_HDR_OFF_3X = "HDR Plus off";
    private static final String DESC_HDR_ON_3X = "HDR Plus on";

    private static final String DESC_HDR_OFF_2X = "HDR off";
    private static final String DESC_HDR_ON_2X = "HDR on";

    private static final String TEXT_4K_ON = "UHD 4K";
    private static final String TEXT_HD_1080 = "HD 1080p";
    private static final String TEXT_HD_720 = "HD 720p";

    public static final int HDR_MODE_AUTO = -1;
    public static final int HDR_MODE_OFF = 0;
    public static final int HDR_MODE_ON = 1;

    public static final int VIDEO_4K_MODE_ON = 1;
    public static final int VIDEO_HD_1080 = 0;
    public static final int VIDEO_HD_720 = -1;

    private static final long APP_INIT_WAIT = 20000;
    private static final long DIALOG_TRANSITION_WAIT = 5000;
    private static final long SHUTTER_WAIT_TIME = 20000;
    private static final long SWITCH_WAIT_TIME = 5000;
    private static final long MENU_WAIT_TIME = 5000;

    private boolean mIsVersionH = false;
    private boolean mIsVersionI = false;
    private boolean mIsVersionJ = false;

    public GoogleCameraHelperImpl(Instrumentation instr) {
        super(instr);

        try {
            mIsVersionH = getVersion().startsWith("2.");
            mIsVersionI = getVersion().startsWith("3.0") || getVersion().startsWith("3.1");
            mIsVersionJ = getVersion().startsWith("3.2");
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, String.format("Unable to find package by name, %s", getPackage()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        super.open();
        waitForAppInit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return "com.google.android.GoogleCamera";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "Camera";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
        if (mIsVersionI || mIsVersionJ) {
            // Dismiss dogfood confidentiality dialog
            Pattern okText = Pattern.compile("OK, GOT IT", Pattern.CASE_INSENSITIVE);
            UiObject2 dogfoodMessage = mDevice.wait(
                    Until.findObject(By.text(okText)), APP_INIT_WAIT);
            if (dogfoodMessage != null) {
                dogfoodMessage.click();
            }
            // Swipe left to dismiss 'how to open video message'
            UiObject2 activityView = mDevice.wait(Until.findObject(
                    By.res(UI_PACKAGE_NAME, "activity_root_view")), DIALOG_TRANSITION_WAIT);
            if (activityView != null) {
                activityView.swipe(Direction.LEFT, 1.0f);
            }
            // Confirm 'GOT IT' for action above
            UiObject2 thanks = mDevice.wait(Until.findObject(By.text("GOT IT")),
                    DIALOG_TRANSITION_WAIT);
            if (thanks != null) {
                thanks.click();
            }
        } else {
            BySelector confirm = By.res(UI_PACKAGE_NAME, "confirm_button");
            UiObject2 location = mDevice.wait(Until.findObject(
                    By.copy(confirm).text("NEXT")), APP_INIT_WAIT);
            if (location != null) {
                location.click();
            }
            // Choose sensor size. It's okay to timeout. These dialog screens might not exist..
            UiObject2 sensor = mDevice.wait(Until.findObject(
                    By.copy(confirm).text("OK, GOT IT")), DIALOG_TRANSITION_WAIT);
            if (sensor != null) {
                sensor.click();
            }
            // Dismiss dogfood dialog
            if (mDevice.wait(Until.hasObject(
                    By.res(UI_PACKAGE_NAME, "internal_release_dialog_title")), 5000)) {
                mDevice.findObject(By.res(UI_PACKAGE_NAME, "ok_button")).click();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void capturePhoto() {
        if (!isCameraMode()) {
            Assert.fail("GoogleCamera must be in Camera mode to capture photos.");
        }

        getCameraShutter().click();
        waitForCameraShutterEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void captureVideo(long timeInMs) {
        if (!isVideoMode()) {
            Assert.fail("GoogleCamera must be in Video mode to record videos.");
        }

        if (isRecording()) {
            return;
        }

        // Temporary hack #1: Make UI code responsive by shortening the UiAutomator idle timeout.
        // The pulsing record button broadcasts unnecessary events of TYPE_WINDOW_CONTENT_CHANGED,
        // but we intend to have a fix and remove this hack with Kenai (GC 3.0).
        long original = Configurator.getInstance().getWaitForIdleTimeout();
        Configurator.getInstance().setWaitForIdleTimeout(1000);

        try {
            getVideoShutter().click();
            SystemClock.sleep(timeInMs);
            getVideoShutter().click();
            waitForVideoShutterEnabled();
        } finally {
            Configurator.getInstance().setWaitForIdleTimeout(original);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void snapshotVideo(long videoTimeInMs, long snapshotStartTimeInMs) {
        if (!isVideoMode()) {
            Assert.fail("GoogleCamera must be in Video mode to record videos.");
        } else if (videoTimeInMs <= snapshotStartTimeInMs) {
            Assert.fail("video recording time length must be larger than snapshot start time");
        }

        // Temporary hack #2: Make UI code responsive by shortening the UiAutomator idle timeout.
        // The pulsing record button broadcasts unnecessary events of TYPE_WINDOW_CONTENT_CHANGED,
        // but we intend to have a fix and remove this hack with Kenai (GC 3.0).
        long original = Configurator.getInstance().getWaitForIdleTimeout();
        Configurator.getInstance().setWaitForIdleTimeout(1000);

        if (isRecording()) {
            return;
        }

        try {
            getVideoShutter().click();
            SystemClock.sleep(snapshotStartTimeInMs);

            boolean snapshot_success = false;

            // Take a snapshot
            if (mIsVersionJ) {
                UiObject2 snapshotButton = mDevice.findObject(By.res(UI_PACKAGE_NAME, "snapshot_button"));
                if (snapshotButton != null) {
                    snapshotButton.click();
                    snapshot_success = true;
                }
            } else if (mIsVersionI) {
                // Ivvavik Version of GCA doesn't support snapshot
                snapshot_success = false;
            } else {
                UiObject2 snapshotButton = mDevice.findObject(By.res(UI_PACKAGE_NAME, "recording_time"));
                if (snapshotButton != null) {
                    snapshotButton.click();
                    snapshot_success = true;
                }
            }

            if (!snapshot_success) {
                getVideoShutter().click();
                waitForVideoShutterEnabled();
                Assert.fail("snapshot button not found!");
                return;
            }

            SystemClock.sleep(videoTimeInMs - snapshotStartTimeInMs);
            getVideoShutter().click();
            waitForVideoShutterEnabled();
        } finally {
            Configurator.getInstance().setWaitForIdleTimeout(original);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToCameraMode() {
        if (isCameraMode()) {
            return;
        }

        if (mIsVersionI || mIsVersionJ) {
            UiObject2 toggle = getCameraVideoToggleButton();
            if (toggle != null) {
                toggle.click();
            }
        } else {
            openMenu();
            selectMenuItem("Camera");
        }

        mDevice.waitForIdle();
        waitForCameraShutterEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToVideoMode() {
        if (isVideoMode()) {
            return;
        }

        if (mIsVersionI || mIsVersionJ) {
            UiObject2 toggle = getCameraVideoToggleButton();
            if (toggle != null) {
                toggle.click();
            }
        } else {
            openMenu();
            selectMenuItem("Video");
        }

        mDevice.waitForIdle();
        waitForVideoShutterEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToBackCamera() {
        if (isBackCamera()) {
            return;
        }

        // Close menu if open
        closeMenu();

        if (mIsVersionI || mIsVersionJ) {
            pressBackFrontToggleButton();
        } else {
            // Open mode options if not open.
            // Note: the mode option button only appear if mode option menu not open
            UiObject2 modeoptions = getModeOptionsMenuButton();
            if (modeoptions != null) {
                modeoptions.click();
            }
            pressBackFrontToggleButton();
        }

        // Wait for ensuring back camera button enabled
        waitForBackEnabled();

        // Wait for ensuring shutter button enabled
        waitForCurrentShutterEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToFrontCamera() {
        if (isFrontCamera()) {
            return;
        }

        // Close menu if open
        closeMenu();

        if (mIsVersionI || mIsVersionJ) {
            pressBackFrontToggleButton();
        } else {
            // Open mode options if not open.
            // Note: the mode option button only appear if mode option menu not open
            UiObject2 modeoptions = getModeOptionsMenuButton();
            if (modeoptions != null) {
                modeoptions.click();
            }
            pressBackFrontToggleButton();
        }

        // Wait for ensuring front camera button enabled
        waitForFrontEnabled();

        // Wait for ensuring shutter button enabled
        waitForCurrentShutterEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public void setHdrMode(int mode) {
        if (!isCameraMode()) {
            throw new IllegalStateException("Cannot set HDR unless in camera mode.");
        }

        if (mIsVersionI || mIsVersionJ) {
            if (getHdrToggleButton() == null) {
                if (mode == HDR_MODE_OFF) {
                    return;
                } else {
                    throw new UnsupportedOperationException(
                            "Cannot set HDR on this device as requested.");
                }
            }

            for (int retries = 0; retries < 3; retries++) {
                if (!isHdrMode(mode)) {
                    getHdrToggleButton().click();
                    mDevice.waitForIdle();
                } else {
                    Log.e(LOG_TAG, "Successfully set HDR mode!");
                    mDevice.waitForIdle();
                    return;
                }
            }
        } else {
            // Open mode options before checking Hdr status
            openModeOptions2X();
            if (getHdrToggleButton() == null) {
                if (mode == HDR_MODE_OFF) {
                    return;
                } else {
                    throw new UnsupportedOperationException(
                            "Cannot set HDR on this device as requested.");
                }
            }

            for (int retries = 0; retries < 3; retries++) {
                if (!isHdrMode(mode)) {
                    getHdrToggleButton().click();
                    mDevice.waitForIdle();
                } else {
                    Log.e(LOG_TAG, "Successfully set HDR mode!");
                    mDevice.waitForIdle();
                    return;
                }
            }
        }
    }

    private boolean isHdrMode(int mode) {
        if (mIsVersionI || mIsVersionJ) {
            String modeDesc = getHdrToggleButton().getContentDescription();
            if (DESC_HDR_AUTO.equals(modeDesc)) {
                return HDR_MODE_AUTO == mode;
            } else if (DESC_HDR_OFF_3X.equals(modeDesc)) {
                return HDR_MODE_OFF == mode;
            } else if (DESC_HDR_ON_3X.equals(modeDesc)) {
                return HDR_MODE_ON == mode;
            } else {
                Assert.fail("Unexpected failure.");
            }
        } else {
            // Open mode options before checking Hdr status
            openModeOptions2X();
            // Check the HDR mode
            String modeDesc = getHdrToggleButton().getContentDescription();
            if (DESC_HDR_OFF_2X.equals(modeDesc)) {
                return HDR_MODE_OFF == mode;
            } else if (DESC_HDR_ON_2X.equals(modeDesc)) {
                return HDR_MODE_ON == mode;
            } else {
                Assert.fail("Unexpected failure.");
            }
        }

        return HDR_MODE_OFF == mode;
    }

    /**
     * {@inheritDoc}
     */
    public void set4KMode(int mode) {
        // If the menu is not open, open it
        if (!isMenuOpen()) {
            openMenu();
        }

        if (mIsVersionI || mIsVersionJ) {
            // Select Menu Item "Settings"
            selectMenuItem("Settings");
        } else {
            // Select Menu Item "Settings"
            selectSetting2X();
        }

        // Select Item "Resolution & Quality"
        selectSettingItem("Resolution & quality");
        // Select Item "Back camera video", which is the only mode supports 4k
        selectVideoResolution(mode);
        // Quit Menu "Resolution & Quality"
        closeSettingItem();
        // Close Main Menu
        closeMenuItem();
    }

    private void openModeOptions2X() {
        // If the mode option is already open, return as it is
        if (mDevice.hasObject(By.res(UI_PACKAGE_NAME, "mode_options_buttons"))) {
            return;
        }
        // Before openning the mode option, close the menu if the menu is open
        closeMenu();
        waitForVideoShutterEnabled();
        // Open the mode options to check HDR mode
        UiObject2 modeoptions = getModeOptionsMenuButton();
        if (modeoptions != null) {
            modeoptions.click();
        } else {
            Assert.fail("Fail to find modeoption button when trying to check HDR mode");
        }
    }

    private void openMenu() {
        if (mIsVersionI || mIsVersionJ) {
            UiObject2 menu = mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_MENU_ID));
            menu.click();
        } else {
            UiObject2 activityView = mDevice.wait(Until.findObject(
                    By.res(UI_PACKAGE_NAME, UI_ACTIVITY_VIEW_ID)), MENU_WAIT_TIME);
            activityView.swipe(Direction.RIGHT, 1.0f);
        }

        mDevice.wait(Until.hasObject(By.text("Photo Sphere")), MENU_WAIT_TIME);

        mDevice.waitForIdle();
    }

    private void selectMenuItem(String mode) {
        UiObject2 menuItem = mDevice.findObject(By.text(mode));
        if (menuItem != null) {
            menuItem.click();
        } else {
            Assert.fail(String.format("Menu item button was not enabled with %d seconds",
                    (int)Math.floor(MENU_WAIT_TIME / 1000)));
        }
        mDevice.wait(Until.gone(By.text("Photo Sphere")), MENU_WAIT_TIME);

        mDevice.waitForIdle();
    }

    private void closeMenuItem() {
        UiObject2 navUp = mDevice.findObject(By.desc("Navigate up"));
        if (navUp != null) {
            navUp.click();
        } else {
            Assert.fail(String.format("Navigation up button was not enabled with %d seconds",
                    (int)Math.floor(MENU_WAIT_TIME / 1000)));
        }
        mDevice.wait(Until.gone(By.text("Help & feedback")), MENU_WAIT_TIME);

        mDevice.waitForIdle();
    }

    private void selectSettingItem(String mode) {
        UiObject2 settingItem = mDevice.findObject(By.text(mode));
        if (settingItem != null) {
            settingItem.click();
        } else {
            Assert.fail(String.format("Setting item button was not enabled with %d seconds",
                    (int)Math.floor(MENU_WAIT_TIME / 1000)));
        }
        mDevice.wait(Until.gone(By.text("Help & feedback")), MENU_WAIT_TIME);

        mDevice.waitForIdle();
    }

    private void selectSetting2X() {
        UiObject2 settingItem = mDevice.findObject(By.desc("Settings"));
        if (settingItem != null) {
            settingItem.click();
        } else {
            Assert.fail(String.format("Setting item button was not enabled with %d seconds",
                    (int)Math.floor(MENU_WAIT_TIME / 1000)));
        }
        mDevice.wait(Until.gone(By.text("Help & feedback")), MENU_WAIT_TIME);

        mDevice.waitForIdle();
    }

    private void closeSettingItem() {
        UiObject2 navUp = mDevice.findObject(By.desc("Navigate up"));
        if (navUp != null) {
            navUp.click();
        } else {
            Assert.fail(String.format("Navigation up button was not enabled with %d seconds",
                    (int)Math.floor(MENU_WAIT_TIME / 1000)));
        }
        mDevice.wait(Until.findObject(By.text("Help & feedback")), MENU_WAIT_TIME);

        mDevice.waitForIdle();
    }

    private void selectVideoResolution(int mode) {
        UiObject2 backCamera = mDevice.findObject(By.text("Back camera video"));
        if (backCamera != null) {
            backCamera.click();
        } else {
            Assert.fail(String.format("Back camera button was not enabled with %d seconds",
                    (int)Math.floor(MENU_WAIT_TIME / 1000)));
        }
        mDevice.wait(Until.findObject(By.text("CANCEL")), MENU_WAIT_TIME);
        mDevice.waitForIdle();

        if (mode == VIDEO_4K_MODE_ON) {
            mDevice.wait(Until.findObject(By.text(TEXT_4K_ON)), MENU_WAIT_TIME).click();
        } else if (mode == VIDEO_HD_1080) {
            mDevice.wait(Until.findObject(By.text(TEXT_HD_1080)), MENU_WAIT_TIME).click();
        } else if (mode == VIDEO_HD_720){
            mDevice.wait(Until.findObject(By.text(TEXT_HD_720)), MENU_WAIT_TIME).click();
        } else {
            Assert.fail("Failed to set video resolution");
        }

        mDevice.wait(Until.gone(By.text("CANCEL")), MENU_WAIT_TIME);

        mDevice.waitForIdle();
    }

    private void closeMenu() {
        // Should only call this function when menu is open, do nothing if menu is not open
        if (!isMenuOpen()) {
            return;
        }

        if (mIsVersionI || mIsVersionJ) {
            // Click menu button to close menu (this is NOT for taking pictures)
            UiObject2 backButton = mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_MENU_BUTTON_ID));
            if (backButton != null) {
                backButton.click();
            }
        } else {
            // Click shutter button to close menu (this is NOT for taking pictures)
            UiObject2 shutter = mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_SHUTTER_BUTTON_ID_2X));
            if (shutter != null) {
                shutter.click();
            }
        }
    }

    private boolean isCameraMode() {
        if (mIsVersionI || mIsVersionJ) {
            return (mDevice.hasObject(By.desc(UI_SHUTTER_DESC_CAM_3X)));
        } else {
            // TODO: identify a Haleakala UiObject2 unique Camera mode
            return !isVideoMode();
        }
    }

    private boolean isVideoMode() {
        if (mIsVersionI || mIsVersionJ) {
            return (mDevice.hasObject(By.desc(UI_SHUTTER_DESC_VID_3X)));
        } else {
            return (mDevice.hasObject(By.res(UI_PACKAGE_NAME, "recording_time_rect")));
        }
    }

    private boolean isRecording() {
        return mDevice.hasObject(By.res(UI_PACKAGE_NAME, UI_RECORDING_TIME_ID));
    }

    private boolean isFrontCamera() {
        // Close menu if open
        closeMenu();

        if (mIsVersionJ) {
            return (mDevice.hasObject(By.desc("Switch to back camera")));
        } else if (mIsVersionI) {
            return (mDevice.hasObject(By.desc("Front camera")));
        } else {
            // Open mode options if not open
            UiObject2 modeoptions = getModeOptionsMenuButton();
            if (modeoptions != null) {
                modeoptions.click();
            }
            return (mDevice.hasObject(By.desc("Front camera")));
        }
    }

    private boolean isBackCamera() {
        // Close menu if open
        closeMenu();

        if (mIsVersionJ) {
            return (mDevice.hasObject(By.desc("Switch to front camera")));
        } else if (mIsVersionI) {
            return (mDevice.hasObject(By.desc("Back camera")));
        } else {
            // Open mode options if not open
            UiObject2 modeoptions = getModeOptionsMenuButton();
            if (modeoptions != null) {
                modeoptions.click();
            }
            return (mDevice.hasObject(By.desc("Back camera")));
        }
    }

    private boolean isMenuOpen() {
        if (mIsVersionI || mIsVersionJ) {
            if (mDevice.hasObject(By.desc("Open settings"))) {
                return true;
            }
        } else {
            if (mDevice.hasObject(By.res(UI_PACKAGE_NAME, UI_SETTINGS_BUTTON_ID))) {
                return true;
            }
        }
        return false;
    }

    private void pressBackFrontToggleButton() {
        UiObject2 toggle = getBackFrontToggleButton();
        if (toggle != null) {
            toggle.click();
        } else {
            Assert.fail("Failed to detect a back-front toggle button");
        }
    }

    private UiObject2 getCameraVideoToggleButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_TOGGLE_BUTTON_ID));
    }

    private UiObject2 getBackFrontToggleButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_BACK_FRONT_TOGGLE_BUTTON_ID));
    }

    private UiObject2 getHdrToggleButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, "hdr_plus_toggle_button"));
    }

    private UiObject2 getModeOptionsMenuButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_MODE_OPTION_TOGGLE_BUTTON_ID));
    }

    private UiObject2 getCameraShutter() {
        if (mIsVersionJ) {
            return mDevice.findObject(By.desc(UI_SHUTTER_DESC_CAM_3X).enabled(true));
        } else {
            return mDevice.findObject(By.desc(UI_SHUTTER_DESC_CAM_2X).enabled(true));
        }
    }

    private UiObject2 getVideoShutter() {
        if (mIsVersionJ) {
            return mDevice.findObject(By.desc(UI_SHUTTER_DESC_VID_3X).enabled(true));
        } else {
            return mDevice.findObject(By.desc(UI_SHUTTER_DESC_VID_2X).enabled(true));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void waitForCameraShutterEnabled() {
        boolean uiSuccess = false;

        if (mIsVersionI || mIsVersionJ) {
            uiSuccess = mDevice.wait(Until.hasObject(
                    By.desc(UI_SHUTTER_DESC_CAM_3X).enabled(true)), SHUTTER_WAIT_TIME);
        } else {
            uiSuccess = mDevice.wait(Until.hasObject(
                    By.desc(UI_SHUTTER_DESC_CAM_2X).enabled(true)), SHUTTER_WAIT_TIME);
        }

        if (!uiSuccess) {
            Assert.fail(String.format("Camera shutter was not enabled with %d seconds",
                    (int)Math.floor(SHUTTER_WAIT_TIME / 1000)));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void waitForVideoShutterEnabled() {
        boolean uiSuccess = false;

        if (mIsVersionI || mIsVersionJ) {
            uiSuccess = mDevice.wait(Until.hasObject(
                    By.desc(UI_SHUTTER_DESC_VID_3X).enabled(true)), SHUTTER_WAIT_TIME);
        } else {
            uiSuccess = mDevice.wait(Until.hasObject(
                    By.desc(UI_SHUTTER_DESC_VID_2X).enabled(true)), SHUTTER_WAIT_TIME);
        }

        if (!uiSuccess) {
            Assert.fail(String.format("Video shutter was not enabled with %d seconds",
                    (int)Math.floor(SHUTTER_WAIT_TIME / 1000)));
        }
    }

    private void waitForCurrentShutterEnabled() {
        // This function is called to wait for shutter button enabled in either camera or video mode
        if (mIsVersionI || mIsVersionJ) {
            mDevice.wait(Until.hasObject(By.res(UI_PACKAGE_NAME, UI_SHUTTER_BUTTON_ID_3X).enabled(true)),
                    SHUTTER_WAIT_TIME);
        } else {
            mDevice.wait(Until.hasObject(By.res(UI_PACKAGE_NAME, UI_SHUTTER_BUTTON_ID_2X).enabled(true)),
                    SHUTTER_WAIT_TIME);
        }
    }

    private void waitForBackEnabled() {
        if (mIsVersionI || mIsVersionJ) {
            mDevice.wait(Until.hasObject(By.desc("Switch to front camera").enabled(true)),
                    SWITCH_WAIT_TIME);
        } else {
            mDevice.wait(Until.hasObject(By.desc("Back camera").enabled(true)),
                    SWITCH_WAIT_TIME);
        }
    }

    private void waitForFrontEnabled() {
        if (mIsVersionI || mIsVersionJ) {
            mDevice.wait(Until.hasObject(By.desc("Switch to back camera").enabled(true)),
                    SWITCH_WAIT_TIME);
        } else {
            mDevice.wait(Until.hasObject(By.desc("Front camera").enabled(true)),
                    SWITCH_WAIT_TIME);
        }
    }

    private void waitForAppInit() {
        boolean initalized = false;
        if (mIsVersionI || mIsVersionJ) {
            initalized = mDevice.wait(Until.hasObject(By.res(UI_PACKAGE_NAME, UI_MENU_BUTTON_ID)),
                    APP_INIT_WAIT);
        } else {
            initalized = mDevice.wait(Until.hasObject(By.res(UI_PACKAGE_NAME, UI_MODE_OPTION_TOGGLE_BUTTON_ID)),
                    APP_INIT_WAIT);
        }

        waitForCurrentShutterEnabled();

        mDevice.waitForIdle();

        if (initalized) {
            Log.e(LOG_TAG, "Successfully initialized.");
        } else {
            Log.e(LOG_TAG, "Failed to find initialization indicator.");
        }
    }

    /**
     * TODO: Temporary. Create long-term solution for registering watchers.
     */
    public void registerCrashWatcher() {
        final UiDevice fDevice = mDevice;

        mDevice.registerWatcher("GoogleCamera-crash-watcher", new UiWatcher() {
            @Override
            public boolean checkForCondition() {
                Pattern dismissWords =
                        Pattern.compile("DISMISS", Pattern.CASE_INSENSITIVE);
                UiObject2 buttonDismiss = fDevice.findObject(By.text(dismissWords).enabled(true));
                if (buttonDismiss != null) {
                    buttonDismiss.click();
                    Assert.fail("Camera crash dialog encountered. Failing test.");
                }

                return false;
            }
        });
    }

    /**
     * TODO: Temporary. Create long-term solution for registering watchers.
     */
    public void unregisterCrashWatcher() {
        mDevice.removeWatcher("GoogleCamera-crash-watcher");
    }

    /**
     * TODO: Should only be temporary
     * {@inheritDoc}
     */
    public String openWithShutterTimeString() {
        String pkg = getPackage();
        String id = getLauncherName();

        long launchStart = ILauncherStrategy.LAUNCH_FAILED_TIMESTAMP;
        if (!mDevice.hasObject(By.pkg(pkg).depth(0))) {
            launchStart = mLauncherStrategy.launch(id, pkg);
        }

        if (launchStart == ILauncherStrategy.LAUNCH_FAILED_TIMESTAMP) {
            Assert.fail("Failed to launch GoogleCamera.");
        }

        waitForAppInit();
        waitForCurrentShutterEnabled();
        long launchDuration = SystemClock.uptimeMillis() - launchStart;

        Date dateNow = new Date();
        DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        String dateString = dateFormat.format(dateNow);

        if (isCameraMode()) {
            return String.format("%s %s %d\n", dateString, "camera", launchDuration);
        } else if (isVideoMode()) {
            return String.format("%s %s %d\n", dateString, "video", launchDuration);
        } else {
            return String.format("%s %s %d\n", dateString, "wtf", launchDuration);
        }
    }
}
