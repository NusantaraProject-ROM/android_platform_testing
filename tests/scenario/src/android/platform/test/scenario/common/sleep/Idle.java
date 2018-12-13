/**
 * Copyright 2018 Google Inc. All Rights Reserved.
 */

package android.platform.test.scenario.common.sleep;

import android.os.SystemClock;
import android.platform.test.scenario.annotation.Scenario;
import androidx.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Performs no actions for a specified amount of time.
 */
@Scenario
@RunWith(JUnit4.class)
public class Idle {
    private static final String DURATION_OPTION = "durationMs";
    private static final String DURATION_DEFAULT = "1000";

    private long mDurationMs = 0L;

    @Before
    public void setUp() {
        String durationMsString = InstrumentationRegistry.getArguments()
                .getString(DURATION_OPTION, DURATION_DEFAULT);
        try {
            mDurationMs = Long.parseLong(durationMsString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Failed to parse option %s: %s", DURATION_OPTION, durationMsString));
        }

    }

    @Test
    public void testDoingNothing() {
        SystemClock.sleep(mDurationMs);
    }
}
