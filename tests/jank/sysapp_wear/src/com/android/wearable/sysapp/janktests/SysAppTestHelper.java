/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.wearable.sysapp.janktests;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;

import junit.framework.Assert;

/**
 * Helper for all they system apps tests
 */
public class SysAppTestHelper {

    public static final int MIN_FRAMES = 20;
    public static final int LONG_TIMEOUT = 5000;
    public static final int SHORT_TIMEOUT = 500;
    private static final long NEW_CARD_TIMEOUT_MS = 10 * 1000; // 10s
    private static final int CARD_SWIPE_STEPS = 20;

    // Demo card selectors
    private static final UiSelector CARD_SELECTOR = new UiSelector()
            .resourceId("com.google.android.wearable.app:id/snippet");
    private static final UiSelector TITLE_SELECTOR = new UiSelector()
            .resourceId("com.google.android.wearable.app:id/title");
    private static final UiSelector CLOCK_SELECTOR = new UiSelector()
            .resourceId("com.google.android.wearable.app:id/clock_bar");

    private UiDevice mDevice = null;
    private Context mContext = null; // Currently not used but for further tests may be useful.
    private UiObject mCard = null;
    private UiObject mTitle = null;
    private UiObject mClock = null;
    private Intent mIntent = null;
    private static SysAppTestHelper sysAppTestHelperInstance;

    /**
     * @param mDevice
     * @param mContext
     */
    private SysAppTestHelper(UiDevice mDevice, Context mContext) {
        super();
        this.mDevice = mDevice;
        this.mContext = mContext;
        mIntent = new Intent();
    }

    public static SysAppTestHelper getInstance(UiDevice device, Context context) {
        if (sysAppTestHelperInstance == null) {
            sysAppTestHelperInstance = new SysAppTestHelper(device, context);
        }
        return sysAppTestHelperInstance;
    }

    public void swipeRight() {
        mDevice.swipe(50,
                mDevice.getDisplayHeight() / 2, mDevice.getDisplayWidth() - 25,
                mDevice.getDisplayHeight() / 2, 30); // slow speed
        SystemClock.sleep(SHORT_TIMEOUT);
    }

    public void swipeLeft() {
        mDevice.swipe(mDevice.getDisplayWidth() - 50, mDevice.getDisplayHeight() / 2, 50,
                mDevice.getDisplayHeight() / 2, 30); // slow speed
        SystemClock.sleep(SHORT_TIMEOUT);
    }

    public void swipeUp() {
        mDevice.swipe(mDevice.getDisplayWidth() / 2, mDevice.getDisplayHeight() / 2 + 50,
                mDevice.getDisplayWidth() / 2, 0, 30); // slow speed
        SystemClock.sleep(SHORT_TIMEOUT);
    }

    public void swipeDown() {
        mDevice.swipe(mDevice.getDisplayWidth() / 2, 0, mDevice.getDisplayWidth() / 2,
                mDevice.getDisplayHeight() / 2 + 50, 30); // slow speed
        SystemClock.sleep(SHORT_TIMEOUT);
    }

    public void flingUp() {
        mDevice.swipe(mDevice.getDisplayWidth() / 2, mDevice.getDisplayHeight() / 2 + 50,
                mDevice.getDisplayWidth() / 2, 0, 5); // fast speed
    }

    public void flingDown() {
        mDevice.swipe(mDevice.getDisplayWidth() / 2, 0, mDevice.getDisplayWidth() / 2,
                mDevice.getDisplayHeight() / 2 + 50, 5); // fast speed
        SystemClock.sleep(SHORT_TIMEOUT);
    }

    // Helper method to go back to home screen
    public void goBackHome() throws RemoteException {
        int count = 0;
        mClock = null;
        while (mClock == null && count++ < 5) {
            mDevice.sleep();
            SystemClock.sleep(LONG_TIMEOUT);
            mDevice.wakeUp();
            SystemClock.sleep(SHORT_TIMEOUT + SHORT_TIMEOUT);
            mClock = mDevice.findObject(CLOCK_SELECTOR); // Ensure device is really on Home screen
        }
    }

    // Helper method to verify if there are any Demo cards.

    // TODO: Allow user to pass in how many cards are expected to find cause some tests may require
    // more than one card.
    public void hasDemoCards() throws Exception {
        // Device should be pre-loaded with demo cards.
        // Start the intent to go to home screen
        mCard = mDevice.findObject(CARD_SELECTOR);
        mTitle = mDevice.findObject(TITLE_SELECTOR);
        mClock = mDevice.findObject(CLOCK_SELECTOR);

        if (mClock.waitForExists(NEW_CARD_TIMEOUT_MS)) {
            mClock.swipeUp(CARD_SWIPE_STEPS);
        }

        // First card from the pre-loaded demo cards could be either in peek view
        // or in full view(e.g Dory) or no peek view(Sturgeon). Ensure to check for demo cards
        // existence in
        // both cases.
        Assert.assertTrue("no cards available for testing",
                (mCard.waitForExists(NEW_CARD_TIMEOUT_MS)
                        || mTitle.waitForExists(NEW_CARD_TIMEOUT_MS)));
    }

    public void launchActivity(String appPackage, String activityToLaunch) {
        mIntent.setAction("android.intent.action.MAIN");
        mIntent.setComponent(new ComponentName(appPackage, activityToLaunch));
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(mIntent);
    }

}
