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
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
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
    private static final String DESC_HDR_OFF = "HDR Plus off";
    private static final String DESC_HDR_ON = "HDR Plus on";

    public static final int HDR_MODE_AUTO = -1;
    public static final int HDR_MODE_OFF = 0;
    public static final int HDR_MODE_ON = 1;

    private static final long APP_INIT_WAIT = 20000;
    private static final long DIALOG_TRANSITION_WAIT = 5000;
    private static final long SHUTTER_WAIT_TIME = 20000;
    private static final long SWITCH_WAIT_TIME = 5000;
    private static final long MENU_WAIT_TIME = 5000;

    private boolean mIsVersion3X = false;

    public GoogleCameraHelperImpl(Instrumentation instr) {
        super(instr);

        try {
            mIsVersion3X = getVersion().startsWith("3.");
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
        if (mIsVersion3X) {
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
    public void captureVideo(long timeInMS) {
        if (!isVideoMode()) {
            Assert.fail("GoogleCamera must be in Video mode to capture photos.");
        }

        if (isRecording()) {
            return;
        }

        getVideoShutter().click();
        SystemClock.sleep(timeInMS);
        getVideoShutter().click();
        waitForVideoShutterEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToCameraMode() {
        if (isCameraMode()) {
            return;
        }

        if (mIsVersion3X) {
            UiObject2 toggle = getCameraVideoToggleButton();
            if (toggle != null) {
                toggle.click();
            }
        } else {
            openMenu();
            selectMenuItem("Camera");
        }

        mDevice.waitForIdle();
        checkForDismissButton();
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

        if (mIsVersion3X) {
            UiObject2 toggle = getCameraVideoToggleButton();
            if (toggle != null) {
                toggle.click();
            }
        } else {
            openMenu();
            selectMenuItem("Video");
        }

        mDevice.waitForIdle();
        checkForDismissButton();
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

        if (mIsVersion3X) {
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

        if (mIsVersion3X) {
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

        if (mIsVersion3X) {
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
                    return;
                }
            }
        } else {
            // Temporary no-op. TODO: implement.
        }
    }

    private boolean isHdrMode(int mode) {
        if (mIsVersion3X) {
            String modeDesc = getHdrToggleButton().getContentDescription();
            if (DESC_HDR_AUTO.equals(modeDesc)) {
                return HDR_MODE_AUTO == mode;
            } else if (DESC_HDR_OFF.equals(modeDesc)) {
                return HDR_MODE_OFF == mode;
            } else if (DESC_HDR_ON.equals(modeDesc)) {
                return HDR_MODE_ON == mode;
            } else {
                Assert.fail("Unexpected failure.");
            }
        } else {
            // Temporary no-op. TODO: implement.
        }

        return HDR_MODE_OFF == mode;
    }

    private void openMenu() {
        if (mIsVersion3X) {
            UiObject2 menu = mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_MENU_ID));
            menu.click();
        } else {
            UiObject2 activityView = mDevice.wait(Until.findObject(
                    By.res(UI_PACKAGE_NAME, UI_ACTIVITY_VIEW_ID)), MENU_WAIT_TIME);
            activityView.swipe(Direction.RIGHT, 1.0f);
        }

        mDevice.wait(Until.hasObject(By.text("Photo Sphere")), MENU_WAIT_TIME);
    }

    private void selectMenuItem(String mode) {
        mDevice.wait(Until.findObject(By.text(mode)), 5000).click();
        mDevice.wait(Until.gone(By.text("Photo Sphere")), MENU_WAIT_TIME);
    }

    private void closeMenu() {
        // Should only call this function when menu is open, do nothing if menu is not open
        if (!isMenuOpen()) {
            return;
        }

        if (mIsVersion3X) {
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
        if (mIsVersion3X) {
            return (mDevice.hasObject(By.res(UI_PACKAGE_NAME, "progress_overlay")));
        } else {
            // TODO: identify a Haleakala UiObject2 unique Camera mode
            return !isVideoMode();
        }
    }

    private boolean isVideoMode() {
        return (mDevice.hasObject(By.res(UI_PACKAGE_NAME, "recording_time_rect")));
    }

    private boolean isRecording() {
        return mDevice.hasObject(By.res(UI_PACKAGE_NAME, UI_RECORDING_TIME_ID));
    }

    private boolean isFrontCamera() {
        // Close menu if open
        closeMenu();

        if (mIsVersion3X) {
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

        if (mIsVersion3X) {
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
        if (mIsVersion3X) {
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
        if (mIsVersion3X) {
            return mDevice.findObject(By.res(UI_PACKAGE_NAME, "hdr_plus_toggle_button"));
        } else {
            // Temporary no-op. TODO: implement.
            return null;
        }
    }

    private UiObject2 getModeOptionsMenuButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_MODE_OPTION_TOGGLE_BUTTON_ID));
    }

    private UiObject2 getCameraShutter() {
        if (mIsVersion3X) {
            return mDevice.findObject(By.desc(UI_SHUTTER_DESC_CAM_3X).enabled(true));
        } else {
            return mDevice.findObject(By.desc(UI_SHUTTER_DESC_CAM_2X).enabled(true));
        }
    }

    private UiObject2 getVideoShutter() {
        if (mIsVersion3X) {
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

        if (mIsVersion3X) {
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

        if (mIsVersion3X) {
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
        if (mIsVersion3X) {
            mDevice.wait(Until.hasObject(By.res(UI_PACKAGE_NAME, UI_SHUTTER_BUTTON_ID_3X).enabled(true)),
                    SHUTTER_WAIT_TIME);
        } else {
            mDevice.wait(Until.hasObject(By.res(UI_PACKAGE_NAME, UI_SHUTTER_BUTTON_ID_2X).enabled(true)),
                    SHUTTER_WAIT_TIME);
        }
    }

    private void waitForBackEnabled() {
        mDevice.wait(Until.hasObject(By.desc("Back camera").enabled(true)),
                SWITCH_WAIT_TIME);
    }

    private void waitForFrontEnabled() {
        mDevice.wait(Until.hasObject(By.desc("Front camera").enabled(true)),
                SWITCH_WAIT_TIME);
    }

    private void waitForAppInit() {
        boolean initalized = false;
        if (mIsVersion3X) {
            initalized = mDevice.wait(Until.hasObject(By.res(UI_PACKAGE_NAME, UI_MENU_BUTTON_ID)),
                    APP_INIT_WAIT);
        } else {
            // Temporary no-op. TODO: implement.
        }

        if (initalized) {
            Log.e(LOG_TAG, "Successfully initialized.");
        } else {
            Log.e(LOG_TAG, "Failed to find initialization indicator.");
        }
    }

    private void checkForDismissButton() {
        Pattern dismissWords =
                Pattern.compile("DISMISS", Pattern.CASE_INSENSITIVE);
        UiObject2 buttonDismiss = mDevice.wait(
                Until.findObject(By.text(dismissWords).enabled(true)), 1000);
        if (buttonDismiss != null) {
            buttonDismiss.click();
            Assert.fail("Camera dialog issued; dismissing and continuing.");
        }
    }

    /**
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
