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

package com.android.androidbvt;

import android.content.Context;
import android.media.AudioManager;
import android.support.test.InstrumentationRegistry;
import android.test.suitebuilder.annotation.MediumTest;
import junit.framework.TestCase;

/**
 * Verify setting vol = 0, device goes into vibrate mode
 */
public class SysMediaTests extends TestCase {
    private Context mContext = null;
    private AudioManager mAudioManager = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mContext = InstrumentationRegistry.getTargetContext();
        mAudioManager = (AudioManager) mContext.getSystemService(mContext.AUDIO_SERVICE);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies setting volume to 0, vibration turns on Ringer Volume icon changes to vibration icon
     * in Sound Settings
     * @throws InterruptedException
     */
    @MediumTest
    public void testVolumeMuteAndVibrate() throws InterruptedException {
        assertTrue(mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_VIBRATE);
        assertTrue(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }
}
