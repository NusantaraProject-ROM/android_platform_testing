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

import android.platform.test.helpers.PlayMusicHelperImpl;
import android.platform.test.helpers.IStandardAppHelper;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PlayMusicHelperTest extends BaseHelperTest {
    private PlayMusicHelperImpl mHelper;

    public PlayMusicHelperTest () {
        mHelper = new PlayMusicHelperImpl(InstrumentationRegistry.getInstrumentation());
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
    public void testGoToSongsTab() {
        mHelper.dismissInitialDialogs();
        mHelper.goToTab("Songs");
    }

    @Test
    public void testShuffleAllSongs() {
        mHelper.dismissInitialDialogs();
        mHelper.goToTab("Songs");
        mHelper.pressShuffleAll();
    }
}
