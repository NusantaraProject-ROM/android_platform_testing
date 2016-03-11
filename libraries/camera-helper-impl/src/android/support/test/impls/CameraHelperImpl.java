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

package com.android.support.test.helpers;

import android.app.Instrumentation;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;

import junit.framework.Assert;

public class CameraHelperImpl extends AbstractCameraHelper {
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
    private static final String UI_SHUTTER_BUTTON_ID = "shutter_button";
    private static final String UI_SETTINGS_BUTTON_ID = "settings_button";
    private static final String UI_MENU_BUTTON_ID = "menuButton";

    private static final String LOG_TAG = CameraHelperImpl.class.getSimpleName();

    private static final long APP_INIT_WAIT = 10000;
    private static final long SHUTTER_WAIT_TIME = 10000;
    private static final long MENU_WAIT_TIME = 5000;

    private boolean mIsVersion3X = false;

    public CameraHelperImpl(Instrumentation instr) {
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
            boolean firstMessage = mDevice.wait(Until.hasObject(
                By.text("Swipe right to left for video")), APP_INIT_WAIT);
            if (firstMessage) {
                // Swipe left to dismiss 'how to open video message'
                UiObject2 activityView = mDevice.findObject(
                        By.res(UI_PACKAGE_NAME, "activity_root_view"));
                if (activityView != null) {
                    activityView.swipe(Direction.LEFT, 1.0f);
                }
            } else {
                Log.e(LOG_TAG, "Timed out waiting for the first message. Continuing anyway.");
            }

            // Confirm 'GOT IT' for action above
            UiObject2 thanks = mDevice.wait(Until.findObject(By.text("GOT IT")), 5000);
            if (thanks != null) {
                thanks.click();
            }
        } else {
            BySelector confirm = By.res(UI_PACKAGE_NAME, "confirm_button");
            UiObject2 location = mDevice.wait(Until.findObject(
                    By.copy(confirm).text("NEXT")), 5000);
            if (location != null) {
                location.click();
            }
            // Choose sensor size. It's okay to timeout. These dialog screens might not exist..
            UiObject2 sensor = mDevice.wait(Until.findObject(
                    By.copy(confirm).text("OK, GOT IT")), 5000);
            if (sensor != null) {
                sensor.click();
            }
        }

        // Dismiss dogfood dialog
        if (mDevice.wait(Until.hasObject(
                By.res(UI_PACKAGE_NAME, "internal_release_dialog_title")), 5000)) {
            mDevice.findObject(By.res(UI_PACKAGE_NAME, "ok_button")).click();
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
        waitForCameraShutter();
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
        waitForVideoShutter();
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
            UiObject2 toggle = getToggleButton();
            if (toggle != null) {
                toggle.click();
            }
        } else {
            openMenu();
            selectMenuItem("Camera");
        }

        waitForCameraShutter();
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
            UiObject2 toggle = getToggleButton();
            if (toggle != null) {
                toggle.click();
            }
        } else {
            openMenu();
            selectMenuItem("Video");
        }

        waitForVideoShutter();
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
            backFrontSwitch();
        } else {
            // Open mode options if not open. Note: the mode option button only appear if mode option menu not open
            UiObject2 modeoptions = getModeOptionToggleButton();
            if (modeoptions != null) {
                modeoptions.click();
            }
            // Press back camera button
            backFrontSwitch();
        }
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
            backFrontSwitch();
        } else {
            // Open mode options if not open. Note: the mode option button only appear if mode option menu not open
            UiObject2 modeoptions = getModeOptionToggleButton();
            if (modeoptions != null) {
                modeoptions.click();
            }
            // Press front camera button
            backFrontSwitch();
        }
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

    private boolean isCameraMode() {
        return (mDevice.hasObject(By.res(UI_PACKAGE_NAME, "progress_overlay")));
    }

    private boolean isVideoMode() {
        return (mDevice.hasObject(By.res(UI_PACKAGE_NAME, "recording_time_rect")));
    }

    private boolean isBackCamera() {
        // Close menu if open
        closeMenu();

        if (mIsVersion3X) {
            return (mDevice.hasObject(By.desc("Back camera")));
        } else {
            // Open mode options if not open
            UiObject2 modeoptions = getModeOptionToggleButton();
            if (modeoptions != null) {
                modeoptions.click();
            }
            return (mDevice.hasObject(By.desc("Back camera")));
        }
    }

    private boolean isFrontCamera() {
        // Close menu if open
        closeMenu();

        if (mIsVersion3X) {
            return (mDevice.hasObject(By.desc("Front camera")));
        } else {
            // Open mode options if not open
            UiObject2 modeoptions = getModeOptionToggleButton();
            if (modeoptions != null) {
                modeoptions.click();
            }
            return (mDevice.hasObject(By.desc("Front camera")));
        }
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
            UiObject2 shutter = mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_SHUTTER_BUTTON_ID));
            if (shutter != null) {
                shutter.click();
            }
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

    private boolean isRecording() {
        return mDevice.hasObject(By.res(UI_PACKAGE_NAME, UI_RECORDING_TIME_ID));
    }

    private UiObject2 getToggleButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_TOGGLE_BUTTON_ID));
    }

    private void backFrontSwitch() {
        UiObject2 toggle = getBackFrontToggleButton();
        if (toggle != null) {
            toggle.click();
        } else {
            Assert.assertNotNull("Failed to detect a toggle bar", toggle);
        }
    }

    private UiObject2 getBackFrontToggleButton() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_BACK_FRONT_TOGGLE_BUTTON_ID));
    }

    private UiObject2 getModeOptionToggleButton() {
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

    private void waitForCameraShutter() {
        if (mIsVersion3X) {
            mDevice.wait(Until.hasObject(By.desc(UI_SHUTTER_DESC_CAM_3X).enabled(true)),
                    SHUTTER_WAIT_TIME);
        } else {
            mDevice.wait(Until.hasObject(By.desc(UI_SHUTTER_DESC_CAM_2X).enabled(true)),
                    SHUTTER_WAIT_TIME);
        }
    }


    private void waitForVideoShutter() {
        if (mIsVersion3X) {
            mDevice.wait(Until.hasObject(By.desc(UI_SHUTTER_DESC_VID_3X).enabled(true)),
                    SHUTTER_WAIT_TIME);
        } else {
            mDevice.wait(Until.hasObject(By.desc(UI_SHUTTER_DESC_VID_2X).enabled(true)),
                    SHUTTER_WAIT_TIME);
        }
    }
}
