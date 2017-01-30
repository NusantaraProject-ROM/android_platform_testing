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

package com.android.systemmetrics.functional;

import android.content.Context;
import android.content.Intent;
import android.metrics.LogMaker;
import android.metrics.MetricsReader;
import android.os.SystemClock;
import android.support.test.metricshelper.MetricsAsserts;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.text.TextUtils;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.Queue;

public class AppStartTests extends InstrumentationTestCase {
    private static final String LOG_TAG = AppStartTests.class.getSimpleName();
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final int LONG_TIMEOUT_MS = 2000;
    private UiDevice mDevice = null;
    private Context mContext;
    private MetricsReader mMetricsReader;
    private int mPreUptime;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mContext = getInstrumentation().getContext();
        mDevice.setOrientationNatural();
        mMetricsReader = new MetricsReader();
        mMetricsReader.checkpoint(); // clear out old logs
        mPreUptime = (int) (SystemClock.uptimeMillis() / 1000);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        mDevice.unfreezeRotation();
        mDevice.pressHome();
    }

    @MediumTest
    public void testStartAop() throws Exception {
        Context context = getInstrumentation().getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(SETTINGS_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear out any previous instances

        assertNotNull("component name is null", intent.getComponent());
        String shortName = intent.getComponent().flattenToShortString();
        assertTrue("shortName is empty", !TextUtils.isEmpty(shortName));

        context.startActivity(intent);
        mDevice.wait(Until.hasObject(By.pkg(SETTINGS_PACKAGE).depth(0)), LONG_TIMEOUT_MS);

        int postUptime = (int) (SystemClock.uptimeMillis() / 1000);

        Queue<LogMaker> startLogs = MetricsAsserts.findMatchinLog(mMetricsReader,
                new LogMaker(MetricsEvent.APP_TRANSITION));
        assertTrue("list of start logs is empty", !startLogs.isEmpty());

        boolean found = false;
        for (LogMaker log : startLogs) {
            Object componentObject = log.getTaggedData(MetricsEvent.APP_TRANSITION_COMPONENT_NAME);
            if (shortName.equals(componentObject)) {
                found = true;
                // not sure how to force this to cold/warm for the test
                assertNotNull("log should have an opinion about cold/warm",
                        log.getTaggedData(MetricsEvent.APP_TRANSITION_PROCESS_RUNNING));

                int startUptime = ((Number)
                        log.getTaggedData(MetricsEvent.APP_TRANSITION_DEVICE_UPTIME_SECONDS))
                        .intValue();
                assertTrue("reported uptime should be after the app was started",
                        mPreUptime <= startUptime);
                assertTrue("reported uptime should be before assertion time",
                        startUptime <= postUptime);
            }
        }
        assertTrue("did not find the app start start log for: " + shortName, found);
    }
}
