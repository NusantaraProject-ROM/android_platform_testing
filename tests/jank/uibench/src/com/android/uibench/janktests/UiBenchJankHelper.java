/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.uibench.janktests;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.platform.helpers.AbstractStandardAppHelper;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.DisplayMetrics;
import android.widget.ListView;

import junit.framework.Assert;

public class UiBenchJankHelper extends AbstractStandardAppHelper implements IUiBenchJankHelper {
    public static final int LONG_TIMEOUT = 5000;
    public static final int FULL_TEST_DURATION = 25000;
    public static final int FIND_OBJECT_TIMEOUT = 250;
    public static final int SHORT_TIMEOUT = 2000;
    public static final int EXPECTED_FRAMES = 100;
    public static final int KEY_DELAY = 1000;

    /**
     * Only to be used for initial-fling tests, or similar cases where perf during brief experience
     * is important.
     */
    public static final int SHORT_EXPECTED_FRAMES = 30;

    public static final String PACKAGE_NAME = "com.android.test.uibench";

    public static final String APP_LAUNCHER_NAME = "UiBench";

    private static final int SLOW_FLING_SPEED = 3000; // compare to UiObject2#DEFAULT_FLING_SPEED

    private UiObject2 mContents;

    public UiBenchJankHelper(Instrumentation instr) {
        super(instr);
    }

    /** {@inheritDoc} */
    @Override
    public String getPackage() {
        return PACKAGE_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public String getLauncherName() {
        return APP_LAUNCHER_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public void dismissInitialDialogs() {}

    /** Launch activity using intent */
    void launchActivity(String activityName, Bundle extras, String verifyText) {
        ComponentName cn =
                new ComponentName(PACKAGE_NAME, String.format("%s.%s", PACKAGE_NAME, activityName));
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.setComponent(cn);
        // Launch the activity
        mInstrumentation.getContext().startActivity(intent);
        UiObject2 expectedTextCmp =
                mDevice.wait(Until.findObject(By.text(verifyText)), LONG_TIMEOUT);
        Assert.assertNotNull(String.format("Issue in opening %s", activityName), expectedTextCmp);
    }

    void launchActivity(String activityName, String verifyText) {
        launchActivity(activityName, null, verifyText);
    }

    void launchActivityAndAssert(String activityName, String verifyText) {
        launchActivity(activityName, verifyText);
        mContents =
                mDevice.wait(Until.findObject(By.res("android", "content")), FIND_OBJECT_TIMEOUT);
        Assert.assertNotNull(activityName + " isn't found", mContents);
    }

    /** To perform the fling down and up on given content for flingCount number of times */
    @Override
    public void flingUpDown(int flingCount) {
        flingUpDown(flingCount, false);
    }

    @Override
    public void flingDownUp(int flingCount) {
        flingUpDown(flingCount, true);
    }

    void flingUpDown(int flingCount, boolean reverse) {
        for (int count = 0; count < flingCount; count++) {
            SystemClock.sleep(SHORT_TIMEOUT);
            mContents.fling(reverse ? Direction.UP : Direction.DOWN);
            SystemClock.sleep(SHORT_TIMEOUT);
            mContents.fling(reverse ? Direction.DOWN : Direction.UP);
        }
    }

    /** To perform the swipe right and left on given content for swipeCount number of times */
    @Override
    public void swipeRightLeft(int swipeCount) {
        for (int count = 0; count < swipeCount; count++) {
            SystemClock.sleep(SHORT_TIMEOUT);
            mContents.swipe(Direction.RIGHT, 1);
            SystemClock.sleep(SHORT_TIMEOUT);
            mContents.swipe(Direction.LEFT, 1);
        }
    }

    @Override
    public void slowSingleFlingDown() {
        SystemClock.sleep(SHORT_TIMEOUT);
        Context context = mInstrumentation.getContext();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mContents.fling(Direction.DOWN, (int) (SLOW_FLING_SPEED * displayMetrics.density));
        mDevice.waitForIdle();
    }

    @Override
    public void pressKeyCode(int keyCode) {
        SystemClock.sleep(KEY_DELAY);
        mDevice.pressKeyCode(keyCode);
    }

    @Override
    public void openDialogList() {
        launchActivity("DialogListActivity", "Dialog");
        mContents = mDevice.wait(Until.findObject(By.clazz(ListView.class)), FIND_OBJECT_TIMEOUT);
        Assert.assertNotNull("Dialog List View isn't found", mContents);
    }
}
