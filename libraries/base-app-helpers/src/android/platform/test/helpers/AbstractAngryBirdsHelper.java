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

public abstract class AbstractAngryBirdsHelper extends AbstractStandardAppHelper {

    public AbstractAngryBirdsHelper(Instrumentation instr) {
        super(instr);
    }

    /**
     * Setup expectation: Angry Birds open and on main menu
     *
     * This method will set up game demo by going to level selection menu
     */
    public abstract void setUpDemo();

    /**
     * Setup expectation: Angry Birds open and on level selection menu
     *
     * This method plays a game demo for demoDurationInMinutes minutes
     * @param demoDurationInMinutes: game demo duration in minutes
     */
    public abstract void playDemo(int demoDurationInMinutes);

    /**
     * Setup expectation: Angry Birds open and on level selection menu
     *
     * This method goes back to the main menu
     */
    public abstract void tearDownDemo();
}
