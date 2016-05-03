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
import android.support.test.uiautomator.Direction;

public abstract class AbstractPhotosHelper extends AbstractStandardAppHelper {

    public AbstractPhotosHelper(Instrumentation instr) {
        super(instr);
    }

    /**
     * Setup expectations: Photos is open and on the main screen.
     *
     * This method will select the first clip to open and play. This will block until the clip
     * begins to plays.
     */
    public abstract void openFirstClip();

    /**
     * Setup expectations: Photos is open and a clip is currently playing.
     *
     * This method will pause the current clip and block until paused.
     */
    public abstract void pauseClip();

    /**
     * Setup expectations: Photos is open and a clip is currently paused in the foreground.
     *
     * This method will play the current clip and block until it is playing.
     */
    public abstract void playClip();

    /**
     * Setup expectations: Photos is open.
     *
     * This method will go to the main screen.
     */
    public abstract void goToMainScreen();

    /**
     * Setup expectations: Photos is open and on the main screen.
     *
     * This method will open the picture at the specified index.
     *
     * @param index The index of the picture to open
     */
    public abstract void openPicture(int index);

    /**
     * Setup expectations: Photos is open and a picture album is open.
     *
     * This method will scroll the picture album in the specified direction.
     *
     * @param direction The direction to scroll, must be LEFT or RIGHT.
     */
    public abstract void scrollAlbum(Direction direction);
}
