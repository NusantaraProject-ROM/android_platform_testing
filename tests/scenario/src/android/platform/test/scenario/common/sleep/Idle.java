/**
 * Copyright 2018 Google Inc. All Rights Reserved.
 */

package android.platform.test.scenario.common.sleep;

import android.os.SystemClock;
import android.platform.test.option.LongOption;
import android.platform.test.scenario.annotation.Scenario;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Performs no actions for a specified amount of time.
 */
@Scenario
@RunWith(JUnit4.class)
public class Idle {
    @Rule public final LongOption mDurationMs = new LongOption("durationMs").setDefault(1000L);

    @Test
    public void testDoingNothing() {
        SystemClock.sleep(mDurationMs.get());
    }
}
