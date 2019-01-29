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
package android.platform.test.longevity;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static java.lang.Math.abs;

import android.platform.test.longevity.proto.Configuration.Scenario;
import android.platform.test.longevity.proto.Configuration.Scenario.AfterTest;
import android.platform.test.longevity.samples.testing.SampleProfileSuite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestTimedOutException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Unit tests for the {@link ScheduledScenarioRunner} runner. */
@RunWith(JUnit4.class)
public class ScheduledScenarioRunnerTest {

    @Mock private RunNotifier mRunNotifier;

    // Threshold above which missing a schedule is considered a failure.
    private static final long TIMEOUT_ERROR_MARGIN_MS = 500;

    @Before
    public void setUpSuite() throws InitializationError {
        initMocks(this);
    }

    /**
     * Test that an over time test causes a JUnit TestTimedOutException with the correct exception
     * timeout.
     */
    @Test
    public void testOverTimeTest_throwsTestTimedOutException() throws InitializationError {
        ArgumentCaptor<Failure> failureCaptor = ArgumentCaptor.forClass(Failure.class);
        // Set a over time test with a 5-second window that will idle until the end of the window is
        // reached.
        long timeoutMs = TimeUnit.SECONDS.toMillis(5);
        ScheduledScenarioRunner runner =
                setUpSpiedRunner(
                        SampleProfileSuite.LongIdleTest.class,
                        timeoutMs,
                        AfterTest.STAY_IN_APP,
                        true);
        runner.run(mRunNotifier);
        // Verify that a TestTimedOutException is fired and that the timeout is correct.
        verify(mRunNotifier, atLeastOnce()).fireTestFailure(failureCaptor.capture());
        List<Failure> failures = failureCaptor.getAllValues();
        boolean correctTestTimedOutExceptionFired =
                failures.stream()
                        .anyMatch(
                                f -> {
                                    if (!(f.getException() instanceof TestTimedOutException)) {
                                        return false;
                                    }
                                    TestTimedOutException exception =
                                            (TestTimedOutException) f.getException();
                                    long exceptionTimeout =
                                            exception
                                                    .getTimeUnit()
                                                    .toMillis(exception.getTimeout());
                                    long expectedTimeout =
                                            timeoutMs - ScheduledScenarioRunner.ENDTIME_LEEWAY_MS;
                                    return abs(exceptionTimeout - expectedTimeout)
                                            <= TIMEOUT_ERROR_MARGIN_MS;
                                });
        Assert.assertTrue(correctTestTimedOutExceptionFired);
    }

    /** Test that an over time test does not idle before teardown. */
    @Test
    public void testOverTimeTest_doesNotIdleBeforeTeardown() throws InitializationError {
        // Set a over time test with a 5-second window that will idle until the end of the window is
        // reached.
        ScheduledScenarioRunner runner =
                setUpSpiedRunner(
                        SampleProfileSuite.LongIdleTest.class,
                        TimeUnit.SECONDS.toMillis(5),
                        AfterTest.STAY_IN_APP,
                        true);
        runner.run(mRunNotifier);
        // There should not be idle before teardown as the test should not have left itself enough
        // time for that.
        verify(runner, times(0)).performIdleBeforeTeardown(anyLong());
    }

    /** Test that an over time test still idles until tne next scenario is supposed to begin. */
    @Test
    public void testOverTimeTest_idlesAfterTeardownUntilNextScenario() throws InitializationError {
        // Set a over time test with a 5-second window that will idle until the end of the window is
        // reached.
        ScheduledScenarioRunner runner =
                setUpSpiedRunner(
                        SampleProfileSuite.LongIdleTest.class,
                        TimeUnit.SECONDS.toMillis(5),
                        AfterTest.STAY_IN_APP,
                        true);
        runner.run(mRunNotifier);
        // Verify that it still idles until the next scenario; duration should be roughly equal to
        // the leeway set in @{link ScheduledScenarioRunner}.
        verify(runner, times(1))
                .performIdleBeforeNextScenario(
                        getWithinMarginMatcher(
                                ScheduledScenarioRunner.ENDTIME_LEEWAY_MS,
                                TIMEOUT_ERROR_MARGIN_MS));
    }

    /** Test that a test set to stay in the app after the test idles after its @Test method. */
    @Test
    public void testRespectsAfterTestPolicy_stayInApp() throws InitializationError {
        // Set a passing test with a 5-second timeout that will idle after its @Test method and
        // idle until the end of the timeout is reached.
        long timeoutMs = TimeUnit.SECONDS.toMillis(5);
        ScheduledScenarioRunner runner =
                setUpSpiedRunner(
                        SampleProfileSuite.PassingTest.class,
                        timeoutMs,
                        AfterTest.STAY_IN_APP,
                        true);
        runner.run(mRunNotifier);
        // Idles before teardown; duration should be roughly equal to the timeout minus the leeway
        // set in {@link ScheduledScenarioRunner}.
        verify(runner, times(1))
                .performIdleBeforeTeardown(
                        getWithinMarginMatcher(
                                timeoutMs - ScheduledScenarioRunner.ENDTIME_LEEWAY_MS,
                                TIMEOUT_ERROR_MARGIN_MS));
    }

    /** Test that a test set to exit the app after the test does not idle after its @Test method. */
    @Test
    public void testRespectsAfterTestPolicy_exit() throws InitializationError {
        // Set a passing test with a 5-second timeout that does not idle after its @Test method and
        // will idle until the end of the timeout is reached.
        long timeoutMs = TimeUnit.SECONDS.toMillis(5);
        ScheduledScenarioRunner runner =
                setUpSpiedRunner(
                        SampleProfileSuite.PassingTest.class, timeoutMs, AfterTest.EXIT, true);
        runner.run(mRunNotifier);
        // There should not be idle before teardown.
        verify(runner, times(0)).performIdleBeforeTeardown(anyLong());
        // Idles before the next scenario; duration should be roughly equal to the timeout.
        verify(runner, times(1))
                .performIdleBeforeNextScenario(
                        getWithinMarginMatcher(timeoutMs, TIMEOUT_ERROR_MARGIN_MS));
    }

    /** Test that the last test does not have idle after it, regardless of its AfterTest policy. */
    @Test
    public void testLastScenarioDoesNotIdle() throws InitializationError {
        // Set a passing test with a 5-second timeout that is set to idle after its @Test method and
        // but should not idle as it will be the last test in practice.
        ScheduledScenarioRunner runner =
                setUpSpiedRunner(
                        SampleProfileSuite.PassingTest.class,
                        TimeUnit.SECONDS.toMillis(5),
                        AfterTest.STAY_IN_APP,
                        false);
        runner.run(mRunNotifier);
        // There should not be idle of any form.
        verify(runner, times(0)).performIdleBeforeTeardown(anyLong());
        verify(runner, times(0)).performIdleBeforeNextScenario(anyLong());
    }

    /**
     * Helper method to set up a over time test with a 5-second window that will idle until the end
     * of the window is reached.
     */
    private ScheduledScenarioRunner setUpSpiedRunner(
            Class<?> scenarioClass,
            long timeoutMs,
            AfterTest afterTestPolicy,
            boolean hasNextScenario)
            throws InitializationError {
        Scenario scenario =
                Scenario.newBuilder()
                        .setAt("00:00:00")
                        .setJourney(scenarioClass.getCanonicalName())
                        .setAfterTest(afterTestPolicy)
                        .build();
        return spy(
                new ScheduledScenarioRunner(scenarioClass, scenario, timeoutMs, hasNextScenario));
    }

    /**
     * Helper method to get an argument matcher that checks whether the input value is equal to
     * expected value within a margin.
     */
    private long getWithinMarginMatcher(long expected, long margin) {
        return longThat(duration -> abs(duration - expected) <= margin);
    }
}
