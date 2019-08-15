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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.Runner;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link HealthRunnerBuilder}. */
@RunWith(JUnit4.class)
public class HealthRunnerBuilderTest {
    // A dummy test class.
    @RunWith(JUnit4.class)
    public static class SampleTest {
        @Test
        public void testMethod() {
            // No-op so passes.
        }
    }

    /** Test that the runner builder returns a {@link LongevityClassRunner}. */
    @Test
    public void testUsesLongevityRunner() throws Throwable {
        Runner runner = (new HealthRunnerBuilder()).runnerForClass(SampleTest.class);
        assertTrue(runner instanceof LongevityClassRunner);
    }
}
