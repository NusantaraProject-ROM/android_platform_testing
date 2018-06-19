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

package com.android.server.wm.flicker;

import android.platform.helpers.IAppHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.Suppress;
import android.support.test.uiautomator.UiDevice;
import android.util.Rational;
import android.view.Surface;

import org.junit.Test;

/**
 * Tests to help debug individual transitions, capture video recordings and create test cases.
 */
@Suppress
public class DebugTest {
    private IAppHelper testApp = new StandardAppHelper(InstrumentationRegistry.getInstrumentation(),
            "com.android.server.wm.flicker.testapp", "SimpleApp");
    private UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

    /**
     * atest FlickerTest:DebugTest#openAppCold
     */
    @Test
    public void openAppCold() {
        CommonTransitions.getOpenAppCold(testApp, uiDevice).recordAllRuns().build().run();
    }

    /**
     * atest FlickerTest:DebugTest#openAppWarm
     */
    @Test
    public void openAppWarm() {
        CommonTransitions.openAppWarm(testApp, uiDevice).recordAllRuns().build().run();
    }

    /**
     * atest FlickerTest:DebugTest#changeOrientationFromNaturalToLeft
     */
    @Test
    public void changeOrientationFromNaturalToLeft() {
        CommonTransitions.changeAppRotation(testApp, uiDevice, Surface.ROTATION_0,
                Surface.ROTATION_270).recordAllRuns().build().run();
    }

    /**
     * atest FlickerTest:DebugTest#closeAppWithBackKey
     */
    @Test
    public void closeAppWithBackKey() {
        CommonTransitions.closeAppWithBackKey(testApp, uiDevice).recordAllRuns().build().run();
    }

    /**
     * atest FlickerTest:DebugTest#closeAppWithHomeKey
     */
    @Test
    public void closeAppWithHomeKey() {
        CommonTransitions.closeAppWithHomeKey(testApp, uiDevice).recordAllRuns().build().run();
    }

    /**
     * atest FlickerTest:DebugTest#openAppToSplitScreen
     */
    @Test
    public void openAppToSplitScreen() {
        CommonTransitions.appToSplitScreen(testApp, uiDevice).includeJankyRuns().recordAllRuns()
                .build().run();
    }

    /**
     * atest FlickerTest:DebugTest#splitScreenToLauncher
     */
    @Test
    public void splitScreenToLauncher() {
        CommonTransitions.splitScreenToLauncher(testApp,
                uiDevice).includeJankyRuns().recordAllRuns()
                .build().run();
    }

    /**
     * atest FlickerTest:DebugTest#resizeSplitScreen
     */
    @Test
    public void resizeSplitScreen() {
        IAppHelper bottomApp = new StandardAppHelper(InstrumentationRegistry.getInstrumentation(),
                "com.android.server.wm.flicker.testapp", "ImeApp");
        CommonTransitions.resizeSplitScreen(testApp, bottomApp, uiDevice, new Rational(1, 3),
                new Rational(2, 3)).includeJankyRuns().recordEachRun().build().run();
    }

    // IME tests

    /**
     * atest FlickerTest:DebugTest#editTextSetFocus
     */
    @Test
    public void editTextSetFocus() {
        CommonTransitions.editTextSetFocus(uiDevice).includeJankyRuns().recordEachRun()
                .build().run();
    }

    /**
     * atest FlickerTest:DebugTest#editTextLoseFocusToHome
     */
    @Test
    public void editTextLoseFocusToHome() {
        CommonTransitions.editTextLoseFocusToHome(uiDevice).includeJankyRuns().recordEachRun()
                .build().run();
    }

    /**
     * atest FlickerTest:DebugTest#editTextLoseFocusToApp
     */
    @Test
    public void editTextLoseFocusToApp() {
        CommonTransitions.editTextLoseFocusToHome(uiDevice).includeJankyRuns().recordEachRun()
                .build().run();
    }

    // PIP tests

    /**
     * atest FlickerTest:DebugTest#enterPipMode
     */
    @Test
    public void enterPipMode() {
        CommonTransitions.enterPipMode(uiDevice).includeJankyRuns().recordEachRun().build().run();
    }

    /**
     * atest FlickerTest:DebugTest#exitPipModeToHome
     */
    @Test
    public void exitPipModeToHome() {
        CommonTransitions.exitPipModeToHome(uiDevice).includeJankyRuns().recordEachRun()
                .build().run();
    }

    /**
     * atest FlickerTest:DebugTest#exitPipModeToApp
     */
    @Test
    public void exitPipModeToApp() {
        CommonTransitions.exitPipModeToApp(uiDevice).includeJankyRuns().recordEachRun()
                .build().run();
    }
}
