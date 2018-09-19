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

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Base class to unit test the logic for {@link Iterate}
 */
public abstract class IterateTestBase<T> {
    protected static final String ITERATIONS_OPTION_NAME = "iterations";

    /**
     * Unit test the iteration count is respected.
     */
    @Test
    public void testIterationsRespected() {
        int expectedIterations = 25;
        // Construct the input list of integers and apply the iterate function.
        List<Integer> input = IntStream.range(1, 10).boxed().collect(Collectors.toList());
        List<Integer> output = getIterate().apply(getArguments(expectedIterations), input);
        // Count occurrences of each integer into a map.
        Map<Integer, Long> countMap = output.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        // Ensure each of the integers have N entries.
        boolean respected = countMap.entrySet().stream()
                .noneMatch(entry -> (entry.getValue() != expectedIterations));
        assertThat(respected).isTrue();
    }

    protected abstract IterateBase<T, Integer> getIterate();

    protected abstract T getArguments(int iterations);
}
