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
package com.android.helpers.tests;

import android.os.SystemClock;
import android.support.test.uiautomator.UiDevice;
import androidx.test.InstrumentationRegistry;

import java.io.IOException;

public class HelperTestUtility {

    // Clear the cache
    private static final String CLEAR_CACHE = "echo 3 > /proc/sys/vm/drop_caches";
    // Delay between actions happening in the device.
    public static final int ACTION_DELAY = 2000;

    private static UiDevice mDevice;

    /**
     * Returns the active {@link UiDevice} to interact with.
     */
    public static UiDevice getUiDevice() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }
        return mDevice;
    }

    /**
     * Runs a shell command, {@code cmd}, and returns the output.
     */
    public static String executeShellCommand(String cmd) {
        try {
            return getUiDevice().executeShellCommand(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clears the cache.
     */
    public static void clearCache() {
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        executeShellCommand(CLEAR_CACHE);
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
    }

    /**
     * Close the test app and clear the cache.
     * TODO: Replace it with the rules.
     */
    public static void clearApp(String killCmd) {
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        executeShellCommand(killCmd);
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
        executeShellCommand(CLEAR_CACHE);
        SystemClock.sleep(HelperTestUtility.ACTION_DELAY);
    }

}

