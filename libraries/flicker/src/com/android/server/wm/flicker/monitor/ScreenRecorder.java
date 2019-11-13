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

package com.android.server.wm.flicker.monitor;

import static com.android.compatibility.common.util.SystemUtil.runShellCommand;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Captures screen contents and saves it as a mp4 video file. */
public class ScreenRecorder implements ITransitionMonitor {
    @VisibleForTesting
    public static final Path DEFAULT_OUTPUT_PATH = OUTPUT_DIR.resolve("transition.mp4");

    private static final String TAG = "FLICKER";
    private int mWidth;
    private int mHeight;
    private Thread mRecorderThread;

    public ScreenRecorder() {
        this(720, 1280);
    }

    public ScreenRecorder(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @VisibleForTesting
    public static Path getPath(String testTag) {
        return OUTPUT_DIR.resolve(testTag + ".mp4");
    }

    @Override
    public void start() {
        OUTPUT_DIR.toFile().mkdirs();
        String command =
                String.format(
                        Locale.getDefault(),
                        "screenrecord --size %dx%d %s",
                        mWidth,
                        mHeight,
                        DEFAULT_OUTPUT_PATH);
        mRecorderThread =
                new Thread(
                        () -> {
                            try {
                                Runtime.getRuntime().exec(command);
                            } catch (IOException e) {
                                Log.e(TAG, "Error executing " + command, e);
                            }
                        });
        mRecorderThread.start();
    }

    @Override
    public void stop() {
        runShellCommand("killall -s 2 screenrecord");
        try {
            mRecorderThread.join();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Override
    public Path save(String testTag) {
        if (!Files.exists(DEFAULT_OUTPUT_PATH)) {
            Log.w(TAG, "No video file found on " + DEFAULT_OUTPUT_PATH);
            return null;
        }

        try {
            Path targetPath = Files.move(DEFAULT_OUTPUT_PATH, getPath(testTag), REPLACE_EXISTING);
            Log.i(TAG, "Video saved to " + targetPath.toString());
            return targetPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
