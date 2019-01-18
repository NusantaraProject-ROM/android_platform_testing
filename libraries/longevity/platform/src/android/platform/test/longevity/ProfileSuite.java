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

package android.platform.test.longevity;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.platform.test.longevity.proto.Configuration.Scenario;
import androidx.annotation.VisibleForTesting;
import androidx.test.InstrumentationRegistry;

import java.util.function.BiFunction;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * {@inheritDoc}
 *
 * This class is used for constructing longevity suites that run on an Android device based on a
 * profile.
 */
public class ProfileSuite extends LongevitySuite {
    // An arbiturary leeway that hopefully allows the @After and @AfterClass methods of a scenario
    // to finish execution.
    @VisibleForTesting static final long ENDTIME_LEEWAY_MS = 3000;

    private static final String LOG_TAG = ProfileSuite.class.getSimpleName();

    // Profile instance for scheduling tests.
    private Profile mProfile;
    // Suite timeout for timing out the last scenario.
    private long mSuiteTimeout;
    // Cached {@link android.host.test.longevity.listener.TimeoutTerminator} instance.
    private android.host.test.longevity.listener.TimeoutTerminator mTimeoutTerminator;

    /**
     * Called reflectively on classes annotated with {@code @RunWith(LongevitySuite.class)}
     */
    public ProfileSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        this(klass, builder, InstrumentationRegistry.getInstrumentation(),
                InstrumentationRegistry.getContext(), InstrumentationRegistry.getArguments());
    }

    /**
     * Used to pass in mock-able Android features for testing.
     * TODO(b/120508148): write tests for logic specific to profiles.
     */
    @VisibleForTesting
    public ProfileSuite(Class<?> klass, RunnerBuilder builder,
            Instrumentation instrumentation, Context context, Bundle arguments)
            throws InitializationError {
        super(klass, constructClassRunners(klass, builder, arguments), arguments);
        mProfile = new Profile(arguments);
    }

    /**
     * Constructs the sequence of {@link Runner}s using platform composers.
     */
    private static List<Runner> constructClassRunners(
                Class<?> suite, RunnerBuilder builder, Bundle args)
            throws InitializationError {
        // TODO(b/118340229): Refactor to share logic with base class. In the meanwhile, keep the
        // logic here in sync with the base class.
        // Retrieve annotated suite classes.
        SuiteClasses annotation = suite.getAnnotation(SuiteClasses.class);
        if (annotation == null) {
            throw new InitializationError(String.format(
                    "Longevity suite, '%s', must have a SuiteClasses annotation", suite.getName()));
        }
        // Validate that runnable scenarios are passed into the suite.
        for (Class<?> scenario : annotation.value()) {
            Runner runner = null;
            try {
                runner = builder.runnerForClass(scenario);
            } catch (Throwable t) {
                throw new InitializationError(t);
            }
            // All scenarios must be annotated with @Scenario.
            if (!scenario.isAnnotationPresent(
                    android.platform.test.scenario.annotation.Scenario.class)) {
                throw new InitializationError(
                        String.format(
                                "%s is not annotated with @Scenario.",
                                runner.getDescription().getDisplayName()));
            }
            // All scenarios must extend BlockJUnit4ClassRunner.
            if (!(runner instanceof BlockJUnit4ClassRunner)) {
                throw new InitializationError(
                        String.format(
                                "All runners must extend BlockJUnit4ClassRunner. %s:%s doesn't.",
                                runner.getClass(), runner.getDescription().getDisplayName()));
            }
        }
        // Construct and store custom runners for the full suite.
        BiFunction<Bundle, List<Runner>, List<Runner>> modifier = new Profile(args);
        return modifier.apply(args, builder.runners(suite, annotation.value()));
    }

    /** {@inheritDoc} */
    @Override
    public void run(final RunNotifier notifier) {
        // Set the test run start time in the profile composer and sleep until the first scheduled
        // test starts. When no profile is supplied, hasNextScheduledScenario() returns false and
        // no sleep is performed.
        if (mProfile.hasNextScheduledScenario()) {
            mProfile.setTestRunStartTimeMs(System.currentTimeMillis());
            SystemClock.sleep(mProfile.getTimeUntilNextScenarioMs());
        }
        // Register other listeners and continue with standard longevity run.
        super.run(notifier);
    }

    /** {@inheritDoc} */
    @Override
    protected void runChild(Runner runner, final RunNotifier notifier) {
        mProfile.scenarioStarted();
        long timeout =
                mProfile.hasNextScheduledScenario()
                        ? mProfile.getTimeUntilNextScenarioMs()
                        : (getTimeoutTerminator(notifier).getTotalSuiteTimeoutMs()
                                - mProfile.getTimeSinceRunStartedMs());
        super.runChild(
                getScheduledRunner(
                        // Cast is safe as runners have been verified to be BlockJUnit4Runner above.
                        (BlockJUnit4ClassRunner) runner,
                        mProfile.getCurrentScenario(),
                        timeout,
                        mProfile.hasNextScheduledScenario()),
                notifier);
    }

    /**
     * Replace a runner with {@link ScheduledScenarioRunner} for features specific to scheduled
     * profiles.
     */
    @VisibleForTesting
    protected ScheduledScenarioRunner getScheduledRunner(
            BlockJUnit4ClassRunner runner, Scenario scenario, long timeout, boolean shouldIdle) {
        Class<?> testClass = runner.getTestClass().getJavaClass();
        try {
            return new ScheduledScenarioRunner(testClass, scenario, timeout, shouldIdle);
        } catch (InitializationError e) {
            throw new RuntimeException(
                    String.format(
                            "Unable to run scenario %s with a scheduled runner.",
                            runner.getDescription().getDisplayName()),
                    e);
        }
    }
}
