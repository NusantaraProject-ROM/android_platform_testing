/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.helpers.tests;

import android.platform.helpers.HelperAccessor;
import android.platform.helpers.ICalendarHelper;
import androidx.test.runner.AndroidJUnit4;

import com.android.helpers.CpuUsageHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Android Unit tests for {@link CpuUsageHelperTest}.
 *
 * To run:
 * atest CollectorsHelperTest:com.android.helpers.tests.CpuUsageHelperTest
 */
@RunWith(AndroidJUnit4.class)
public class CpuUsageHelperTest {

    // Kill the calendar app.
    private static final String KILL_TEST_APP_CMD = "am force-stop com.google.android.calendar";
    // Key prefix used for cpu usage by frequency index.
    private static final String CPU_USAGE_FREQ_PREFIX = "cpu_usage_freq";
    // Key prefix used for cpu usage by package name or uid
    private static final String CPU_USAGE_PKG_UID_PREFIX = "cpu_usage_pkg_or_uid";

    private CpuUsageHelper mCpuUsageHelper = new CpuUsageHelper();
    private HelperAccessor<ICalendarHelper> mHelper =
            new HelperAccessor<>(ICalendarHelper.class);

    @Before
    public void setUp() {
        mCpuUsageHelper = new CpuUsageHelper();
    }

    /**
     * Test successfull cpu usage config.
     */
    @Test
    public void testCpuUsageConfig() throws Exception {
        assertTrue(mCpuUsageHelper.startCollecting());
        assertTrue(mCpuUsageHelper.stopCollecting());
    }

    /**
     * Test cpu usage metrics are collected.
     */
    @Test
    public void testCpuUsageMetrics() throws Exception {
        assertTrue(mCpuUsageHelper.startCollecting());
        mHelper.get().open();
        Map<String, Long> cpuUsage = mCpuUsageHelper.getMetrics();
        assertTrue(cpuUsage.size() > 0);
        assertTrue(mCpuUsageHelper.stopCollecting());
        mHelper.get().exit();
    }

    /**
     * Test atleast one cpu usage per pkg or uid and per preq index is collected.
     */
    @Test
    public void testCpuUsageMetricsKey() throws Exception {
        boolean isFreqIndexPresent = false;
        boolean isPkgorUidPresent = false;
        boolean isFreqUsed = false;
        boolean isUIDUsed = false;
        assertTrue(mCpuUsageHelper.startCollecting());
        mHelper.get().open();
        for (Map.Entry<String, Long> cpuUsageEntry : mCpuUsageHelper.getMetrics().entrySet()) {
            if (cpuUsageEntry.getKey().contains(CPU_USAGE_FREQ_PREFIX)) {
                isFreqIndexPresent = true;
                if (cpuUsageEntry.getValue() > 0) {
                    isFreqUsed = true;
                }
            }
            if (cpuUsageEntry.getKey().contains(CPU_USAGE_PKG_UID_PREFIX)) {
                isPkgorUidPresent = true;
                if (cpuUsageEntry.getValue() > 0) {
                    isUIDUsed = true;
                }
            }
            assertTrue(isFreqIndexPresent && isFreqUsed);
            assertTrue(isPkgorUidPresent && isUIDUsed);
        }
        assertTrue(mCpuUsageHelper.stopCollecting());
        mHelper.get().exit();
    }

}

