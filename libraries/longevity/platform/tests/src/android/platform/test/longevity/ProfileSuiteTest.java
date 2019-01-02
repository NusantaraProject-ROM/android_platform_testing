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
package android.platform.test.longevity;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import android.app.Instrumentation;
import android.content.Context;
import android.platform.test.scenario.annotation.Scenario;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import org.mockito.Mock;

/** Unit tests for the {@link ProfileSuite} runner. */
@RunWith(JUnit4.class)
public class ProfileSuiteTest {
    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Mock private Instrumentation mInstrumentation;
    @Mock private Context mContext;

    @Before
    public void setUpSuite() throws InitializationError {
        MockitoAnnotations.initMocks(this);
    }

    /** Test that profile suites with classes that aren't scenarios are rejected. */
    @Test
    public void testRejectInvalidTests_notScenarios() throws InitializationError {
        mExpectedException.expect(InitializationError.class);
        new ProfileSuite(NonScenarioSuite.class, new AllDefaultPossibilitiesBuilder(true),
                mInstrumentation, mContext, new Bundle());
    }

    /** Test that profile suites with classes that aren't scenarios are rejected. */
    @Test
    public void testRejectInvalidTests_notSupportedRunner() throws InitializationError {
        mExpectedException.expect(InitializationError.class);
        new ProfileSuite(InvalidRunnerSuite.class, new AllDefaultPossibilitiesBuilder(true),
                mInstrumentation, mContext, new Bundle());
    }

    /** Test that profile suites with classes that have no runner are rejected. */
    @Test
    public void testRejectInvalidTests_badRunnerBuilder() throws Throwable {
        mExpectedException.expect(InitializationError.class);
        RunnerBuilder builder = Mockito.spy(new AllDefaultPossibilitiesBuilder(true));
        when(builder.runnerForClass(BasicScenario.class)).thenThrow(new Throwable("empty"));
        new ProfileSuite(BasicSuite.class, builder, mInstrumentation, mContext, new Bundle());
    }

    /** Test that the basic scenario suite is accepted if properly annotated. */
    @Test
    public void testValidScenario_basic() throws InitializationError {
        new ProfileSuite(BasicSuite.class, new AllDefaultPossibilitiesBuilder(true),
                    mInstrumentation, mContext, new Bundle());

    }

    @RunWith(ProfileSuite.class)
    @SuiteClasses({
        BasicScenario.class,
    })
    public static class BasicSuite { }

    @RunWith(ProfileSuite.class)
    @SuiteClasses({
        NonScenario.class,
    })
    public static class NonScenarioSuite { }

    @RunWith(ProfileSuite.class)
    @SuiteClasses({
        NotSupportedRunner.class,
    })
    public static class InvalidRunnerSuite { }

    @Scenario
    @RunWith(JUnit4.class)
    public static class BasicScenario {
        @Test
        public void testNothing() { }
    }

    // Note: @Scenario annotations are not inherited.
    @RunWith(JUnit4.class)
    public static class NonScenario extends BasicScenario { }

    @Scenario
    @RunWith(Parameterized.class)
    public static class NotSupportedRunner extends BasicScenario { }
}
