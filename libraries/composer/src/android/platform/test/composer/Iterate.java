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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Compose} function for repeating objects a configurable number of times.
 */
public class Iterate<T> implements Compose<T> {
    private static final String DEFAULT_OPTION_NAME = "iterations";
    private static final int DEFAULT_VALUE = 1;

    private final String mOptionName;
    private final int mDefaultValue;

    public Iterate() {
        this(DEFAULT_OPTION_NAME);
    }

    public Iterate(String optionName) {
        this(optionName, DEFAULT_VALUE);
    }

    public Iterate(String optionName, int defaultValue) {
        mOptionName = optionName;
        mDefaultValue = defaultValue;
    }

    @Override
    public List<T> apply(Bundle args, List<T> input) {
        int iterations = Integer.parseInt(
                args.getString(mOptionName, String.valueOf(mDefaultValue)));
        return Collections.nCopies(iterations, input)
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
