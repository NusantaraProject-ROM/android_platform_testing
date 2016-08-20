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

import android.platform.test.helpers.GoogleKeyboardHelperImpl;
import android.platform.test.helpers.IStandardAppHelper;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;

public class GoogleKeyboardHelperTest extends BaseHelperTest {
    private GoogleKeyboardHelperImpl mHelper;

    public GoogleKeyboardHelperTest () {
        mHelper = new GoogleKeyboardHelperImpl(InstrumentationRegistry.getInstrumentation());
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
    @Ignore("Not critical test functionality.")
    public void testWaitForKeyboard_False() {
        mHelper.dismissInitialDialogs();
        mHelper.exit();
        Assert.assertFalse("Method didn't return false waiting for the keyboard",
                mHelper.waitForKeyboard(2500));
    }

    @Test
    @Ignore("Not critical test functionality.")
    public void testWaitForKeyboard_True() {
        mHelper.dismissInitialDialogs();
        Assert.assertTrue("Method didn't return true waiting for the keyboard",
                mHelper.waitForKeyboard(2500));
    }

    @Test
    @Ignore("Not critical test functionality.")
    public void testTypeText() {
        mHelper.dismissInitialDialogs();
        mHelper.waitForKeyboard(2500);
        mHelper.typeText("abcd efgh", 250);
    }
}
