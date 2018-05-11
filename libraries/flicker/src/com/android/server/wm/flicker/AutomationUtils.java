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

import static android.system.helpers.OverviewHelper.isRecentsInLauncher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Configurator;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Collection of UI Automation helper functions.
 */
public class AutomationUtils {
    private static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    private static final long FIND_TIMEOUT = 10000;
    private static final long LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout() * 2L;

    public static void wakeUpAndGoToHomeScreen() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry
                .getInstrumentation());
        try {
            device.wakeUp();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        device.pressHome();
    }

    /**
     * Sets {@link android.app.UiAutomation#waitForIdle(long, long)} global timeout to 0 causing
     * the {@link android.app.UiAutomation#waitForIdle(long, long)} function to timeout instantly.
     * This removes some delays when using the UIAutomator library required to create fast UI
     * transitions.
     */
    static void setFastWait() {
        Configurator.getInstance().setWaitForIdleTimeout(0);
    }

    /**
     * Reverts {@link android.app.UiAutomation#waitForIdle(long, long)} to default behavior.
     */
    static void setDefaultWait() {
        Configurator.getInstance().setWaitForIdleTimeout(10000);
    }

    private static boolean isQuickstepEnabled(UiDevice device) {
        return device.findObject(By.res(SYSTEMUI_PACKAGE, "recent_apps")) == null;
    }

    private static void openQuickstep(UiDevice device) {
        if (isQuickstepEnabled(device)) {
            int height = device.getDisplayHeight();
            UiObject2 navBar = device.findObject(By.res(SYSTEMUI_PACKAGE, "navigation_bar_frame"));

            // Swipe from nav bar to 2/3rd down the screen.
            device.swipe(
                    navBar.getVisibleBounds().centerX(), navBar.getVisibleBounds().centerY(),
                    navBar.getVisibleBounds().centerX(), height * 2 / 3,
                    (navBar.getVisibleBounds().centerY() - height * 2 / 3) / 100); // 100 px/step
        } else {
            try {
                device.pressRecentApps();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        BySelector RECENTS = By.res(SYSTEMUI_PACKAGE, "recents_view");

        // use a long timeout to wait until recents populated
        if (device.wait(
                Until.findObject(isRecentsInLauncher()
                        ? getLauncherOverviewSelector(device) : RECENTS),
                10000) == null) {
            fail("Recents didn't appear");
        }
        device.waitForIdle();
    }

    private static BySelector getLauncherOverviewSelector(UiDevice device) {
        return By.res(device.getLauncherPackageName(), "overview_panel");
    }

    private static void longpressRecents(UiDevice device) {
        BySelector recentsSelector = By.res(SYSTEMUI_PACKAGE, "recent_apps");
        UiObject2 recentsButton = device.wait(Until.findObject(recentsSelector), FIND_TIMEOUT);
        assertNotNull("Unable to find recents button", recentsButton);
        recentsButton.click(LONG_PRESS_TIMEOUT);
    }

    static void launchSplitscreen() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        String mLauncherPackage = LauncherStrategyFactory.getInstance(UiDevice.getInstance(
                instrumentation)).getLauncherStrategy().getSupportedLauncherPackage();

        if (isQuickstepEnabled(device)) {
            // Quickstep enabled
            openQuickstep(device);

            BySelector overviewIconSelector = By.res(mLauncherPackage, "icon")
                    .clazz(View.class);
            UiObject2 overviewIcon = device.wait(Until.findObject(overviewIconSelector),
                    FIND_TIMEOUT);
            assertNotNull("Unable to find app icon in Overview", overviewIcon);
            overviewIcon.click();

            BySelector splitscreenButtonSelector = By.text("Split screen");
            UiObject2 splitscreenButton = device.wait(Until.findObject(splitscreenButtonSelector),
                    FIND_TIMEOUT);
            assertNotNull("Unable to find Split screen button in Overview", overviewIcon);
            splitscreenButton.click();
        } else {
            // Classic long press recents
            longpressRecents(device);
        }
    }

    static void exitSplitscreen() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        if (isQuickstepEnabled(device)) {
            // Quickstep enabled
            BySelector dividerSelector = By.res(SYSTEMUI_PACKAGE, "docked_divider_handle");
            UiObject2 divider = device.wait(Until.findObject(dividerSelector), FIND_TIMEOUT);
            assertNotNull("Unable to find Split screen divider", divider);

            // Drag the splitscreen divider to the top of the screen
            divider.drag(new Point(device.getDisplayWidth() / 2, 0), 400);
        } else {
            // Classic longpress recents
            longpressRecents(device);
        }
    }
}
