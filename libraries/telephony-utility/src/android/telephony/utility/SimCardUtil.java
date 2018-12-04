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
package android.telephony.utility;

import android.app.Instrumentation;
import android.content.Context;
import android.device.collectors.util.SendToInstrumentation;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Instrumentation test that allows to get some telephony states.
 * <p>If TelephonyManager does not exists or is not supported, a test failure will be reported
 */
@RunWith(AndroidJUnit4.class)
public class SimCardUtil {

    private static final String SIM_STATE = "sim_state";
    private static final String CARRIER_PRIVILEGES = "has_carried_privileges";

    @Test
    public void getSimCardInformation() throws Exception {
        // Context of the app under test.
        Context context = InstrumentationRegistry.getTargetContext();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        assertNotNull(tm);

        Bundle returnBundle = new Bundle();
        // Sim card - SIM_STATE_READY 5
        int state = tm.getSimState();
        returnBundle.putInt(SIM_STATE, state);

        // UICC check
        boolean carrierPrivileges = tm.hasCarrierPrivileges();
        returnBundle.putBoolean(CARRIER_PRIVILEGES, carrierPrivileges);

        SendToInstrumentation.sendBundle(instrumentation, returnBundle);
    }
}
