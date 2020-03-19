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
 * limitations under the License.
 */

package android.platform.test.rule;

import android.os.SystemClock;
import android.platform.test.rule.DropCachesRule;
import android.util.Log;
import androidx.annotation.VisibleForTesting;

import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

/** This rule toggles iorap compilation for an app, or skips if unspecified. */
public class IorapCompilationRule extends TestWatcher {
    //
    private static final String TAG = IorapCompilationRule.class.getSimpleName();
    // constants
    @VisibleForTesting static final String ARGUMENT_IORAPD_ENABLED = "iorapd-enabled";

    @VisibleForTesting
    static final String IORAP_COMPILE_CMD = "cmd jobscheduler run -f android 283673059";
    @VisibleForTesting
    static final String IORAP_MAINTENANCE_CMD =
            "iorap.cmd.maintenance --purge-package %s /data/misc/iorapd/sqlite.db";
    @VisibleForTesting
    static final String IORAP_DUMPSYS_CMD = "dumpsys iorapd";

    private static final int IORAP_COMPILE_CMD_TIMEOUT_SEC = 600;  // in seconds: 10 minutes
    private static final int IORAP_TRACE_DURATION_TIMEOUT = 7000; // Allow 7s for trace to complete.
    private static final int IORAP_TRIAL_LAUNCH_ITERATIONS = 3;  // min 3 launches to merge traces.

    private static final String DROP_CACHE_SCRIPT = "/data/local/tmp/dropCache.sh";

    // Global static counter. Each junit instrument command must launch 1 package with
    // 1 compiler filter.
    private static int sIterationCounter = 0;
    private String mApplication;

    private enum IorapStatus {
        UNDEFINED,
        ENABLED,
        DISABLED
    }
    private static IorapStatus sIorapStatus = IorapStatus.UNDEFINED;

    private enum IorapCompilationStatus {
        INCOMPLETE,
        COMPLETE,
        INSUFFICIENT_TRACES,
    }

    @VisibleForTesting
    protected static void resetState() {
        sIorapStatus = IorapStatus.UNDEFINED;
        sIterationCounter = 0;
        Log.v(TAG, "resetState");
    }

    public IorapCompilationRule() throws InitializationError {
        throw new InitializationError("Must supply an application to enable iorapd for.");
    }

    public IorapCompilationRule(String application) {
        mApplication = application;
    }

    protected void sleep(int ms) {
        SystemClock.sleep(ms);
    }

    // [[ $(adb shell whoami) == "root" ]]
    protected boolean checkIfRoot() {
        String result = executeShellCommand("whoami");
        return result.contains("root");
    }

    // Delete all db rows and files associated with a package in iorapd.
    // Effectively deletes any raw or compiled trace files, unoptimizing the package in iorap.
    private void purgeIorapPackage(String packageName) {
        if (!checkIfRoot()) {
            throw new IllegalStateException("Must be root to toggle iorapd; try adb root?");
        }

        executeShellCommand("stop iorapd");
        sleep(100);  // give iorapd enough time to stop.
        executeShellCommand(String.format(IORAP_MAINTENANCE_CMD, packageName));
        Log.v(TAG, "Executed: " + String.format(IORAP_MAINTENANCE_CMD, packageName));
        executeShellCommand("start iorapd");
        sleep(2000);  // give iorapd enough time to start up.
    }

    /**
     * Toggle iorapd-based readahead and trace-collection.
     * If iorapd is already enabled and enable is true, does nothing.
     * If iorapd is already disabled and enable is false, does nothing.
     */
    private void toggleIorapStatus(boolean enable) {
        boolean currentlyEnabled = false;
        Log.v(TAG, "toggleIorapStatus " + Boolean.toString(enable));

        // Do nothing if we are already enabled or disabled.
        if (sIorapStatus == IorapStatus.ENABLED && enable) {
            return;
        } else if (sIorapStatus == IorapStatus.DISABLED && !enable) {
            return;
        }

        if (!checkIfRoot()) {
            throw new IllegalStateException("Must be root to toggle iorapd; try adb root?");
        }

        executeShellCommand("stop iorapd");
        executeShellCommand(String.format("setprop iorapd.perfetto.enable %b", enable));
        executeShellCommand(String.format("setprop iorapd.readahead.enable %b", enable));
        executeShellCommand("start iorapd");
        sleep(2000);  // give enough time for iorapd to start back up.

        if (enable) {
            sIorapStatus = IorapStatus.ENABLED;
        } else {
            sIorapStatus = IorapStatus.DISABLED;
        }
    }

    /**
     * Compile the app package using iorap.cmd.maintenance and return false
     * if the compilation failed for some reason.
     */
    private boolean compileAppForIorap(String appPkgName) {
        executeShellCommand(IORAP_COMPILE_CMD);

        for (int i = 0; i < IORAP_COMPILE_CMD_TIMEOUT_SEC; ++i) {
            IorapCompilationStatus status = waitForIorapCompiled(appPkgName);
            if (status == IorapCompilationStatus.COMPLETE) {
                Log.v(TAG, "compileAppForIorap: complete");
                return true;
            } else if (status == IorapCompilationStatus.INSUFFICIENT_TRACES) {
                Log.v(TAG, "compileAppForIorap: insufficient traces");
                return false;
            } // else INCOMPLETE. keep asking iorapd if it's done yet.
            sleep(1000);
        }

        Log.v(TAG, "compileAppForIorap: timed out");
        return false;
    }

    private IorapCompilationStatus waitForIorapCompiled(String appPkgName) {
        String output = executeShellCommand(IORAP_DUMPSYS_CMD);

        String prevLine = "";
        for (String line : output.split("\n")) {
            // Match the indented VersionedComponentName string.
            // "  com.google.android.deskclock/com.android.deskclock.DeskClock@62000712"
            // Note: spaces are meaningful here.
            if (prevLine.contains("  " + appPkgName) && prevLine.contains("@")) {
                // pre-requisite:
                // Compiled Status: Raw traces pending compilation (3)
                if (line.contains("Compiled Status: Usable compiled trace")) {
                    return IorapCompilationStatus.COMPLETE;
                } else if (line.contains("Compiled Status: ") &&
                        line.contains("more traces for compilation")) {
                    //      Compiled Status: Need 1 more traces for compilation
                    // No amount of waiting will help here because there were
                    // insufficient traces made.
                    return IorapCompilationStatus.INSUFFICIENT_TRACES;
                }
            }
            prevLine = line;
        }
        return IorapCompilationStatus.INCOMPLETE;
    }

    /**
     * The first {@code IORAP_TRIAL_LAUNCH_ITERATIONS} are used for collecting an iorap trace file.
     */
    private boolean isIorapTraceBeingCollected() {
        return sIterationCounter < IORAP_TRIAL_LAUNCH_ITERATIONS;
    }

    private boolean isLastIorapTraceCollection() {
        return sIterationCounter == IORAP_TRIAL_LAUNCH_ITERATIONS - 1;
    }

    private boolean isFirstIorapTraceCollection() {
        return sIterationCounter == 0;
    }

    /**
     * Returns null if iorapd-enabled is unset, otherwise returns
     * the true/false value of iorapd-enabled.
     */
    private Boolean isIorapdEnabled() {
        String value = getArguments().getString(ARGUMENT_IORAPD_ENABLED);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    protected void starting(Description description) {
        // Don't do anything if iorapd-enabled was not set.
        Boolean enabled = isIorapdEnabled();
        if (enabled == null) {
            Log.d(TAG, "Skipping iorapd toggling because 'iorapd-enabled' option is unset.");
            return;
        }
        logStatus("starting");
        // Compile each application in sequence.
        String app = mApplication;

        toggleIorapStatus(enabled);

        if (!enabled) {
            return;
        }

        if (isIorapTraceBeingCollected()) {
            // Purge all iorap traces prior to first run of an application.
            if (isFirstIorapTraceCollection()) {
                purgeIorapPackage(mApplication);
            }

            // We must always drop caches to simulate a cold start if the app
            // launch is going to be used for an iorap-trace collection.
            DropCachesRule.executeDropCaches();
        }
    }

    @Override
    protected void finished(Description description) {
        logStatus("finishing");

        Boolean enabled = isIorapdEnabled();

        if (Boolean.TRUE.equals(enabled) && isIorapTraceBeingCollected()) {
            // wait for slightly more than 5s (iorapd.perfetto.trace_duration_ms) for the
            // trace buffers to complete.
            sleep(IORAP_TRACE_DURATION_TIMEOUT);

            if (isLastIorapTraceCollection()) {
                compileAppForIorap(mApplication);
            }
        }

        logStatus("finished");
        sIterationCounter++;
    }

    private void logStatus(String status) {
        Log.v(TAG, String.format("%s iteration %s for app %s", status, sIterationCounter, mApplication));
    }
}

