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

package android.test.functional.tv.common;

import android.app.Instrumentation;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiWatcher;
import android.support.test.uiautomator.Until;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to monitor a UI object to dismiss.
 */
public final class UiWatchers {
    private static final String LOG_TAG = UiWatchers.class.getSimpleName();
    private static final long WAIT_TIME_MS = 3000;

    private UiDevice mDevice;
    private List<String> mWatcherNames = new ArrayList<>();


    public UiWatchers(Instrumentation instrumentation) {
        mDevice = UiDevice.getInstance(instrumentation);
    }

    /**
     * Register a new watcher that looks for a object and dismisses it.
     */
    public void registerDismissWatcher(final String watcherName, final BySelector watch,
            final BySelector click) {
        if (mWatcherNames.contains(watcherName)) {
            Log.i(LOG_TAG,
                    String.format("The watcher %s already registered. Skipped!", watcherName));
            return;
        }
        mWatcherNames.add(watcherName);
        mDevice.registerWatcher(watcherName, new UiWatcher() {
            @Override
            public boolean checkForCondition() {
                if (mDevice.hasObject(watch)) {
                    UiObject2 dismiss = mDevice.wait(Until.findObject(click), WAIT_TIME_MS);
                    dismiss.click();
                    postHandler(watcherName);
                    return true;    // triggered
                }
                return false;   // not triggered
            }
        });
    }

    public void unregisterDismissWatcher(String watcherName) {
        mDevice.removeWatcher(watcherName);
        mWatcherNames.remove(watcherName);
    }

    /**
     * Checks if any registered UiWatcher have triggered.
     * @return
     */
    public boolean hasWatcherTriggered() {
        for (String watcherName : mWatcherNames) {
            if (hasWatcherTriggered(watcherName)) {
                Log.i(LOG_TAG,
                        String.format("Found the watcher %s have triggered.", watcherName));
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a specific registered UiWatcher has triggered.
     * @param watcherName
     * @return
     */
    public boolean hasWatcherTriggered(String watcherName) {
        if (!mWatcherNames.contains(watcherName)) {
            Log.w(LOG_TAG, String.format("The watcher %s not registered.", watcherName));
        }
        return mDevice.hasWatcherTriggered(watcherName);
    }

    public void resetWatchers() {
        mDevice.resetWatcherTriggers();
    }

    /**
     * Current implementation ignores the exception and continues.
     */
    public void postHandler(String watcherName) {
        Log.i(LOG_TAG, String.format("%s dismissed", watcherName));
    }
}
