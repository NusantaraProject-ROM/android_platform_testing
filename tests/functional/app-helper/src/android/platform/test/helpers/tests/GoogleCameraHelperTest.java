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

package android.platform.test.helpers.tests;

import android.platform.test.helpers.GoogleCameraHelperImpl;
import android.platform.test.helpers.IStandardAppHelper;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class GoogleCameraHelperTest extends BaseHelperTest {
    private GoogleCameraHelperImpl mHelper;

    public GoogleCameraHelperTest () {
        mHelper = new GoogleCameraHelperImpl(InstrumentationRegistry.getInstrumentation());
    }

    @Override
    protected IStandardAppHelper getHelper() {
        return mHelper;
    }

    @Before
    public void before() {
        mHelper.open();
    }

    @After
    public void after() {
        mHelper.exit();
    }

    @Test
    public void testDismissInitialDialogs() {
        mHelper.dismissInitialDialogs();
    }

    @Test
    public void testGoToVideoMode() {
        mHelper.dismissInitialDialogs();
        mHelper.goToVideoMode();
    }

    @Test
    public void testGoToCameraMode() {
        mHelper.dismissInitialDialogs();
        mHelper.goToVideoMode();
        mHelper.goToCameraMode();
    }

    @Test
    public void testWaitForCameraShutterEnabled() {
        mHelper.dismissInitialDialogs();
        mHelper.waitForCameraShutterEnabled();
    }

    @Test
    public void testWaitForVideoShutterEnabled() {
        mHelper.dismissInitialDialogs();
        mHelper.goToVideoMode();
        mHelper.waitForVideoShutterEnabled();
    }

    @Test
    public void testCapturePhoto() {
        mHelper.dismissInitialDialogs();
        mHelper.waitForCameraShutterEnabled();
        mHelper.capturePhoto();
    }

    @Test
    public void testCaptureVideo() {
        mHelper.dismissInitialDialogs();
        mHelper.goToVideoMode();
        mHelper.waitForVideoShutterEnabled();
        mHelper.captureVideo(5000);
    }

    @Test
    @Ignore("Not supported by all devices.")
    public void testSetHdrMode_AUTO() {
        mHelper.dismissInitialDialogs();
        mHelper.setHdrMode(GoogleCameraHelperImpl.HDR_MODE_AUTO);
    }

    @Test
    @Ignore("Not supported by all devices.")
    public void testSetHdrMode_OFF() {
        mHelper.dismissInitialDialogs();
        mHelper.setHdrMode(GoogleCameraHelperImpl.HDR_MODE_OFF);
    }

    @Test
    @Ignore("Not supported by all devices.")
    public void testSetHdrMode_ON() {
        mHelper.dismissInitialDialogs();
        mHelper.setHdrMode(GoogleCameraHelperImpl.HDR_MODE_ON);
    }
}
