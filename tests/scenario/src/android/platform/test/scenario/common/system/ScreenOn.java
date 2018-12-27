/**
 * Copyright 2018 Google Inc. All Rights Reserved.
 */

package android.platform.test.scenario.common.system;

import android.os.RemoteException;
import android.os.SystemClock;
import android.platform.test.scenario.annotation.Scenario;
import android.support.test.uiautomator.UiDevice;
import androidx.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Turns the screen on and waits for a specified amount of time.
 */
@Scenario
@RunWith(JUnit4.class)
public class ScreenOn {
    private static final String DURATION_OPTION = "screenOnDurationMs";
    private static final String DURATION_DEFAULT = "1000";

    private long mDurationMs = 0L;
    private UiDevice mDevice;

    @Before
    public void setUp() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
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
    public void testScreenOn() throws RemoteException {
        mDevice.wakeUp();
        SystemClock.sleep(mDurationMs);
    }
}
