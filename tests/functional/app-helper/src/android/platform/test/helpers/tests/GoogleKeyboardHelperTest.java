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
import android.os.SystemClock;
import android.platform.test.helpers.GoogleKeyboardHelperImpl;
import android.platform.test.helpers.IStandardAppHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;

public class GoogleKeyboardHelperTest extends BaseHelperTest {
    private GoogleKeyboardHelperImpl mHelper;
    private UiDevice mDevice;

    public GoogleKeyboardHelperTest () {
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        mHelper = new GoogleKeyboardHelperImpl(instr);
        mDevice = UiDevice.getInstance(instr);
    }

    @Override
    protected IStandardAppHelper getHelper() {
        return mHelper;
    }

    @After
    public void after() {
        mDevice.pressHome();
    }

    @Test
    public void testDismissInitialDialogs() {
        // Sleep reduces flakiness after clearing state
        SystemClock.sleep(5000);
        mHelper.dismissInitialDialogs();
    }

    @Test
    @Ignore("Flaky, and not critical test functionality.")
    public void testWaitForKeyboard_False() {
        // Sleep reduces flakiness after clearing state
        SystemClock.sleep(5000);
        mHelper.dismissInitialDialogs();
        mHelper.exit();
        Assert.assertFalse("Method didn't return false waiting for the keyboard",
                mHelper.waitForKeyboard(2500));
    }

    @Test
    @Ignore("Flaky, and not critical test functionality.")
    public void testWaitForKeyboard_True() {
        // Sleep reduces flakiness after clearing state
        SystemClock.sleep(5000);
        mHelper.dismissInitialDialogs();
        Assert.assertTrue("Method didn't return true waiting for the keyboard",
                mHelper.waitForKeyboard(2500));
    }

    @Test
    @Ignore("Flaky, and not critical test functionality.")
    public void testTypeText() {
        // Sleep reduces flakiness after clearing state
        SystemClock.sleep(5000);
        mHelper.dismissInitialDialogs();
        mHelper.waitForKeyboard(2500);
        mHelper.typeText("abcd efgh", 250);
    }
}
