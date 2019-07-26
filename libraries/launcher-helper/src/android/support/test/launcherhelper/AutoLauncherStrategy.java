/*
 * Copyright (C) 2019 The Android Open Source Project
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
package android.support.test.launcherhelper;

import android.app.Instrumentation;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class AutoLauncherStrategy implements IAutoLauncherStrategy {
    private static final String LOG_TAG = AutoLauncherStrategy.class.getSimpleName();
    private static final String CAR_LENSPICKER = "com.android.car.carlauncher";
    private static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    private static final String MEDIA_PACKAGE = "com.android.car.media";
    private static final String RADIO_PACKAGE = "com.android.car.radio";

    private static final long APP_LAUNCH_TIMEOUT = 10000;
    private static final long UI_WAIT_TIMEOUT = 5000;

    private static final BySelector UP_BTN = By.res(CAR_LENSPICKER, "page_up");
    private static final BySelector DOWN_BTN = By.res(CAR_LENSPICKER, "page_down");
    private static final BySelector MEDIA_APP_SWITCH =
            By.res(MEDIA_PACKAGE, "app_switch_container");
    private static final BySelector RADIO_APP_SWITCH =
            By.res(RADIO_PACKAGE, "app_switch_container");
    private static final BySelector QUICK_SETTINGS = By.res(SYSTEM_UI_PACKAGE, "qs");
    private static final BySelector LEFT_HVAC = By.res(SYSTEM_UI_PACKAGE, "hvacleft");
    private static final BySelector RIGHT_HVAC = By.res(SYSTEM_UI_PACKAGE, "hvacright");

    private static final Map<String, BySelector> FACET_MAP =
            Stream.of(new Object[][] {
                { "Home", By.res(SYSTEM_UI_PACKAGE, "home").clickable(true) },
                { "Maps", By.res(SYSTEM_UI_PACKAGE, "maps_nav").clickable(true) },
                { "Media", By.res(SYSTEM_UI_PACKAGE, "music_nav").clickable(true) },
                { "Dial", By.res(SYSTEM_UI_PACKAGE, "phone_nav").clickable(true) },
                { "App Grid", By.res(SYSTEM_UI_PACKAGE, "grid_nav").clickable(true) },
                { "Notification", By.res(SYSTEM_UI_PACKAGE, "notifications").clickable(true) },
                { "Google Assistant", By.res(SYSTEM_UI_PACKAGE, "assist").clickable(true) },
            }).collect(Collectors.toMap(data -> (String) data[0], data -> (BySelector) data[1]));

    protected UiDevice mDevice;
    private Instrumentation mInstrumentation;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSupportedLauncherPackage() {
        return CAR_LENSPICKER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUiDevice(UiDevice uiDevice) {
        mDevice = uiDevice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInstrumentation(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        openHomeFacet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openHomeFacet() {
        openFacet("Home");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openMapsFacet() {
        openFacet("Maps");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openMediaFacet() {
        openFacet("Media");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openMediaFacet(String appName) {
        openMediaFacet();

        // Radio has its own app switch from other media apps, check for both.
        UiObject2 appSwitch = mDevice.findObject(MEDIA_APP_SWITCH);
        if (appSwitch == null) {
            appSwitch = mDevice.findObject(RADIO_APP_SWITCH);
        }
        if (appSwitch == null) {
            throw new RuntimeException("Failed to find app switch.");
        } else {
            appSwitch.click();
            BySelector appSelector = By.clickable(true).hasChild(By.text(appName));
            if (mDevice.wait(Until.hasObject(appSelector), UI_WAIT_TIMEOUT)) {
                mDevice.findObject(appSelector).clickAndWait(Until.newWindow(), APP_LAUNCH_TIMEOUT);
            } else {
                throw new RuntimeException(
                        String.format("Failed to find %s app in media.", appName));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openDialFacet() {
        openFacet("Dial");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openAppGridFacet() {
        openFacet("App Grid");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openNotificationFacet() {
        openFacet("Notification");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openAssistantFacet() {
        openFacet("Google Assistant");
    }

    private void openFacet(String facetName) {
        BySelector facetSelector = FACET_MAP.get(facetName);
        UiObject2 facet = mDevice.findObject(facetSelector);
        if (facet != null) {
            mDevice.waitForIdle();
            facet.clickAndWait(Until.newWindow(), APP_LAUNCH_TIMEOUT);
        } else {
            throw new RuntimeException(String.format("Failed to find %s facet.", facetName));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openQuickSettings() {
        UiObject2 quickSettings = mDevice.findObject(QUICK_SETTINGS);
        if (quickSettings != null) {
            mDevice.waitForIdle();
            quickSettings.clickAndWait(Until.newWindow(), APP_LAUNCH_TIMEOUT);
        } else {
            throw new RuntimeException("Failed to find quick settings.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clickLeftHvac() {
        clickHvac(LEFT_HVAC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clickRightHvac() {
        clickHvac(RIGHT_HVAC);
    }

    private void clickHvac(BySelector hvacSelector) {
        UiObject2 hvac = mDevice.findObject(hvacSelector);
        if (hvac != null) {
            mDevice.waitForIdle();
            // Hvac is not verifiable from uiautomator. It does not starts a new window, nor does
            // it spawn any specific identifiable ui components from uiautomator's perspective.
            // Therefore, this wait does not verify a new window, but rather it always timeout
            // after <code>APP_LAUNCH_TIMEOUT</code> amount of time so that hvac has sufficient
            // time to be opened/closed.
            hvac.clickAndWait(Until.newWindow(), APP_LAUNCH_TIMEOUT);
        } else {
            throw new RuntimeException("Failed to find hvac.");
        }
    }

    @Override
    public boolean checkApplicationExists(String appName) {
        openAppGridFacet();
        UiObject2 up = mDevice.wait(Until.findObject(UP_BTN), APP_LAUNCH_TIMEOUT);
        UiObject2 down = mDevice.wait(Until.findObject(DOWN_BTN), APP_LAUNCH_TIMEOUT);
        while (up.isEnabled()) {
            up.click();
            up = mDevice.wait(Until.findObject(UP_BTN), UI_WAIT_TIMEOUT);
        }
        UiObject2 object = mDevice.wait(Until.findObject(By.text(appName)), UI_WAIT_TIMEOUT);
        while (down.isEnabled() && object == null) {
            down.click();
            object = mDevice.wait(Until.findObject(By.text(appName)), UI_WAIT_TIMEOUT);
            down = mDevice.wait(Until.findObject(DOWN_BTN), UI_WAIT_TIMEOUT);
        }
        return object != null;
    }

    @Override
    public void openApp(String appName) {
        if (checkApplicationExists(appName)) {
            UiObject2 app = mDevice.wait(Until.findObject(By.text(appName)), APP_LAUNCH_TIMEOUT);
            app.click();
            mDevice.waitForIdle();
        } else {
            throw new RuntimeException(String.format("Application %s not found", appName));
        }
    }

    @SuppressWarnings("unused")
    @Override
    public UiObject2 openAllApps(boolean reset) {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public BySelector getAllAppsButtonSelector() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public BySelector getAllAppsSelector() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public Direction getAllAppsScrollDirection() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public UiObject2 openAllWidgets(boolean reset) {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public BySelector getAllWidgetsSelector() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public Direction getAllWidgetsScrollDirection() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public BySelector getWorkspaceSelector() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public BySelector getHotSeatSelector() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public Direction getWorkspaceScrollDirection() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @SuppressWarnings("unused")
    @Override
    public long launch(String appName, String packageName) {
        openApp(appName);
        return 0;
    }
}
