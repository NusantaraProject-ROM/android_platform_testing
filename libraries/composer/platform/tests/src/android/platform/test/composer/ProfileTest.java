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
package android.platform.test.composer;

import android.host.test.composer.profile.Configuration;
import android.host.test.composer.ProfileBase;
import android.host.test.composer.ProfileTestBase;
import android.os.Bundle;

import org.junit.runner.Runner;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit test the logic for device-side {@link Profile}
 */
@RunWith(JUnit4.class)
public class ProfileTest extends ProfileTestBase<Bundle> {
    protected class TestableProfile extends ProfileBase<Bundle> {
        @Override
        protected Configuration getConfigurationArgument(Bundle args) {
            return TEST_CONFIGS.get(args.getString(PROFILE_OPTION_NAME));
        }
    }

    @Override
    protected ProfileBase<Bundle> getProfile() {
        return new TestableProfile();
    }

    @Override
    protected Bundle getArguments(String configName) {
        Bundle args = new Bundle();
        args.putString(PROFILE_OPTION_NAME, configName);
        return args;
    }
}
