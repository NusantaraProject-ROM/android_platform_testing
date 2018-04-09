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

import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.Configurator;
import android.support.test.uiautomator.UiDevice;

/**
 * Collection of UI Automation helper functions.
 */
public class AutomationUtils {
    public static void wakeUpAndGoToHomeScreen() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry
                .getInstrumentation());
        try {
            device.wakeUp();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        device.pressHome();
    }

    /**
     * Sets {@link android.app.UiAutomation#waitForIdle(long, long)} global timeout to 0 causing
     * the {@link android.app.UiAutomation#waitForIdle(long, long)} function to timeout instantly.
     * This removes some delays when using the UIAutomator library required to create fast UI
     * transitions.
     */
    static void setFastWait() {
        Configurator.getInstance().setWaitForIdleTimeout(0);
    }

    /**
     * Reverts {@link android.app.UiAutomation#waitForIdle(long, long)} to default behavior.
     */
    static void setDefaultWait() {
        Configurator.getInstance().setWaitForIdleTimeout(10000);
    }
}
