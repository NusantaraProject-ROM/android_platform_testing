/*
 * Copyright (C) 2017 The Android Open Source Project
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
package android.support.test.metricshelper;

import android.metrics.LogMaker;
import android.metrics.MetricsReader;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.LinkedList;
import java.util.Queue;

import static junit.framework.Assert.assertTrue;

/**
 * Useful test utilities for metrics tests.
 */
public class MetricsAsserts {

    /**
     * Assert unless there is a log with the matching category and with ACTION type.
     */
    public static void assertHasActionLog(String message, MetricsReader reader, int view) {
        reader.reset();
        boolean found = false;
        while(reader.hasNext()) {
            LogMaker b = reader.next();
            if (b.getType() == MetricsEvent.TYPE_ACTION && b.getCategory() == view) {
                found = true;
            }
        }
        assertTrue(message, found);
    }

    /**
     * Assert unless there is a log with the matching category and with visibility type.
     */
    public static void assertHasVisibilityLog(String message, MetricsReader reader,
            int view, boolean visible) {
        int type = visible ? MetricsEvent.TYPE_OPEN : MetricsEvent.TYPE_CLOSE;
        reader.reset();
        boolean found = false;
        while(reader.hasNext()) {
            LogMaker b = reader.next();
            if (b.getType() == type && b.getCategory() == view) {
                found = true;
            }
        }
        assertTrue(message, found);
    }

    /**
     * @returns logs that have at least all the matching fields in the template.
     */
    public static Queue<LogMaker> findMatchinLog(MetricsReader reader, LogMaker template) {
        LinkedList<LogMaker> logs = new LinkedList<>();
        if (template == null) {
            return logs;
        }
        reader.reset();
        while(reader.hasNext()) {
            LogMaker b = reader.next();
            if (template.isSubsetOf(b)) {
                logs.push(b);
            }
        }
        return logs;
    }

    /**
     * Assert unless there is at least one  log that matches the template.
     */
    public static void assertHasLog(String message, MetricsReader reader, LogMaker expected) {
        assertTrue(message, !findMatchinLog(reader, expected).isEmpty());
    }
}
