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
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link Compose} function base class for taking a profile and returning the tests in the
 * specified sequence.
 */
public abstract class ProfileBase<T> implements Compose<T, Runner> {
    protected static final String PROFILE_OPTION_NAME = "profile";
    protected static final String PROFILE_EXTENSION = ".pb";

    private static final String LOG_TAG = ProfileBase.class.getSimpleName();

    // Parser for parsing "at" timestamps in profiles.
    private static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("HH:mm:ss");

    // Keeps track of the current scenario to run.
    private int mScenarioIndex = 0;
    // A list of scenarios in the order that they will be run.
    private List<Scenario> mOrderedScenariosList;
    // Timestamp when the test run starts, defaults to time when the ProfileBase object is
    // constructed. Can be overridden by {@link setTestRunStartTimeMillis}.
    // TODO(b/118843085): Clarify whether timestamps are relative to run start time or device clock.
    private long mRunStartTimeMillis = System.currentTimeMillis();

    public ProfileBase(T args) {
        super();
        // Set the timestamp parser to UTC to get test timstamps as "time elapsed since zero".
        TIMESTAMP_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Load configuration from arguments and stored the list of scenarios sorted according to
        // their timestamps.
        Configuration config = getConfigurationArgument(args);
        if (config == null) {
            return;
        }
        mOrderedScenariosList = new ArrayList<Scenario>(config.getScenariosList());
        if (config.hasScheduled()) {
            Collections.sort(mOrderedScenariosList, new ScenarioTimestampComparator());
        }
    }

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
        Configuration config = getConfigurationArgument(args);
        if (config == null) {
            return input;
        }
        return getTestSequenceFromConfiguration(config, input);
    }

    protected List<Runner> getTestSequenceFromConfiguration(
            Configuration config, List<Runner> input) {
        Map<String, Runner> nameToRunner =
                input.stream().collect(
                        Collectors.toMap(
                                r -> r.getDescription().getDisplayName(), Function.identity()));
        logInfo(LOG_TAG, String.format(
                "Available journeys: %s",
                nameToRunner.keySet().stream().collect(Collectors.joining(", "))));
        List<Runner> result = mOrderedScenariosList
                .stream()
                .map(Configuration.Scenario::getJourney)
                .map(
                        journeyName -> {
                            if (nameToRunner.containsKey(journeyName)) {
                                return nameToRunner.get(journeyName);
                            } else {
                                throw new IllegalArgumentException(
                                        String.format(
                                                "Journey %s in profile not found. "
                                                + "Check logcat to see available journeys.",
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
     * Enables classes using the profile composer to set the test run start time.
     */
    public void setTestRunStartTimeMillis(long timestamp) {
        mRunStartTimeMillis = timestamp;
    }

    /**
     * Called by suite runners to signal that a scenario/test has ended; increments the scenario
     * index.
     */
    public void scenarioEnded() {
        mScenarioIndex += 1;
    }

    /**
     * Returns true if there is a next scheduled scenario to run. If no profile is supplied, returns
     * false.
     */
    public boolean hasNextScheduledScenario() {
        return (mOrderedScenariosList != null) && (mScenarioIndex < mOrderedScenariosList.size());
    }

    /**
     * Returns time in milliseconds until the next scenario.
     */
    public long getMillisecondsUntilNextScenario() {
        Scenario nextScenario = mOrderedScenariosList.get(mScenarioIndex);
        if (nextScenario.hasAt()) {
            try {
                long startTimeMillis = TIMESTAMP_FORMATTER.parse(nextScenario.getAt()).getTime();
                // Time in milliseconds from the start of the test run to the current point in time.
                long currentTimeMillis = System.currentTimeMillis() - mRunStartTimeMillis;
                // If the next test should not start yet, sleep until its start time. Otherwise,
                // start it immediately.
                // TODO(b/118495360): Deal with the IfLate situation.
                if (startTimeMillis > currentTimeMillis) {
                    return startTimeMillis - currentTimeMillis;
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException(
                        String.format("Time %s from scenario %s could not be parsed",
                                nextScenario.getAt(), nextScenario.getJourney()));
            }
        }
        // For non-scheduled profiles (not a priority at this point), simply return 0.
        return 0L;
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
