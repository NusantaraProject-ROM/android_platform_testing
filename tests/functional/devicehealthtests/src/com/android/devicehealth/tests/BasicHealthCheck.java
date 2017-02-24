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

import android.content.Context;
import android.os.DropBoxManager;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests used for basic device health validation (ex: system app crash or system app
 * native crash) after the device boot is completed. This test class can be used to
 * add more tests in the future for additional basic device health validation
 * after the device boot is completed.
 */
@RunWith(Parameterized.class)
public class BasicHealthCheck {

    private Context mContext;

    @Parameter
    public String mDropboxLabel;

    @Parameters(name = "{0}")
    public static String[] dropboxLabels() {
        return new String[] {"system_app_native_crash",
                "system_server_crash", "system_server_native_crash", "system_server_anr"};
    }

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getInstrumentation().getContext();
    }

    /**
     * Test if there are app crashes in the device by checking system_app_crash,
     * system_app_native_crash or system_server_anr using DropBoxManager service.
     */
    @Test
    public void checkCrash() {
        DropBoxManager dropbox = (DropBoxManager) mContext
                .getSystemService(Context.DROPBOX_SERVICE);
        Assert.assertNotNull("Unable access the DropBoxManager service", dropbox);

        long timestamp = 0;
        DropBoxManager.Entry entry = null;
        int crashCount = 0;
        StringBuilder errorDetails = new StringBuilder("Error details:\n");
        while (null != (entry = dropbox.getNextEntry(null, timestamp))) {
            try {
                if (mDropboxLabel.equals(entry.getTag())) {
                    crashCount++;
                    errorDetails.append(mDropboxLabel);
                    errorDetails.append(": ");
                    errorDetails.append(entry.getText(70));
                    errorDetails.append("    ...\n");
                }
            } finally {
                entry.close();
            }
            timestamp = entry.getTimeMillis();
        }
        Assert.assertEquals(errorDetails.toString(), 0, crashCount);
    }

}
