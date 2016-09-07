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

package android.platform.test.helpers.tv;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.platform.test.helpers.AbstractLeanbackAppHelper;
import android.platform.test.helpers.exceptions.UiTimeoutException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Until;
import android.util.Log;


public class SearchHelperImpl extends AbstractLeanbackAppHelper {

    private static final String TAG = SearchHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.google.android.katniss";
    private static final String SEARCH_ACTIVITY_NAME =
            "com.google.android.katniss.search.SearchActivity";
    private static final long SHORT_SLEEP_MS = 3000;    // 3 seconds

    private static Instrumentation  mInstrumentation;

    public static final int VOICE_SEARCH = 1;
    public static final int KEYBOARD_SEARCH = 2;


    public SearchHelperImpl(Instrumentation instrumentation) {
        super(instrumentation);
        mInstrumentation = instrumentation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return UI_PACKAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return null;
    }

    /**
     * Setup expectations: None
     *
     * Starts the voice search activity for querying the content.
     * @param searchType Type of search request (1=voice, 2=keyboard)
     * @param searchQuery Query string
     */
    public void launchActivityAndQuery(int searchType, String searchQuery) {
        Intent intent = new Intent("android.intent.action.ASSIST");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setComponent(new ComponentName(UI_PACKAGE, SEARCH_ACTIVITY_NAME));
        intent.putExtra("search_type", searchType);
        intent.putExtra("query", searchQuery);
        mInstrumentation.getContext().startActivity(intent);
        Log.d(TAG, String.format("launchActivityAndQuery searchType=%d query=%s", searchType,
                searchQuery));

        // Ensure that the package is open
        if (isOpen(SHORT_SLEEP_MS) == false) {
            throw new UiTimeoutException("The Search activity is not launched.");
        }
        if (isInKeyboardMode()) {
            Log.i(TAG, "Search activity Is in keyboard mode. Pressing the ENTER key.");
            mDPadHelper.pressEnter();
            mDevice.waitForIdle();
        }
    }

    public BySelector getSearchTextEditorSelector() {
        return By.res(UI_PACKAGE, "search_text_editor");
    }

    public BySelector getResultContainerSelector() {
        return By.res(UI_PACKAGE, "container_list");
    }

    public boolean isInKeyboardMode() {
        return mDevice.hasObject(getSearchTextEditorSelector());
    }

    public boolean isOpen(long waitMs) {
        return mDevice.wait(Until.hasObject(By.pkg(UI_PACKAGE).depth(0)), waitMs);
    }
}

