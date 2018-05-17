/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.server.wm.flicker;

import static android.os.SystemClock.sleep;
import static android.view.Surface.rotationToString;

import static org.junit.Assert.assertNotNull;

import android.os.RemoteException;
import android.platform.helpers.IAppHelper;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;
import android.view.Surface;

import com.android.server.wm.flicker.TransitionRunner.TransitionBuilder;

/**
 * Collection of common transitions which can be used to test different apps or scenarios.
 */
class CommonTransitions {

    public static final int ITERATIONS = 1;
    private static final String TAG = "FLICKER";

    private static void setRotation(UiDevice device, int rotation) {
        try {
            switch (rotation) {
                case Surface.ROTATION_270:
                    device.setOrientationLeft();
                    return;

                case Surface.ROTATION_90:
                    device.setOrientationRight();
                    return;

                case Surface.ROTATION_0:
                default:
                    device.setOrientationNatural();
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    static TransitionBuilder openAppWarm(IAppHelper testApp, UiDevice
            device) {
        return TransitionRunner.newBuilder()
                .withTag("OpenAppWarm_" + testApp.getLauncherName())
                .runBeforeAll(AutomationUtils::wakeUpAndGoToHomeScreen)
                .runBeforeAll(testApp::open)
                .runBefore(device::pressHome)
                .runBefore(device::waitForIdle)
                .run(testApp::open)
                .runAfterAll(testApp::exit)
                .runAfterAll(AutomationUtils::setDefaultWait)
                .repeat(ITERATIONS);
    }

    static TransitionBuilder closeAppWithBackKey(IAppHelper testApp, UiDevice
            device) {
        return TransitionRunner.newBuilder()
                .withTag("closeAppWithBackKey_" + testApp.getLauncherName())
                .runBeforeAll(AutomationUtils::wakeUpAndGoToHomeScreen)
                .runBefore(testApp::open)
                .runBefore(device::waitForIdle)
                .run(device::pressBack)
                .run(device::waitForIdle)
                .runAfterAll(testApp::exit)
                .runAfterAll(AutomationUtils::setDefaultWait)
                .repeat(ITERATIONS);
    }

    static TransitionBuilder closeAppWithHomeKey(IAppHelper testApp, UiDevice
            device) {
        return TransitionRunner.newBuilder()
                .withTag("closeAppWithHomeKey_" + testApp.getLauncherName())
                .runBeforeAll(AutomationUtils::wakeUpAndGoToHomeScreen)
                .runBefore(testApp::open)
                .runBefore(device::waitForIdle)
                .run(device::pressHome)
                .run(device::waitForIdle)
                .runAfterAll(testApp::exit)
                .runAfterAll(AutomationUtils::setDefaultWait)
                .repeat(ITERATIONS);
    }

    static TransitionBuilder getOpenAppCold(IAppHelper testApp,
            UiDevice device) {
        return TransitionRunner.newBuilder()
                .withTag("OpenAppCold_" + testApp.getLauncherName())
                .runBeforeAll(AutomationUtils::wakeUpAndGoToHomeScreen)
                .runBefore(device::pressHome)
                .runBefore(testApp::exit)
                .runBefore(device::waitForIdle)
                .run(testApp::open)
                .runAfterAll(testApp::exit)
                .repeat(ITERATIONS);
    }

    static TransitionBuilder changeAppRotation(IAppHelper testApp, UiDevice
            device, int beginRotation, int endRotation) {
        return TransitionRunner.newBuilder()
                .withTag("changeAppRotation_" + testApp.getLauncherName()
                        + rotationToString(beginRotation) + "_" +
                        rotationToString(endRotation))
                .runBeforeAll(AutomationUtils::wakeUpAndGoToHomeScreen)
                .runBeforeAll(testApp::open)
                .runBefore(() -> setRotation(device, beginRotation))
                .runBefore(device::waitForIdle)
                .runBefore(() -> sleep(2000))
                .run(() -> setRotation(device, endRotation))
                .run(device::waitForIdle)
                .run(() -> sleep(3000))
                .runAfterAll(testApp::exit)
                .runAfterAll(() -> setRotation(device, Surface.ROTATION_0))
                .repeat(ITERATIONS);
    }

    static TransitionBuilder appToSplitScreen(IAppHelper testApp, UiDevice device) {
        return TransitionRunner.newBuilder()
                .withTag("appToSplitScreen_" + testApp.getLauncherName())
                .runBeforeAll(AutomationUtils::wakeUpAndGoToHomeScreen)
                .runBefore(testApp::open)
                .runBefore(device::waitForIdle)
                .run(AutomationUtils::launchSplitscreen)
                .run(device::waitForIdle)
                .run(() -> sleep(2000))
                .runAfter(AutomationUtils::exitSplitscreen)
                .runAfterAll(testApp::exit)
                .repeat(ITERATIONS);
    }

    static TransitionBuilder splitScreenToLauncher(IAppHelper testApp, UiDevice device) {
        return TransitionRunner.newBuilder()
                .withTag("splitScreenToLauncher_" + testApp.getLauncherName())
                .runBeforeAll(AutomationUtils::wakeUpAndGoToHomeScreen)
                .runBefore(testApp::open)
                .runBefore(AutomationUtils::launchSplitscreen)
                .runBefore(() -> sleep(2000))
                .run(AutomationUtils::exitSplitscreen)
                .run(device::waitForIdle)
                .run(() -> sleep(2000))
                .runAfterAll(testApp::exit)
                .repeat(ITERATIONS);
    }
}