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

package com.android.functional.permissiontests;

import android.app.UiAutomation;
import android.content.Context;
import android.support.test.uiautomator.UiDevice;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Suppress;

import com.android.functional.permissiontests.PermissionHelper.PermissionStatus;

import java.util.Arrays;
import java.util.List;

public class GenericAppPermissionTests extends InstrumentationTestCase {
    protected final String TARGET_APP_PKG = "com.android.functional.permissiontests";
    private UiDevice mDevice = null;
    private Context mContext = null;
    private UiAutomation mUiAutomation = null;
    private PermissionHelper pHelper;
    private final String[] mDefaultPermittedGroups = new String[] {
            "CONTACTS", "SMS", "STORAGE"
    };

    private final String[] mDefaultPermittedDangerousPermissions = new String[] {
            "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_CONTACTS",
            "android.permission.SEND_SMS", "android.permission.RECEIVE_SMS"
    };

    private List<String> mDefaultGrantedPermissions;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mContext = getInstrumentation().getContext();
        mUiAutomation = getInstrumentation().getUiAutomation();
        pHelper = PermissionHelper.getInstance(mDevice, mContext, mUiAutomation);
        mDefaultGrantedPermissions = pHelper.getPermissionByPackage(TARGET_APP_PKG, Boolean.TRUE);
    }

    @SmallTest
    public void testDefaultDangerousPermissionGranted() {
        pHelper.verifyDefaultDangerousPermissionGranted(TARGET_APP_PKG,
                mDefaultPermittedDangerousPermissions, Boolean.FALSE);
    }

    @SmallTest
    public void testExtraDangerousPermissionNotGranted() {
        pHelper.verifyExtraDangerousPermissionNotGranted(TARGET_APP_PKG, mDefaultPermittedGroups);
    }

    @SmallTest
    public void testNormalPermissionsAutoGranted() {
        pHelper.verifyNormalPermissionsAutoGranted(TARGET_APP_PKG);
    }

    @Suppress
    @MediumTest
    public void testToggleAppPermisssionOFF() {
        pHelper.togglePermissionSetting(TARGET_APP_PKG, "Contacts", Boolean.FALSE);
        pHelper.verifyPermissionSettingStatus(TARGET_APP_PKG, "Contacts", PermissionStatus.OFF);
    }

    @Suppress
    @MediumTest
    public void testToggleAppPermisssionON() {
        pHelper.togglePermissionSetting(TARGET_APP_PKG, "Contacts", Boolean.TRUE);
        pHelper.verifyPermissionSettingStatus(TARGET_APP_PKG, "Contacts", PermissionStatus.ON);
    }

    @MediumTest
    public void testViewPermissionDescription() {
        List<String> permissionDescGrpNamesList = pHelper
                .getPermissionDescGroupNames(TARGET_APP_PKG);
        permissionDescGrpNamesList.removeAll(Arrays.asList(mDefaultPermittedGroups));
        assertTrue("Still there are more", permissionDescGrpNamesList.isEmpty());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
