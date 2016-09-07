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

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.platform.test.helpers.CommandHelper;
import android.platform.test.helpers.DPadHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.launcherhelper.ILeanbackLauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.launcherhelper.LeanbackLauncherStrategy;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import org.junit.runner.RunWith;

/**
 * Base test class for functional testing for leanback platform
 */
@RunWith(AndroidJUnit4.class)
public abstract class SysUiTestBase {

    private static final String TAG = SysUiTestBase.class.getSimpleName();

    protected UiDevice mDevice;
    protected Instrumentation mInstrumentation;
    protected Context mContext;
    protected Bundle mArguments;

    protected CommandHelper mCmdHelper;
    protected DPadHelper mDPadHelper;
    protected ILeanbackLauncherStrategy mLauncherStrategy;

    public SysUiTestBase() {
        initialize(InstrumentationRegistry.getInstrumentation());
    }

    public SysUiTestBase(Instrumentation instrumentation) {
        initialize(instrumentation);
    }

    private void initialize(Instrumentation instrumentation) {
        // Initialize instances of testing support library
        mInstrumentation = instrumentation;
        mContext = getInstrumentation().getContext();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mArguments = InstrumentationRegistry.getArguments();

        // Initialize instances of leanback and app helpers
        ILeanbackLauncherStrategy launcherStrategy = LauncherStrategyFactory.getInstance(
                mDevice).getLeanbackLauncherStrategy();
        if (launcherStrategy instanceof LeanbackLauncherStrategy) {
            mLauncherStrategy = (LeanbackLauncherStrategy) launcherStrategy;
        }
        mCmdHelper = new CommandHelper(getInstrumentation());
        mDPadHelper = DPadHelper.getInstance(getInstrumentation());
    }

    protected Instrumentation getInstrumentation() {
        return mInstrumentation;
    }

    protected int getArgumentsAsInt(String key, int defaultValue) {
        String stringValue = mArguments.getString(key);
        if (stringValue != null) {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                Log.w(TAG, String.format("Unable to parse arg %s with value %s to a integer.",
                        key, stringValue), e);
            }
        }
        return defaultValue;
    }

    protected boolean getArgumentsAsBoolean(String key, boolean defaultValue) {
        String stringValue = mArguments.getString(key);
        if (stringValue != null) {
            try {
                return Boolean.parseBoolean(stringValue);
            } catch (Exception e) {
                Log.w(TAG, String.format("Unable to parse arg %s with value to a boolean.",
                        key, stringValue), e);
            }
        }
        return defaultValue;
    }
}
