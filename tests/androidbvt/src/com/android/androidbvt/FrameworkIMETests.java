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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;
import android.view.inputmethod.InputMethodInfo;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests to ensure IME service runs and onClick() on a EditTextView prompts GoogleKB to popup and
 * onClick() on any char commits to EditTextView. For tests, Settings search view which is a
 * EditText view has been chosen.
 */
public class FrameworkIMETests extends TestCase {
    private final String SEARCH_RES_ID = "android:id/search_src_text";
    private UiDevice mDevice;
    private Context mContext = null;
    private AndroidBvtHelper mABvtHelper = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = InstrumentationRegistry.getTargetContext();
        mABvtHelper = AndroidBvtHelper.getInstance(mDevice, mContext,
                InstrumentationRegistry.getInstrumentation().getUiAutomation());
        mDevice.setOrientationNatural();
        mDevice.pressHome();
    }

    @Override
    public void tearDown() throws Exception {
        mDevice.wakeUp();
        mDevice.unfreezeRotation();
        mDevice.pressHome();
        mDevice.waitForIdle();
        super.tearDown();
    }

    /**
     * IME should be one of default IME services By default, 2 IME are default services
     * com.android.inputmethod.latin.LatinIME
     * com.google.android.voicesearch.ime.VoiceInputMethodService
     */
    public void testIsEnabledLatinIME() {
        List<InputMethodInfo> inputMethodList = mABvtHelper.getInputMethodManager()
                .getEnabledInputMethodList();
        for (InputMethodInfo im : inputMethodList) {
            if (im.getServiceName().equalsIgnoreCase(mABvtHelper.GOOGLE_KB_SVC)) {
                return;
            }
        }
        fail("Latin IME isn't one of default IME");
    }

    /**
     * Tests onClick() on EditTextView, GoogleKB pops up
     */
    public void testGoogleKBShownWhenTextViewClicked() throws InterruptedException {
        openSettingsSearchEditTextView();
        assertTrue("Google KB hasn't popped up", mDevice.wait(
                Until.hasObject(By.res(mABvtHelper.GOOGLE_KB_PACKAGE, "keyboard_area")),
                mABvtHelper.LONG_TIMEOUT));
        assertFalse("Google KB shouldn't be full screen",
                mABvtHelper.getInputMethodManager().isFullscreenMode());
    }

    /**
     * Tests onClick() a char on GoogleKB shows up in Edit text view
     */
    public void testCharInputFromKBCommitsInEditTextView() throws InterruptedException {
        openSettingsSearchEditTextView();
        /**
         * chars can be identified on KB by indices For QWERTY KB example:
         * 'Q' is the first char on first row, index : 0, 0. It's resource id is key_pos_0_0
         * 'A' is the first char on first row. index : 1, 0. It's resource id is key_pos_1_0
         */
        try {
            for (int i = 0; i < 2; ++i) {
                mDevice.wait(
                        Until.findObject(By.res(mABvtHelper.GOOGLE_KB_PACKAGE,
                                String.format("key_pos_%d_0", i))),
                        mABvtHelper.LONG_TIMEOUT).click();
                Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
            }
            // Hide KB now
            mDevice.pressBack();
            // Now ensure text is committed in search text view
            assertTrue("Search textview's text isn't 'qa' as expected",
                    mDevice.wait(Until.findObject(By.res(SEARCH_RES_ID)),
                            mABvtHelper.LONG_TIMEOUT).getText().equals("qa"));
        } finally {
            mDevice.wait(Until.findObject(By.res(SEARCH_RES_ID)),
                    mABvtHelper.LONG_TIMEOUT).setText("");
        }
    }

    /**
     * Clicking on EditTextView causes IME to pop up Search box in settings is an EditTextView.
     * There are EditTextView too But searchbox in settings have been chosen for its simplicity
     * Following method opens searchbox in settings view
     * @throws InterruptedException
     */
    private void openSettingsSearchEditTextView() throws InterruptedException {
        mABvtHelper.launchPackage(mABvtHelper.SETTINGS_PACKAGE);
        mDevice.waitForIdle();
        mDevice.wait(Until
                .findObject(By.res(mABvtHelper.SETTINGS_PACKAGE, "search")),
                mABvtHelper.LONG_TIMEOUT).click();
        Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
        mDevice.wait(
                Until.findObject(By.res(SEARCH_RES_ID)),
                mABvtHelper.LONG_TIMEOUT).setText("");
        Thread.sleep(mABvtHelper.SHORT_TIMEOUT);
    }
}