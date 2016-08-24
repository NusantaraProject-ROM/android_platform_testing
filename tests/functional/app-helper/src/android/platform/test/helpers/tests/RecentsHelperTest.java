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

import android.app.Instrumentation;
import android.platform.test.helpers.ChromeHelperImpl;
import android.platform.test.helpers.GoogleCameraHelperImpl;
import android.platform.test.helpers.IStandardAppHelper;
import android.platform.test.helpers.RecentsHelperImpl;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RecentsHelperTest extends BaseHelperTest {
    private RecentsHelperImpl mHelper;
    private UiDevice mDevice;

    public RecentsHelperTest () {
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        mHelper = new RecentsHelperImpl(instr);
        mDevice = UiDevice.getInstance(instr);

        GoogleCameraHelperImpl cameraHelper = new GoogleCameraHelperImpl(instr);
        ChromeHelperImpl chromeHelper = new ChromeHelperImpl(instr);
        // Populate the recent apps screen
        cameraHelper.open();
        mDevice.pressHome();
        chromeHelper.open();
        mDevice.pressHome();
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
    public void testFlingRecents() {
        mHelper.flingRecents(Direction.UP);
        mDevice.waitForIdle();
        mHelper.flingRecents(Direction.DOWN);
    }
}
