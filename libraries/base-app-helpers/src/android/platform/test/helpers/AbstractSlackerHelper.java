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

package android.platform.test.helpers;

import android.app.Instrumentation;

public abstract class AbstractSlackerHelper extends AbstractStandardAppHelper {

    public AbstractSlackerHelper(Instrumentation instr) {
        super(instr);
    }

    /**
     * Setup expectation: Slacker is on page with playable radio channels
     *
     * This method starts playing one of the playable radio channels.
     * If app is already on channel page, this function starts playing
     * the radio on the current page. If it cannot start streaming a radio
     * channel, it throws an exception.
     */
    public abstract void startAnyChannel();

    /**
     * Setup expectation: Slacker is on channel page
     *
     * This method pauses the audio streaming and returns to main page.
     * If app is already on main page, this function does nothing.  If it
     * cannot stop streamming channel, it throws an exception.
     */
    public abstract void stopChannel();
}
