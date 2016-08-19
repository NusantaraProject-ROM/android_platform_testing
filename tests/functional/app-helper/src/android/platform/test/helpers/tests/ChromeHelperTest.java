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

import android.platform.test.helpers.ChromeHelperImpl;
import android.platform.test.helpers.IStandardAppHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.Direction;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ChromeHelperTest extends BaseHelperTest {
    private ChromeHelperImpl mHelper;

    public ChromeHelperTest () {
        mHelper = new ChromeHelperImpl(InstrumentationRegistry.getInstrumentation());
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
    public void testOpenUrl() {
        mHelper.dismissInitialDialogs();
        mHelper.openUrl("news.google.com");
    }

    @Test
    public void testFlingPage() {
        mHelper.dismissInitialDialogs();
        mHelper.openUrl("news.google.com");
        mHelper.flingPage(Direction.DOWN);
    }

    @Test
    @Ignore("Not critical for testing.")
    public void testOpenMenu() {
        mHelper.dismissInitialDialogs();
        mHelper.openUrl("news.google.com");
        mHelper.openMenu();
    }

    @Test
    @Ignore("Not critical for testing.")
    public void testMergeTabs() {
        mHelper.dismissInitialDialogs();
        mHelper.openUrl("news.google.com");
        mHelper.mergeTabs();
    }

    @Test
    @Ignore("Not critical for testing.")
    public void testUnmergeTabs() {
        mHelper.dismissInitialDialogs();
        mHelper.openUrl("news.google.com");
        mHelper.mergeTabs();
        mHelper.unmergeTabs();
    }
}
