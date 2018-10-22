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
package android.platform.test.rule;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test the logic for {@link GarbageCollectRule}
 */
@RunWith(JUnit4.class)
public class GarbageCollectRuleTest {
    /**
     * Tests that this rule will fail to register if no apps are supplied.
     */
    @Test
    public void testNoAppToGcFails() {
        try {
            GarbageCollectRule rule = new GarbageCollectRule();
            fail("An initialization error should have been thrown, but wasn't.");
        } catch (InitializationError e) {
            return;
        }
    }

    /**
     * Tests that this rule will gc one app before the test, if supplied.
     */
    @Test
    public void testOneAppToGc() throws Throwable {
        TestableGarbageCollectRule rule = new TestableGarbageCollectRule("package.name1");
        rule.apply(rule.getTestStatement(), Description.createTestDescription("clzz", "mthd"))
                .evaluate();
        assertThat(rule.getOperations()).containsExactly(
                "pidof package.name1", "kill -10 1", "test")
            .inOrder();
    }

    /**
     * Tests that this rule will gc multiple apps before the test, if supplied.
     */
    @Test
    public void testMultipleAppsToGc() throws Throwable {
        TestableGarbageCollectRule rule = new TestableGarbageCollectRule(
                "package.name1",
                "package.name2",
                "package.name3");
        rule.apply(rule.getTestStatement(), Description.createTestDescription("clzz", "mthd"))
                .evaluate();
        assertThat(rule.getOperations()).containsExactly(
                "pidof package.name1",
                "kill -10 1",
                "pidof package.name2",
                "kill -10 2",
                "pidof package.name3",
                "kill -10 3",
                "test")
            .inOrder();
    }

    /**
     * Tests that this rule will skip unavailable apps, if supplied.
     */
    @Test
    public void testSkipsGcOnDneApp() throws Throwable {
        TestableGarbageCollectRule rule = new TestableGarbageCollectRule(
                "does.not.exist", "package.name1");
        rule.apply(rule.getTestStatement(), Description.createTestDescription("clzz", "mthd"))
                .evaluate();
        assertThat(rule.getOperations()).containsExactly(
                "pidof does.not.exist",
                "pidof package.name1",
                "kill -10 1",
                "test")
            .inOrder();
    }

    private static class TestableGarbageCollectRule extends GarbageCollectRule {
        private List<String> mOperations = new ArrayList<>();
        private int mPidCounter = 0;

        public TestableGarbageCollectRule(String app) {
            super(app);
        }

        public TestableGarbageCollectRule(String... apps) {
            super(apps);
        }

        @Override
        protected String executeShellCommand(String cmd) {
            mOperations.add(cmd);
            if (cmd.startsWith("pidof package")) {
                mPidCounter++;
                return String.valueOf(mPidCounter);
            } else {
                return "";
            }
        }

        public List<String> getOperations() {
            return mOperations;
        }

        public Statement getTestStatement() {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    mOperations.add("test");
                }
            };
        }
    }
}
