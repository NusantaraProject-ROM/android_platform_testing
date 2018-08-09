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

import android.os.Bundle;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A {@link Compose} function for shuffling all objects with an optional seed.
 */
public class Shuffle<T> implements Compose<T> {
    private static final String SHUFFLE_OPTION_NAME = "shuffle";
    private static final boolean SHUFFLE_DEFAULT_VALUE = false;
    private static final String SEED_OPTION_NAME = "seed";

    private final String mShuffleOptionName;
    private final String mSeedOptionName;
    private final boolean mShuffleDefaultValue;

    public Shuffle() {
        this(SHUFFLE_OPTION_NAME);
    }

    public Shuffle(String shuffleOptionName) {
        this(shuffleOptionName, SHUFFLE_DEFAULT_VALUE);
    }

    public Shuffle(String shuffleOptionName, String seedOptionName) {
        this(shuffleOptionName, SHUFFLE_DEFAULT_VALUE, seedOptionName);
    }

    public Shuffle(String shuffleOptionName, boolean shuffleDefaultValue) {
        this(shuffleOptionName, shuffleDefaultValue, SEED_OPTION_NAME);
    }

    public Shuffle(String shuffleOptionName, boolean shuffleDefaultValue, String seedOptionName) {
        mShuffleOptionName = shuffleOptionName;
        mShuffleDefaultValue = shuffleDefaultValue;
        mSeedOptionName = seedOptionName;
    }

    @Override
    public List<T> apply(Bundle args, List<T> input) {
        boolean shuffle =
            Boolean.parseBoolean(
                    args.getString(mShuffleOptionName, String.valueOf(mShuffleDefaultValue)));
        if (shuffle) {
            long seed =
                Long.parseLong(
                        args.getString(mSeedOptionName, String.valueOf(new Random().nextLong())));
            Collections.shuffle(input, new Random(seed));
        }
        return input;
    }
}
