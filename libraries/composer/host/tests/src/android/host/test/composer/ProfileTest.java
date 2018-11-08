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
package android.host.test.composer;

import android.host.test.composer.profile.Configuration;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.Runner;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit test the logic for host-side {@link Profile}
 */
@RunWith(JUnit4.class)
public class ProfileTest extends ProfileTestBase<Map<String, String>> {
    protected class TestableProfile extends ProfileBase<Map<String, String>> {
        public TestableProfile(Map<String, String> args) {
            super(args);
        }

        @Override
        protected Configuration getConfigurationArgument(Map<String, String> args) {
            return TEST_CONFIGS.get(args.get(PROFILE_OPTION_NAME));
        }
    }

    @Override
    protected ProfileBase<Map<String, String>> getProfile(Map<String, String> args) {
        return new TestableProfile(args);
    }

    @Override
    protected Map<String, String> getArguments(String configName) {
        HashMap<String, String> args = new HashMap<>();
        args.put(PROFILE_OPTION_NAME, configName);
        return args;
    }
}
