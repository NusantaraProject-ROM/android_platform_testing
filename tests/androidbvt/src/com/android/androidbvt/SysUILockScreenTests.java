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

package com.android.androidbvt;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.RemoteException;
import android.platform.test.annotations.HermeticTest;
import android.platform.test.helpers.GoogleCameraHelperImpl;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.accessibility.AccessibilityWindowInfo;
import java.io.File;
import java.io.IOException;
import java.util.List;
import junit.framework.Assert;
import junit.framework.TestCase;

@HermeticTest
public class SysUILockScreenTests extends TestCase {
    private static final String EDIT_TEXT_CLASS_NAME = "android.widget.EditText";
    private static final int SHORT_TIMEOUT = 200;
    private static final int LONG_TIMEOUT = 2000;
    private static final int PIN = 1234;
    private static final String PASSWORD = "aaaa";
    private AndroidBvtHelper mABvtHelper = null;
    private UiDevice mDevice = null;
    private Context mContext;
    private boolean mIsMr1Device = false;
    private GoogleCameraHelperImpl mCameraHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.freezeRotation();
        mContext = InstrumentationRegistry.getTargetContext();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext,
                InstrumentationRegistry.getInstrumentation().getUiAutomation());
        mDevice.wakeUp();
        mDevice.pressHome();
        mIsMr1Device = mABvtHelper.isNexusExperienceDevice();
        mCameraHelper = new GoogleCameraHelperImpl(InstrumentationRegistry.getInstrumentation());
    }

    @Override
    public void tearDown() throws Exception {
        mDevice.pressHome();
        mDevice.pressMenu();
        mDevice.unfreezeRotation();
        mDevice.waitForIdle();
        super.tearDown();
    }

    /**
     * Following test will add PIN for Lock Screen, and remove PIN
     * @throws InterruptedException, IOException, RemoteException
     */
    @LargeTest
    public void testLockScreenPIN() throws InterruptedException, IOException, RemoteException {
        setScreenLock(Integer.toString(PIN), "PIN");
        sleepAndWakeUpDevice();
        unlockScreen(Integer.toString(PIN));
        removeScreenLock(Integer.toString(PIN));
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        Assert.assertFalse("Lock Screen is still enabled", isLockScreenEnabled());
    }

    /**
     * Following test will add password for Lock Screen, and remove Password
     * @throws InterruptedException, IOException, RemoteException
     */
    @LargeTest
    public void testLockScreenPwd() throws InterruptedException, IOException, RemoteException {
        setScreenLock(PASSWORD, "Password");
        sleepAndWakeUpDevice();
        unlockScreen(PASSWORD);
        removeScreenLock(PASSWORD);
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        Assert.assertFalse("Lock Screen is still enabled", isLockScreenEnabled());
    }

    /**
     * Following test will add password for Lock Screen, check Emergency Call Page existence, and
     * remove password for Lock Screen
     * @throws InterruptedException, IOException, RemoteException
     */
    @LargeTest
    public void testEmergencyCall() throws InterruptedException, IOException, RemoteException {
        if (!mABvtHelper.isTablet()) {
            setScreenLock(PASSWORD, "Password");
            sleepAndWakeUpDevice();
            checkEmergencyCall();
            unlockScreen(PASSWORD);
            removeScreenLock(PASSWORD);
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            Assert.assertFalse("Lock Screen is still enabled", isLockScreenEnabled());
        }
    }

    /**
     * Just lock the screen and slide up to unlock
     * @throws InterruptedException, IOException, RemoteException
     */
    @LargeTest
    public void testSlideUnlock() throws InterruptedException, IOException, RemoteException {
        sleepAndWakeUpDevice();
        mDevice.wait(Until.findObject(
                By.res(mABvtHelper.SYSTEMUI_PACKAGE, "notification_stack_scroller")), 2000)
                .swipe(Direction.UP, 1.0f);
        int counter = 6;
        Thread.sleep(LONG_TIMEOUT);
        UiObject2 workspace = mDevice
                .wait(Until.findObject(By.clazz("com.android.launcher3.Workspace")), LONG_TIMEOUT);
        assertNotNull("Workspace wasn't found", workspace);
    }

    /**
     * Verify Camera can be launched on LockScreen
     * @throws InterruptedException, IOException, RemoteException
     */
    public void testLaunchCameraOnLockScreen()
            throws InterruptedException, IOException, RemoteException {
        setScreenLock(Integer.toString(PIN), "PIN");
        sleepAndWakeUpDevice();
        try {
            launchCameraOnLockScreen();
        } finally {
            mDevice.pressHome();
            mDevice.waitForIdle();
            unlockScreen(Integer.toString(PIN));
            removeScreenLock(Integer.toString(PIN));
        }
    }

    /**
     * Test photo can be captured on lockscreen
     * @throws InterruptedException, IOException, RemoteException
     */
    public void testCapturePhotoOnLockScreen()
            throws InterruptedException, IOException, RemoteException {
        setScreenLock(Integer.toString(PIN), "PIN");
        sleepAndWakeUpDevice();
        try {
            int prevPhotoCount = getPhotoVideoCount("jpg");
            launchCameraOnLockScreen();
            mCameraHelper.goToCameraMode();
            mCameraHelper.capturePhoto();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
            assertTrue("", (prevPhotoCount + 1) == getPhotoVideoCount("jpg"));
        } finally {
            mDevice.pressHome();
            mDevice.waitForIdle();
            unlockScreen(Integer.toString(PIN));
            removeScreenLock(Integer.toString(PIN));
        }
    }

    /**
     * Test video can be recorded on lockscreen
     * @throws InterruptedException, IOException, RemoteException
     */
    public void testCaptureVideoOnLockScreen()
            throws InterruptedException, IOException, RemoteException {
        setScreenLock(Integer.toString(PIN), "PIN");
        sleepAndWakeUpDevice();
        try {
            int prevVideoCount = getPhotoVideoCount("mp4");
            launchCameraOnLockScreen();
            mCameraHelper.goToVideoMode();
            // Capture video for time equal to LONG_TIMEOUT
            mCameraHelper.captureVideo((long) mABvtHelper.LONG_TIMEOUT);
            Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
            assertTrue("", (prevVideoCount + 1) == getPhotoVideoCount("mp4"));
        } finally {
            mDevice.pressHome();
            mDevice.waitForIdle();
            unlockScreen(Integer.toString(PIN));
            removeScreenLock(Integer.toString(PIN));
        }
    }

    /**
     * Test only photos taken from lock screen are visible to user, not all photos
     * @throws InterruptedException, IOException, RemoteException
     */
    public void testPhotosTakenOnLockscreenOnlyVisible()
            throws InterruptedException, IOException, RemoteException {
        populatePhotoInDCIM();
        setScreenLock(Integer.toString(PIN), "PIN");
        sleepAndWakeUpDevice();
        try {
            launchCameraOnLockScreen();
            mCameraHelper.goToCameraMode();
            mCameraHelper.capturePhoto();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
            // Find Photo/Video viewer in bottom control panel and click to view photo taken
            mDevice.wait(
                    Until.findObject(By.res(mABvtHelper.CAMERA2_PACKAGE, "rounded_thumbnail_view")),
                    mABvtHelper.LONG_TIMEOUT).click();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            // Ensure image view loaded and image detail icon is present
            assertTrue("Photos detail icon isn't found", mDevice.wait(Until.hasObject(
                    By.res(mABvtHelper.CAMERA2_PACKAGE, "filmstrip_bottom_control_details")),
                    mABvtHelper.LONG_TIMEOUT));

            swipePhotoVideoLeft();
            // As only photos taken in lock screen are visible
            // After swiping left there shouldn't be any photo
            // Hence, Image_Detail icon should be absent
            assertFalse("Photos taken from lockscreen can't be viewed",
                    mDevice.wait(Until.hasObject(
                            By.res(mABvtHelper.CAMERA2_PACKAGE,
                                    "filmstrip_bottom_control_details")),
                            mABvtHelper.LONG_TIMEOUT));
        } finally {
            mDevice.pressHome();
            mDevice.waitForIdle();
            unlockScreen(Integer.toString(PIN));
            removeScreenLock(Integer.toString(PIN));
        }
    }

    /**
     * Test only videoss taken from lock screen are visible to user, not all videos
     * @throws InterruptedException, IOException, RemoteException
     */
    public void testVideoTakenOnLockscreenOnlyVisible()
            throws InterruptedException, IOException, RemoteException {
        populatePhotoInDCIM();
        setScreenLock(Integer.toString(PIN), "PIN");
        sleepAndWakeUpDevice();
        try {
            launchCameraOnLockScreen();
            mCameraHelper.goToVideoMode();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            // Capture video for time equal to LONG_TIMEOUT
            mCameraHelper.captureVideo((long) mABvtHelper.LONG_TIMEOUT);
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            mDevice.wait(
                    Until.findObject(By.res(mABvtHelper.CAMERA2_PACKAGE, "rounded_thumbnail_view")),
                    mABvtHelper.LONG_TIMEOUT).click();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            // Ensure video_play_button is present
            assertTrue("Video taken from lockscreen can't be viewed",
                    mDevice.wait(
                            Until.hasObject(By.res(mABvtHelper.CAMERA2_PACKAGE, "play_button")),
                            mABvtHelper.LONG_TIMEOUT));
            swipePhotoVideoLeft();
            // As only videos taken in lock screen are visible
            // After swiping left there shouldn't be any video
            // Hence, video_play_button should be absent
            assertFalse("",
                    mDevice.wait(
                            Until.hasObject(By.res(mABvtHelper.CAMERA2_PACKAGE, "play_button")),
                            mABvtHelper.LONG_TIMEOUT));
        } finally {
            mDevice.pressHome();
            mDevice.waitForIdle();
            unlockScreen(Integer.toString(PIN));
            removeScreenLock(Integer.toString(PIN));
        }
    }

    /*
     * Tap on lock icon on Lockscreenc camera prompts for lock screen After successful unlock,camera
     * opens in Camera Mode
     * @throws InterruptedException, IOException, RemoteException
     */
    public void testLockIconCameraOpensCameraAfterUnlock()
            throws InterruptedException, IOException, RemoteException {
        setScreenLock(Integer.toString(PIN), "PIN");
        sleepAndWakeUpDevice();
        try {
            launchCameraOnLockScreen();
            mCameraHelper.goToCameraMode();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            mDevice.wait(
                    Until.findObject(By.res(mABvtHelper.CAMERA2_PACKAGE, "rounded_thumbnail_view")),
                    mABvtHelper.LONG_TIMEOUT).click();
            mDevice.wait(
                    Until.hasObject(By.res("com.android.systemui:id/keyguard_security_container")),
                    mABvtHelper.LONG_TIMEOUT);
            unlockScreen(Integer.toString(PIN));
            Thread.sleep(mABvtHelper.LONG_TIMEOUT);
            List<AccessibilityWindowInfo> windows = InstrumentationRegistry.getInstrumentation()
                    .getUiAutomation().getWindows();
            AccessibilityWindowInfo window = windows.get(1);
            assertTrue("Camera isn't active window",
                    window.getRoot().getPackageName().equals(mABvtHelper.CAMERA_PACKAGE));

            removeScreenLock(Integer.toString(PIN));
        } finally {
            mDevice.pressHome();
            mDevice.waitForIdle();
            if (isLockScreenEnabled()) {
                unlockScreen(Integer.toString(PIN));
                removeScreenLock(Integer.toString(PIN));
            }
        }
    }

    private void launchCameraOnLockScreen() {
        int w = mDevice.getDisplayWidth();
        int h = mDevice.getDisplayHeight();
        // Load camera on LockScreen and take a photo
        mDevice.drag((w - 25), (h - 25), (int) (w * 0.5), (int) (w * 0.5), 40);
        mDevice.waitForIdle();
        assertTrue("Camera isn't lauched on lockScreen", mDevice.wait(Until.hasObject(
                By.res(mABvtHelper.CAMERA2_PACKAGE, "activity_root_view")),
                mABvtHelper.LONG_TIMEOUT));
    }

    private int getPhotoVideoCount(String ext) {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File cameraFolder = new File(String.format("%s/Camera", path));
        File[] files = cameraFolder.listFiles();
        int count = 0;
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(String.format("%s", ext))) {
                    count++;
                }
            }
        }
        return count;
    }

    private void swipePhotoVideoLeft() throws InterruptedException {
        // Swipe the image left to view next one, if there is any
        int w = mDevice.getDisplayWidth();
        int h = mDevice.getDisplayHeight();
        mDevice.drag((int) (w * 0.9), (int) (h * 0.5), (int) (w * 0.1), (int) (w * 0.5), 50);
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);

    }

    private void populatePhotoInDCIM() throws InterruptedException {
        // Ensure that DCIM folder has either a photo/video
        if (getPhotoVideoCount("jpg") == 0) {
            mCameraHelper.open();
            mCameraHelper.dismissInitialDialogs();
            mCameraHelper.capturePhoto();
            Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
            assertTrue("DCIM dir doesn't have any photo/video", getPhotoVideoCount("jpg") > 0);
        }
    }

    /**
     * Sets the screen lock pin or password
     * @param pwd text of Password or Pin for lockscreen
     * @param mode indicate if its password or PIN
     */
    private void setScreenLock(String pwd, String mode) throws InterruptedException {
        navigateToScreenLock();
        mDevice.wait(Until.findObject(By.text(mode)), mABvtHelper.LONG_TIMEOUT).click();
        // set up Secure start-up page
        if (!mIsMr1Device) {
            mDevice.wait(Until.findObject(By.text("No thanks")), mABvtHelper.LONG_TIMEOUT).click();
        }
        UiObject2 pinField = mDevice.wait(Until.findObject(By.clazz(EDIT_TEXT_CLASS_NAME)),
                mABvtHelper.LONG_TIMEOUT);
        pinField.setText(pwd);
        // enter and verify password
        mDevice.pressEnter();
        pinField.setText(pwd);
        mDevice.pressEnter();
        mDevice.wait(Until.findObject(By.text("DONE")), mABvtHelper.LONG_TIMEOUT).click();
    }

    /**
     * check if Emergency Call page exists
     */
    private void checkEmergencyCall() throws InterruptedException {
        mDevice.pressMenu();
        mDevice.wait(Until.findObject(By.text("EMERGENCY")), mABvtHelper.LONG_TIMEOUT).click();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        UiObject2 dialButton = mDevice.wait(Until.findObject(By.desc("dial")),
                mABvtHelper.LONG_TIMEOUT);
        Assert.assertNotNull("Can't reach emergency call page", dialButton);
        mDevice.pressBack();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
    }

    private void removeScreenLock(String pwd) throws InterruptedException {
        navigateToScreenLock();
        UiObject2 pinField = mDevice.wait(Until.findObject(By.clazz(EDIT_TEXT_CLASS_NAME)),
                mABvtHelper.LONG_TIMEOUT);
        pinField.setText(pwd);
        mDevice.pressEnter();
        mDevice.wait(Until.findObject(By.text("Swipe")), mABvtHelper.LONG_TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("YES, REMOVE")), mABvtHelper.LONG_TIMEOUT).click();
    }

    private void unlockScreen(String pwd) throws InterruptedException, IOException {
        swipeUp();
        Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
        // enter password to unlock screen
        String command = String.format(" %s %s %s", "input", "text", pwd);
        mDevice.executeShellCommand(command);
        mDevice.waitForIdle();
        Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
        mDevice.pressEnter();
    }

    private void navigateToScreenLock() throws InterruptedException {
        launchSettingsPage(mContext, Settings.ACTION_SECURITY_SETTINGS);
        mDevice.wait(Until.findObject(By.text("Screen lock")), mABvtHelper.LONG_TIMEOUT).click();
    }

    private void launchSettingsPage(Context ctx, String pageName) throws InterruptedException {
        Intent intent = new Intent(pageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
        Thread.sleep(mABvtHelper.LONG_TIMEOUT * 2);
    }

    private void sleepAndWakeUpDevice() throws RemoteException, InterruptedException {
        mDevice.sleep();
        Thread.sleep(mABvtHelper.LONG_TIMEOUT);
        mDevice.wakeUp();
    }

    private void swipeUp() throws InterruptedException {
        mDevice.swipe(mDevice.getDisplayWidth() / 2, mDevice.getDisplayHeight(),
                mDevice.getDisplayWidth() / 2, 0, 30);
        Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
    }

    private boolean isLockScreenEnabled() {
        KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        return km.isKeyguardSecure();
    }
}