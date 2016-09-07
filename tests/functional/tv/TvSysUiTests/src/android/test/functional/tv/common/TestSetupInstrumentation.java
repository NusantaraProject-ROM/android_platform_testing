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

package android.test.functional.tv.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.test.functional.tv.settings.MultiUserInRestrictedProfileTests;
import android.util.Log;

/**
 * Test setup instrumentation for Functional verification tests
 *
 * adb shell am instrument -w -r \
 * -e restrictedProfile [create | delete | exit] \
 * -e pinCode <4 digit code> \
 * android.test.functional.tv.sysui/android.test.functional.tv.common.TestSetupInstrumentation
 */
public class TestSetupInstrumentation extends Instrumentation {

    private static final String TAG = TestSetupInstrumentation.class.getSimpleName();
    private static final String ARGUMENT_SETUP_MODE = "restrictedProfile";
    private static final String ARGUMENT_PINCODE = "pinCode";
    private static final String SETUP_MODE_CREATE_RESTRICTED_PROFILE = "create";
    private static final String SETUP_MODE_DELETE_RESTRICTED_PROFILE = "delete";
    private static final String SETUP_MODE_EXIT_RESTRICTED_PROFILE = "exit";
    private static final String PIN_CODE = "1010";

    private Bundle mArguments;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        InstrumentationRegistry.registerInstance(this, arguments);
        mArguments = arguments;
        start();
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            setup();
            finish(Activity.RESULT_OK, new Bundle());
        } catch (TestSetupException e) {
            error(e.getMessage());
        }
    }

    private void setup() throws TestSetupException {
        final String setupMode = mArguments.getString(ARGUMENT_SETUP_MODE, "");
        if (setupMode == null) {
            error("Performing no setup actions because " + ARGUMENT_SETUP_MODE
                    + " was not passed as an argument");
        } else {
            Log.i(TAG, "Running setup for " + setupMode + " tests.");
            switch (setupMode) {
                case SETUP_MODE_CREATE_RESTRICTED_PROFILE:
                    createRestrictedProfile();
                    break;
                case SETUP_MODE_DELETE_RESTRICTED_PROFILE:
                    deleteRestrictedProfile();
                    break;
                case SETUP_MODE_EXIT_RESTRICTED_PROFILE:
                    exitRestrictedProfile();
                    break;
                default:
                    throw new TestSetupException(
                            "Unknown " + ARGUMENT_SETUP_MODE + " of " + setupMode);
            }
        }
    }

    private void createRestrictedProfile() throws TestSetupException {
        final String pinCode = mArguments.getString(ARGUMENT_PINCODE, PIN_CODE);
        if (!MultiUserInRestrictedProfileTests.Setup.createRestrictedProfile(this, pinCode, true)) {
            throw new TestSetupException("Failed to create the restricted profile");
        }
    }

    private void deleteRestrictedProfile() throws TestSetupException {
        final String pinCode = mArguments.getString(ARGUMENT_PINCODE, PIN_CODE);
        if (!MultiUserInRestrictedProfileTests.Setup.deleteRestrictedProfile(this, pinCode)) {
            throw new TestSetupException("Failed to delete the restricted profile");
        }
    }

    private void exitRestrictedProfile() throws TestSetupException {
        if (!MultiUserInRestrictedProfileTests.Setup.exitRestrictedProfile(this)) {
            throw new TestSetupException("Failed to exit the restricted profile");
        }
    }

    /**
     * Provide an error message to the instrumentation result
     * @param message
     */
    public void error(String message) {
        Log.e(TAG, String.format("error message=%s", message));
        Bundle output = new Bundle();
        output.putString("error", message);
        finish(Activity.RESULT_CANCELED, output);
    }

    static class TestSetupException extends Exception {
        public TestSetupException(String msg) {
            super(msg);
        }
    }
}

