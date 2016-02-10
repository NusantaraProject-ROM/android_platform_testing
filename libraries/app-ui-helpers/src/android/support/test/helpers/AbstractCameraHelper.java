/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.support.test.helpers;

import android.app.Instrumentation;

public abstract class AbstractCameraHelper extends AbstractStandardAppHelper {

    public AbstractCameraHelper(Instrumentation instr) {
        super(instr);
    }

    /**
     * Setup expectations: GoogleCamera is open and idle in video mode.
     *
     * This method will change to camera mode and block until the transition is complete.
     */
    public abstract void goToCameraMode();

    /**
     * Setup expectations: GoogleCamera is open and idle in camera mode.
     *
     * This method will change to video mode and block until the transition is complete.
     */
    public abstract void goToVideoMode();

    /**
     * Setup expectation: in Camera mode with the capture button present.
     *
     * This method will capture a photo and block until the transaction is complete.
     */
    public abstract void capturePhoto();

    /**
     * Setup expectation: in Video mode with the capture button present.
     *
     * This method will capture a video of length timeInMs and block until the transaction is
     * complete.
     * @param time duration of video in milliseconds
     */
    public abstract void captureVideo(long time);
}
