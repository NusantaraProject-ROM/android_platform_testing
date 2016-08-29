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

import android.app.UiAutomation;
import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import junit.framework.TestCase;

public class SysUIMultiUserTests extends TestCase {
    private final String TEST_USER = "test";
    private UiAutomation mUiAutomation = null;
    private UiDevice mDevice;
    private Context mContext = null;
    private AndroidBvtHelper mABvtHelper = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.setOrientationNatural();
        mContext = InstrumentationRegistry.getTargetContext();
        mUiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext, mUiAutomation);
    }

    @Override
    public void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        super.tearDown();
    }

    /**
     * Following test creates a second user and verifies user created
     */
    @LargeTest
    public void testMultiUserCreate() throws InterruptedException, IOException {
        int secondUserId = -1;
        List<String> cmdOut;
        try {
            // Ensure there are exactly 1 user
            assertTrue("There aren't exactly 1 user", mABvtHelper.getUserCount() == 1);
            secondUserId = mABvtHelper.createSecondaryUser(TEST_USER);
            Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
            assertTrue("There aren't exactly 2 users", mABvtHelper.getUserCount() == 2);
            assertEquals("Second User id doesn't match", mABvtHelper.getSecondaryUserId(),
                    secondUserId);
            Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
        } finally {
            if (secondUserId != -1) {
                mABvtHelper.removeSecondaryUser(secondUserId);
            }
        }
    }

    /**
     * Test ensures Second user shows up in QS panel
     */
    public void testSecondUSerIconShowsUpInUserSettings() {
        int secondUserId = -1;
        try {
            secondUserId = mABvtHelper.createSecondaryUser(TEST_USER);
            mDevice.openQuickSettings();
            mDevice.waitForIdle();
            mDevice.wait(Until.findObject(By.res("com.android.systemui:id/multi_user_avatar")),
                    mABvtHelper.SHORT_TIMEOUT)
                    .clickAndWait(Until.newWindow(), mABvtHelper.LONG_TIMEOUT);
            assertTrue("", mDevice.wait(
                    Until.hasObject(By.res("com.android.systemui:id/user_name").text(TEST_USER)),
                    mABvtHelper.LONG_TIMEOUT));
        } finally {
            if (secondUserId != -1) {
                mABvtHelper.removeSecondaryUser(secondUserId);
            }
        }
    }

    /**
     * Test ensures owner has no access to second user's dir
     */
    public void testPrimaryUserHasNoAccessToSecondUserData() {
        int secondUserId = -1;
        try {
            secondUserId = mABvtHelper.createSecondaryUser(TEST_USER);
            // Ensure owner has no access to second user's directory
            final File myPath = Environment.getExternalStorageDirectory();
            final int myId = android.os.Process.myUid() / 100000;
            final File basePath = myPath.getParentFile();
            assertEquals(String.valueOf(myId), myPath.getName());
            for (int i = 0; i < 128; i++) {
                if (i == myId) {
                    continue;
                }

                final File otherPath = new File(basePath, String.valueOf(i));
                assertNull("Owner have access to other user's resources!", otherPath.list());
                assertFalse("Owner can read other user's content!", otherPath.canRead());
            }
        } finally {
            if (secondUserId != -1) {
                mABvtHelper.removeSecondaryUser(secondUserId);
            }
        }
    }
}
