/*
 * Copyright (C) 2020 The Android Open Source Project
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
 * limitations under the License
 */

package android.platform.test.scenario.sample;

import android.platform.test.microbenchmark.Microbenchmark;

import org.junit.runner.RunWith;

/**
 * A test showcasing the order of execution for different components of a microbenchmark.
 *
 * <p>Run this test with the listener alongside, {@link PrintListener}, to see how they interact.
 */
@RunWith(Microbenchmark.class)
public class SampleMicrobenchmark extends SampleTest {}
