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

import android.os.Bundle;

import com.android.internal.logging.LogBuilder;
import com.android.internal.logging.MetricsReader;
import com.android.internal.logging.legacy.LegacyConversionLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import static junit.framework.Assert.assertTrue;

/**
 * Useful test utilities for metrics tests.
 */
public class MetricsAsserts {

    public static void assertHasActionLog(String message, MetricsReader reader, int view) {
        reader.reset();
        boolean found = false;
        while(reader.hasNext()) {
            LogBuilder b = reader.next();
            if (b.getType() == MetricsEvent.TYPE_ACTION && b.getCategory() == view) {
                found = true;
            }
        }
        assertTrue(message, found);
    }
}
