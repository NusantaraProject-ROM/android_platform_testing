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

import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit test the logic for {@link Iterate}
 */
@RunWith(JUnit4.class)
public class IterateTest {
    private static final int NUM_TESTS = 10;
    private static final int TEST_ITERATIONS = 25;
    private static final String OPTION_NAME = "iterations";

    private Iterate<Integer> mIterate = new Iterate<>(OPTION_NAME);

    /**
     * Unit test the iteration count is respected.
     */
    @Test
    public void testIterationsRespected() {
        // Construct argument bundle and input list.
        Bundle args = new Bundle();
        args.putString(OPTION_NAME, String.valueOf(TEST_ITERATIONS));
        List<Integer> input = IntStream.range(1, NUM_TESTS).boxed().collect(Collectors.toList());
        // Apply iterator on arguments and runners.
        List<Integer> output = mIterate.apply(args, input);
        // Count occurrences of test descriptions into a map.
        Map<Integer, Long> countMap = output.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        // Ensure all test descriptions have N entries.
        boolean respected = countMap.entrySet().stream()
                .noneMatch(entry -> (entry.getValue() != TEST_ITERATIONS));
        assertThat(respected).isTrue();
    }
}
