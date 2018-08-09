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
package android.platform.test.microbenchmark;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import android.os.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Unit tests for the {@link Microbenchmark} runner.
 */
@RunWith(JUnit4.class)
public final class MicrobenchmarkTest {
    /**
     * Tests that iterations are respected for microbenchmark tests.
     */
    @Test
    public void testIterationCount() throws InitializationError {
        Bundle args = new Bundle();
        args.putString("iterations", "10");
        Microbenchmark microbench = new Microbenchmark(BasicTest.class, args);
        assertThat(microbench.testCount()).isEqualTo(10);
    }

    public static class BasicTest {
        @Test
        public void doNothingTest() { }
    }

    /**
     * Tests that {@link TightMethodRule}s are ordered properly.
     *
     * <p>@Before --> @Tight Before --> @Test --> @Tight After --> @After.
     */
    @Test
    public void testTightMethodRuleOrder() {
        try {
            Result result = new JUnitCore().run(OrderTest.class);
            assertThat(result.wasSuccessful()).isTrue();
            assertThat(result.getRunCount()).isAtLeast(1);
        } catch (Exception e) {
            fail("This test should not throw.");
        }
    }

    @RunWith(Microbenchmark.class)
    public static class OrderTest {
        @Microbenchmark.TightMethodRule
        public TightRule orderRule = new TightRule();

        private boolean hasCalledBefore = false;
        private boolean hasCalledAfter = false;
        private boolean hasCalledTest = false;

        @Before
        public void beforeMethod() {
            hasCalledBefore = true;
        }

        @Test
        public void testMethod() {
            hasCalledTest = true;
        }

        @After
        public void afterMethod() {
            hasCalledAfter = true;
        }

        class TightRule implements MethodRule {
            @Override
            public Statement apply(Statement base, FrameworkMethod method, Object target) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        // Tight before statement.
                        assertWithMessage("Before was not called before rule evaluation.")
                                .that(hasCalledBefore).isTrue();
                        // Test method evaluation.
                        base.evaluate();
                        // Tight after statement.
                        assertWithMessage("Test not called before base evaluation.")
                                .that(hasCalledTest).isTrue();
                        assertWithMessage("After was called before rule evaluation.")
                                .that(hasCalledAfter).isFalse();
                    }
                };
            }
        }
    }
}
