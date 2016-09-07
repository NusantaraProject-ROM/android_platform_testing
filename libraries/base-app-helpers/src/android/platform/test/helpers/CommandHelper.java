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
 * limitations under the License
 */

package android.platform.test.helpers;

import android.app.Instrumentation;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import java.io.IOException;

/**
 * Utility class to execute the shell command.
 */
public final class CommandHelper {
    private static final String TAG = CommandHelper.class.getSimpleName();

    private UiDevice mDevice;


    public CommandHelper(Instrumentation instrumentation) {
        mDevice = UiDevice.getInstance(instrumentation);
    }

    public void executeAmStackMovetask(int taskId, int stackId) {
        executeShellCommand(
                String.format("am stack movetask %d %d true", taskId, stackId));
    }

    public String executeAmStackInfo(int stackId) {
        return executeShellCommand(String.format("am stack info %d", stackId));
    }

    public String executeAmStackList() {
        return executeShellCommand("am stack list");
    }

    public String executeDumpsysMediaSession() {
        return executeShellCommand("dumpsys media_session");
    }

    public String executeGetProp(String prop) {
        return executeShellCommand(String.format("getprop %s", prop), true);
    }

    public String executeShellCommand(String command) {
        return executeShellCommand(command, false);
    }

    private String executeShellCommand(String command, boolean trim) {
        try {
            String output = mDevice.executeShellCommand(command);
            return output.trim();
        } catch (IOException e) {
            // ignore
            Log.w(TAG, String.format("The shell command failed to run: %s exception: %s",
                    command, e.getMessage()));
            return "";
        }
    }
}
