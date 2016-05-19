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

package com.android.test.util.account;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * A utility for Google account syncing
 */
public class AccountSyncInstrumentation extends Instrumentation {
    private static final String LOG_TAG = AccountSyncInstrumentation.class.getSimpleName();

    private static final long DEFAULT_INTERVAL = TimeUnit.SECONDS.toMillis(10);
    private static final long DEFAULT_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    // Option: long to indicate the sync-checking interval
    private static final String PARAM_INTERVAL = "interval";
    // Option: total timeout to quit sync-checking
    private static final String PARAM_TIMEOUT = "timeout";

    // Key for a Bundle's status message
    private static final String BUNDLE_STATUS_MESSAGE_KEY = "status-message";

    private long mInterval;
    private long mTimeout;
    private Runnable mSyncCheckRunnable = new Runnable() {
        private long mStartTime;
        @Override
        public void run() {
            mStartTime = SystemClock.uptimeMillis();
            while (SystemClock.uptimeMillis() - mStartTime < mTimeout) {
                int syncSize = getCurrentSyncsSize();
                if (syncSize == 0) {
                    updateStatus(true, Activity.RESULT_OK, "All sync completed.");
                    return;
                } else {
                    // Active syncs
                    long sleepDuration = Math.min(mInterval, mTimeout -
                            SystemClock.uptimeMillis() - mStartTime);
                    updateStatus(false, Activity.RESULT_OK, String.format(
                            "Active syncs: %d, sleep for %d ms", syncSize, sleepDuration));
                    try {
                        Thread.sleep(sleepDuration);
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "AccountSyncInstrumentation InterruptedException", e);
                        updateStatus(true, Activity.RESULT_CANCELED, "Interrupted thread.");
                        return;
                    }
                }
            }
            // Timeout exceeded
            updateStatus(true, Activity.RESULT_CANCELED, "Sync utility timed out.");
        }
    };

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        String intervalString = arguments.getString(PARAM_INTERVAL);
        mInterval = (intervalString != null) ? Long.parseLong(intervalString) : DEFAULT_INTERVAL;

        String timeoutString = arguments.getString(PARAM_TIMEOUT);
        mTimeout = (timeoutString != null) ? Long.parseLong(timeoutString) : DEFAULT_TIMEOUT;

        start();
    }

    @Override
    public void onStart() {
        super.onStart();

        Thread syncThread = new Thread(mSyncCheckRunnable);
        syncThread.start();
    }

    public int getCurrentSyncsSize() {
        return getContext().getContentResolver().getCurrentSyncs().size();
    }

    public void updateStatus(boolean end, int result, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_STATUS_MESSAGE_KEY, message);

        if (end) {
            finish(result, bundle);
        } else {
            sendStatus(result, bundle);
        }
    }
}
