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

package android.test.util.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

/**
 * An open-source Android account utility used for instrumentation tests.
 */
public class AccountUtil {
    /**
     * @return a list of the registered accounts on the device
     */
    public static Account[] getRegisteredAccounts() {
        Context context = InstrumentationRegistry.getContext();
        return AccountManager.get(context).getAccounts();
    }

    /**
     * @return a Google-registered account, if there is one or more, or null otherwise
     */
    public static String getRegisteredGoogleAccount() {
        Account[] accounts = getRegisteredAccounts();
        for (int i = 0; i < accounts.length; ++i) {
            if (accounts[i].type.equals("com.google")) {
                return accounts[i].name;
            }
        }
        return null;
    }

    /**
     * @return true if there is a Google-registered account on the device
     */
    public static boolean hasRegisteredGoogleAccount() {
        return (getRegisteredGoogleAccount() != null);
    }
}
