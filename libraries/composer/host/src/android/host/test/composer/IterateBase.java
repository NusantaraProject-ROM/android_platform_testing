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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Compose} function base class for repeating objects a configurable number of times.
 */
public abstract class IterateBase<T, U> implements Compose<T, U> {
    protected static final String ITERATIONS_OPTION_NAME = "iterations";
    protected static final int ITERATIONS_DEFAULT_VALUE = 1;

    protected final int mDefaultValue;

    public IterateBase() {
        this(ITERATIONS_DEFAULT_VALUE);
    }

    public IterateBase(int defaultIterations) {
        mDefaultValue = defaultIterations;
    }

    @Override
    public List<U> apply(T args, List<U> input) {
        int iterations = getIterationsArgument(args);
        return Collections.nCopies(iterations, input)
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /** Returns the number of iterations to run from {@code args}. */
    protected abstract int getIterationsArgument(T args);
}
