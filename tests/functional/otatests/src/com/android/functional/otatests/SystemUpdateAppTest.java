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

package com.android.functional.otatests;

import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.platform.test.helpers.SystemUpdateHelperImpl;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests functionality related to the System Update app.
 *
 * Precondition: an OTA must be available to the device.
 */
@RunWith(Parameterized.class)
public class SystemUpdateAppTest {

    private static UiDevice sDevice;
    private static Instrumentation sInstrumentation;
    private static SystemUpdateHelperImpl sHelper;
    private static IUpdateBehaviorStrategy sBehavior;
    private static Semaphore sUpdateCompleteLock;

    private static final String KEY_URGENCY = "update_urgency";

    protected String mUrgency;

    private static class ShutdownReceiver extends BroadcastReceiver {

        private Semaphore mLock;
        ShutdownReceiver(Semaphore variableLock) {
            mLock = variableLock;
            try {
                mLock.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            mLock.release();
        }
    }

    public SystemUpdateAppTest(String urgency) {
        mUrgency = urgency;
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        sInstrumentation = InstrumentationRegistry.getInstrumentation();
        sDevice = UiDevice.getInstance(sInstrumentation);
        sHelper = new SystemUpdateHelperImpl(sInstrumentation);
        sBehavior = getBehaviorFromUpdateType();
        sBehavior.setHelper(sHelper);
        sUpdateCompleteLock = new Semaphore(1, true);
        BroadcastReceiver receiver = new ShutdownReceiver(sUpdateCompleteLock);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);

        HandlerThread ht = new HandlerThread("shutdownReceiver");
        ht.start();
        Handler shutdownHandler = new Handler(ht.getLooper());
        sInstrumentation.getContext().registerReceiver(receiver, filter, null, shutdownHandler);
    }

    @Before
    public void setUp() throws Exception {
        sDevice.pressHome();
    }

    @After
    public void tearDown() throws Exception {
        sDevice.pressHome();
    }

    /**
     * Test that the OTA notification is present, and that the app shows an update available
     * @throws Exception
     */
    @Test
    public void testUpdateIsAvailable() throws Exception {
        // The notification disappears when system update is open, so check for it first
        if (sBehavior.createsNotification()) {
            assertTrue(sHelper.hasOtaNotification());
        }
        sDevice.pressHome();
        Thread.sleep(1000);
        sHelper.open();
        assertTrue(sHelper.isUpdateAvailable());
    }

    /**
     * Test that it's possible to download an OTA by clicking on the notification from
     * the home screen.
     */
    @Test
    public void testDownloadUpdateViaNotification() throws Exception {
        if (!sBehavior.createsNotification()) {
            return;
        }
        sBehavior.interactWithNotification();
        SystemClock.sleep(5000);
        sBehavior.setShouldInstall(false);
        sBehavior.interactWithApp();
        assertTrue(sHelper.isOtaDownloadCompleted());
    }

    private static IUpdateBehaviorStrategy getBehaviorFromUpdateType() {
        UpdateUrgency urgency = getUrgency();
        switch (urgency) {
            case AUTOMATIC:
                throw new UnsupportedOperationException("Only recommended supported currently");
            case MANDATORY:
                throw new UnsupportedOperationException("Only recommended supported currently");
            case RECOMMENDED:
                return new RecommendedBehavior();
            default:
                throw new IllegalArgumentException("Unknown update urgency " + urgency);
        }
    }

    private static UpdateUrgency getUrgency() {
        Bundle args = InstrumentationRegistry.getArguments();
        if (!args.containsKey(KEY_URGENCY)) {
            throw new IllegalArgumentException("This test must be provided an update urgency");
        }
        UpdateUrgency urgency = Enum.valueOf(UpdateUrgency.class, args.getString(KEY_URGENCY));
        return urgency;
    }

    @Parameters(name = "{0}")
    public static Iterable<? extends Object> getUrgencyName() {
        UpdateUrgency urgency = getUrgency();
        return Arrays.asList(urgency.mUrgency);
    }
}
