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

import org.junit.runner.Description;
import org.junit.runner.Runner;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link Compose} function base class for taking a profile and returning the tests in the specified sequence.
 */
public abstract class ProfileBase<T> implements Compose<T, Runner> {
    protected static final String PROFILE_OPTION_NAME = "profile";
    protected static final String PROFILE_EXTENSION = ".pb";

    private static final String LOG_TAG = ProfileBase.class.getSimpleName();

    // Store the configuration to be read by the test runner.
    private Configuration mConfiguration;

    // Comparator for sorting timstamped CUJs.
    private static class ScenarioTimestampComparator implements Comparator<Scenario> {
        public int compare(Scenario s1, Scenario s2) {
            if (! (s1.hasAt() && s2.hasAt())) {
                throw new IllegalArgumentException(
                      "Scenarios in scheduled profiles must have timestamps.");
            }
            return s1.getAt().compareTo(s2.getAt());
        }
    }

    @Override
    public List<Runner> apply(T args, List<Runner> input) {
        mConfiguration = getConfigurationArgument(args);
        if (mConfiguration == null) {
            return input;
        }
        return getTestSequenceFromConfiguration(mConfiguration, input);
    }

    public boolean isConfigurationLoaded() {
        return (mConfiguration == null);
    }

    public Configuration getLoadedConfiguration() {
        return mConfiguration;
    }

    protected List<Runner> getTestSequenceFromConfiguration(
            Configuration config, List<Runner> input) {
        Map<String, Runner> nameToRunner =
                input.stream().collect(
                        Collectors.toMap(
                                r -> r.getDescription().getDisplayName(), Function.identity()));
        logInfo(LOG_TAG, String.format(
                "Available scenarios: %s",
                nameToRunner.keySet().stream().collect(Collectors.joining(", "))));
        List<Scenario> scenarios = new ArrayList<Scenario>(config.getScenariosList());
        if (config.hasScheduled()) {
            Collections.sort(scenarios, new ScenarioTimestampComparator());
        }
        List<Runner> result = scenarios
                .stream()
                .map(Configuration.Scenario::getJourney)
                .map(
                        journeyName -> {
                            if (nameToRunner.containsKey(journeyName)) {
                                return nameToRunner.get(journeyName);
                            } else {
                                throw new IllegalArgumentException(
                                        String.format(
                                                "Journey %s in profile does not exist.",
                                                journeyName));
                            }
                        })
                .collect(Collectors.toList());
        logInfo(LOG_TAG, String.format(
                "Returned runners: %s",
                result.stream()
                            .map(Runner::getDescription)
                            .map(Description::getDisplayName)
                            .collect(Collectors.toList())));
        return result;
    }

    /**
     * Parses the arguments, reads the configuration file and returns the Configuraiton object.
     *
     * If no profile option is found in the arguments, function should return null, in which case
     * the input sequence is returned without modification. Otherwise, function should parse the
     * profile according to the supplied argument and return the Configuration object or throw an
     * exception if the file is not available or cannot be parsed.
     */
    protected abstract Configuration getConfigurationArgument(T args);

    /**
     * Overrideable, platform-dependent logging function.
     */
    protected void logInfo(String tag, String content) {
        System.out.println(content);
    }
}
