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

import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

/**
 * This rule will gc the provided apps before running each test method.
 */
public class GarbageCollectRule extends TestWatcher {
    private final String[] mApplications;

    public GarbageCollectRule() throws InitializationError {
        throw new InitializationError("Must supply an application for garbage collection.");
    }

    public GarbageCollectRule(String... applications) {
        mApplications = applications;
    }

    @Override
    protected void starting(Description description) {
        // Garbage collect each application in sequence.
        for (String app : mApplications) {
            String pidofOutput = executeShellCommand(String.format("pidof %s", app));
            if (!pidofOutput.isEmpty()) {
                executeShellCommand(String.format("kill -10 %s", pidofOutput));
            }
        }
    }
}
