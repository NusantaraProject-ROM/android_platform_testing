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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit test the logic for {@link Shuffle}
 */
@RunWith(JUnit4.class)
public class ShuffleTest {
    private static final int NUM_TESTS = 10;
    private static final String SHUFFLE_OPTION_NAME = "shuffle";
    private static final String SEED_OPTION_NAME = "seed";
    private static final long SEED_VALUE = new Random().nextLong();

    private Shuffle<Integer> mShuffle = new Shuffle<>(SHUFFLE_OPTION_NAME, SEED_OPTION_NAME);

    /**
     * Unit test that shuffling with a specific seed is respected.
     */
    @Test
    public void testShuffleSeedRespected()  {
        // Construct argument bundle and input list.
        Bundle args = new Bundle();
        args.putString(SHUFFLE_OPTION_NAME, "true");
        args.putString(SEED_OPTION_NAME, String.valueOf(SEED_VALUE));
        List<Integer> input = IntStream.range(1, NUM_TESTS).boxed().collect(Collectors.toList());
        // Apply shuffler on arguments and runners.
        List<Integer> output = mShuffle.apply(args, input);
        // Shuffle locally against the same seed and compare results.
        Collections.shuffle(input, new Random(SEED_VALUE));
        assertThat(input).isEqualTo(output);
    }
}
