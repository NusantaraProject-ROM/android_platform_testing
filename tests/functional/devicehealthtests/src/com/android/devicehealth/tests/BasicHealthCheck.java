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
package com.android.devicehealth.tests;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;

import android.content.Context;
import android.os.DropBoxManager;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

/**
 * Tests used for basic device health validation (ex: system app crash or system app
 * native crash) after the device boot is completed. This test class can be used to
 * add more tests in the future for additional basic device health validation
 * after the device boot is completed.
 */
@RunWith(AndroidJUnit4.class)
public class BasicHealthCheck {

    private static final String SYSTEM_APP_CRASH_TAG = "system_app_crash";
    private static final String SYSTEM_APP_NATIVE_CRASH_TAG = "system_app_native_crash";
    private static final String SYSTEM_SERVER_ANR_TAG = "system_server_anr";
    private static final String OUTPUT_FORMAT = "%s count = %d, %s count = %d, %s count = %d";
    private static final String TEST_APP_CRASHES = "testAppCrashes";

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = getInstrumentation().getContext();
    }

    /**
     * Test if there are app crashes in the device by checking system_app_crash or
     * system_app_native_crash using DropBoxManager service.
     */
    @Test
    public void testAppCrashes() {
        DropBoxManager dropbox = (DropBoxManager) mContext
                .getSystemService(mContext.DROPBOX_SERVICE);
        assertNotNull("Unable access the DropBoxManager service", dropbox);
        int systemAppCrashCount = 0;
        int systemAppNativeCrashCount = 0;
        int systemServerAnrCount = 0;

        DropBoxManager.Entry entry = dropbox.getNextEntry(null, 0);
        // TODO: Fail the test if system_server_anr is observed or not.
        while (null != entry) {
            String errorTag = entry.getTag();
            if (SYSTEM_APP_CRASH_TAG.equalsIgnoreCase(errorTag)) {
                systemAppCrashCount++;
            } else if (SYSTEM_APP_NATIVE_CRASH_TAG.equalsIgnoreCase(errorTag)) {
                systemAppNativeCrashCount++;
            } else if (SYSTEM_SERVER_ANR_TAG.equalsIgnoreCase(errorTag)) {
                systemServerAnrCount++;
            }
            entry.close();
            entry = dropbox.getNextEntry(null, entry.getTimeMillis());
        }
        String status = String.format(OUTPUT_FORMAT, SYSTEM_APP_CRASH_TAG, systemAppCrashCount,
                SYSTEM_APP_NATIVE_CRASH_TAG, systemAppNativeCrashCount, SYSTEM_SERVER_ANR_TAG,
                systemServerAnrCount);
        Log.i(TEST_APP_CRASHES, status);
        assertEquals(status, 0, (systemAppCrashCount + systemAppNativeCrashCount));
    }

}
