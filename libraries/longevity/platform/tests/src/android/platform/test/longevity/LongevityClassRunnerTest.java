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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoAssertionError;

/** Unit tests for the {@link LongevityClassRunner}. */
public class LongevityClassRunnerTest {
    // A sample test class to test the runner with.
    @RunWith(JUnit4.class)
    public static class SampleTest {
        @BeforeClass
        public static void beforeClassMethod() {}

        @Before
        public void beforeMethod() {}

        @Test
        public void testMethod() {}

        @After
        public void afterMethod() {}

        @AfterClass
        public static void afterClassMethod() {}
    }

    @Mock private RunNotifier mRunNotifier;

    // A failure message for assertion calls in Mockito stubs. These assertion failures will cause
    // the runner under test to fail but will not trigger a test failure directly. This message is
    // used to filter failures reported by the mocked RunNotifier and re-throw the ones injected in
    // the spy.
    private static final String ASSERTION_FAILURE_MESSAGE = "Test assertions failed";

    @Before
    public void setUp() throws InitializationError {
        initMocks(this);
    }

    /**
     * Test that the {@link BeforeClass} methods are added to the test statement as {@link Before}
     * methods.
     */
    @Test
    public void testBeforeClassMethodsAddedAsBeforeMethods() throws Throwable {
        LongevityClassRunner runner = spy(new LongevityClassRunner(SampleTest.class));
        // Spy the withBeforeClasses() method to check that the method does not make changes to the
        // statement despite the presence of a @BeforeClass method.
        doAnswer(
                        invocation -> {
                            Statement returnedStatement = (Statement) invocation.callRealMethod();
                            // If this assertion fails, mRunNofitier will fire a test failure.
                            Assert.assertEquals(
                                    ASSERTION_FAILURE_MESSAGE,
                                    returnedStatement,
                                    invocation.getArgument(0));
                            return returnedStatement;
                        })
                .when(runner)
                .withBeforeClasses(any(Statement.class));
        // Spy the getRunBefores() method to check that the @BeforeClass method is added to the
        // @Before methods.
        doAnswer(
                        invocation -> {
                            List<FrameworkMethod> methodList =
                                    (List<FrameworkMethod>) invocation.getArgument(1);
                            // If any of these assertions fail, mRunNofitier will fire a test
                            // failure.
                            // There should be two methods.
                            Assert.assertEquals(ASSERTION_FAILURE_MESSAGE, methodList.size(), 2);
                            // The first one should be the @BeforeClass one.
                            Assert.assertEquals(
                                    ASSERTION_FAILURE_MESSAGE,
                                    methodList.get(0).getName(),
                                    "beforeClassMethod");
                            // The second one should be the @Before one.
                            Assert.assertEquals(
                                    ASSERTION_FAILURE_MESSAGE,
                                    methodList.get(1).getName(),
                                    "beforeMethod");
                            return invocation.callRealMethod();
                        })
                .when(runner)
                .getRunBefores(any(Statement.class), any(List.class), any(Object.class));
        // Run the runner.
        runner.run(mRunNotifier);
        verifyForAssertionFailures(mRunNotifier);
        // Verify that the stubbed methods are indeed called.
        verify(runner, times(1)).withBeforeClasses(any(Statement.class));
        verify(runner, times(1))
                .getRunBefores(any(Statement.class), any(List.class), any(Object.class));
    }

    /**
     * Test that the {@link AfterClass} methods are added to the test statement as {@link After}
     * methods.
     */
    @Test
    public void testAfterClassMethodsAddedAsAfterMethods() throws Throwable {
        LongevityClassRunner runner = spy(new LongevityClassRunner(SampleTest.class));
        // Spy the withAfterClasses() method to check that the method does not make changes to the
        // statement despite the presence of a @AfterClass method.
        doAnswer(
                        invocation -> {
                            Statement returnedStatement = (Statement) invocation.callRealMethod();
                            // If this assertion fails, mRunNofitier will fire a test failure.
                            Assert.assertEquals(
                                    ASSERTION_FAILURE_MESSAGE,
                                    returnedStatement,
                                    invocation.getArgument(0));
                            return returnedStatement;
                        })
                .when(runner)
                .withAfterClasses(any(Statement.class));
        // Spy the getRunAfters() method to check that the @AfterClass method is added to the
        // @After methods.
        doAnswer(
                        invocation -> {
                            List<FrameworkMethod> methodList =
                                    (List<FrameworkMethod>) invocation.getArgument(1);
                            // If any of these assertions fail, mRunNofitier will fire a test
                            // failure.
                            // There should be two methods.
                            Assert.assertEquals(ASSERTION_FAILURE_MESSAGE, methodList.size(), 2);
                            // The first one should be the @After one.
                            Assert.assertEquals(
                                    ASSERTION_FAILURE_MESSAGE,
                                    methodList.get(0).getName(),
                                    "afterMethod");
                            // The second one should be the @AfterClass one.
                            Assert.assertEquals(
                                    ASSERTION_FAILURE_MESSAGE,
                                    methodList.get(1).getName(),
                                    "afterClassMethod");
                            return invocation.callRealMethod();
                        })
                .when(runner)
                .getRunAfters(any(Statement.class), any(List.class), any(Object.class));
        // Run the runner.
        runner.run(mRunNotifier);
        verifyForAssertionFailures(mRunNotifier);
        // Verify that the stubbed methods are indeed called.
        verify(runner, times(1)).withAfterClasses(any(Statement.class));
        verify(runner, times(1))
                .getRunAfters(any(Statement.class), any(List.class), any(Object.class));
    }

    /**
     * Verify that no test failure is fired because of an assertion failure in the stubbed methods.
     * If the verfication fails, check whether it's due the injected assertions failing. If yes,
     * throw that exception out; otherwise, throw the first exception.
     */
    private void verifyForAssertionFailures(final RunNotifier notifier) throws Throwable {
        try {
            verify(notifier, never()).fireTestFailure(any());
        } catch (MockitoAssertionError e) {
            ArgumentCaptor<Failure> failureCaptor = ArgumentCaptor.forClass(Failure.class);
            verify(notifier, atLeastOnce()).fireTestFailure(failureCaptor.capture());
            List<Failure> failures = failureCaptor.getAllValues();
            // Go through the failures, look for an known failure case from the above exceptions
            // and throw the exception in the first one out if any.
            for (Failure failure : failures) {
                if (failure.getException().getMessage().contains(ASSERTION_FAILURE_MESSAGE)) {
                    throw failure.getException();
                }
            }
            // Otherwise, throw the exception from the first failure reported.
            throw failures.get(0).getException();
        }
    }
}
