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
import android.host.test.composer.profile.Configuration.Scenario;
import android.host.test.composer.profile.Configuration.Scheduled;
import android.host.test.composer.profile.Configuration.Scheduled.IfEarly;
import android.host.test.composer.profile.Configuration.Scheduled.IfLate;

import java.util.Map;

/**
 * An extension of {@link android.host.test.composer.ProfileBase} for host-side testing.
 */
public class Profile extends ProfileBase<Map<String, String>> {
    public Profile(Map<String, String> args) {
        super(args);
    }

    @Override
    protected Configuration getConfigurationArgument(Map<String, String> args) {
        if (!args.containsKey(PROFILE_OPTION_NAME)) {
            return null;
        }
        return getStubConfiguration();
    }

    private Configuration getStubConfiguration() {
        return Configuration.newBuilder()
            .setScheduled(
                    Scheduled.newBuilder()
                        .setIfEarly(IfEarly.SLEEP)
                        .setIfLate(IfLate.END))
            .addScenarios(
                    Scenario.newBuilder()
                        .setWeight(0.5)
                        .setJourney("android.platform.test.scenario.calendar.FlingWeekPage")
                )
            .addScenarios(
                    Scenario.newBuilder()
                        .setWeight(0.25)
                        .setJourney("android.platform.test.scenario.calendar.FlingDayPage")
                )
            .addScenarios(
                    Scenario.newBuilder()
                        .setWeight(0.25)
                        .setJourney("android.platform.test.scenario.calendar.FlingWeekPage")
                )
            .build();
    }
}
