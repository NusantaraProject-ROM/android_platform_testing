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

package android.platform.test.helpers.common.test;

import android.platform.test.helpers.HelperManager;
import android.platform.test.helpers.AbstractStandardAppHelper;
import android.platform.test.helpers.listeners.FailureTestWatcher;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.system.helpers.PackageHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import java.time.Duration;

import com.android.permissionutils.GrantPermissionUtil;

/**
 * A base-class for testing app helper implementations.
 *
 * @param T the helper interface under test.
 */
public abstract class HelperTest<T extends AbstractStandardAppHelper> {
    // Global 5-minute test timeout
    @Rule
    public final TestRule timeout = Timeout.millis(Duration.ofMinutes(5).toMillis());

    // Global screenshot capture on failures
    public FailureTestWatcher watcher = new FailureTestWatcher();

    @Rule
    public FailureTestWatcher setUpWatcher() {
        watcher.setHelper(getHelper());
        return watcher;
    }

    protected UiDevice mDevice;
    protected T mHelper;

    @Before
    public void setUp() {
        // Clear the application's state.
        PackageHelper.getInstance(InstrumentationRegistry.getInstrumentation())
                .cleanPackage(getHelper().getPackage());
        // Re-grant cleared runtime permissions.
        GrantPermissionUtil.grantAllPermissions(InstrumentationRegistry.getContext());
        // Open the application.
        getHelper().open();
    }

    @After
    public void tearDown() {
        // Exit the application.
        getHelper().exit();
    }

    /**
     * @return the connected @{code UiDevice}
     */
    protected UiDevice getDevice() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }

        return mDevice;
    }

    /**
     * @return an implementation for {@code T}
     */
    protected T getHelper() {
        if (mHelper == null) {
            mHelper = HelperManager.getInstance(
                    InstrumentationRegistry.getContext(),
                    InstrumentationRegistry.getInstrumentation())
                        .get(getHelperClass());
        }

        return mHelper;
    }

    protected abstract Class<T> getHelperClass();
}
