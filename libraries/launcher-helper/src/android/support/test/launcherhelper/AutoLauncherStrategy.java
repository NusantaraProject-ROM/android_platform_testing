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
import android.system.helpers.CommandsHelper;

import junit.framework.Assert;

public class AutoLauncherStrategy implements IAutoLauncherStrategy {

    private static final String LOG_TAG = AutoLauncherStrategy.class.getSimpleName();
    private static final String CAR_LENSPICKER = "com.android.car.carlauncher";
    private static final String OPEN_APP_GRID_COMMAND =
            "am start -n com.android.car.carlauncher/.AppGridActivity";

    private static final long APP_INIT_WAIT = 10000;
    private static final int OPEN_FACET_RETRY_TIME = 5;

    //todo: Remove x and y axis and use resource ID's.
    private static final int FACET_APPS = 560;
    private static final int MAP_FACET = 250;

    protected UiDevice mDevice;
    private Instrumentation mInstrumentation;

    @Override
    public String getSupportedLauncherPackage() {
        return CAR_LENSPICKER;
    }

    @Override
    public void setUiDevice(UiDevice uiDevice) {
        mDevice = uiDevice;
    }

    @Override
    public void setInstrumentation(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
    }

    @Override
    public void open() {

    }

    @Override
    public void openDialFacet() {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @Override
    public void openMediaFacet(String appName) {
        BySelector button = By.clickable(true).hasDescendant(By.text(appName));
        for (int tries = 3; tries >= 0; tries--) {
            // TODO: Switch this to intents. It doesn't appear to work via intents on my system.
            CommandsHelper.getInstance(mInstrumentation).executeShellCommand(
                    "am start -n com.android.support.car.lenspicker/.LensPickerActivity"
                            + " --esa categories android.intent.category.APP_MUSIC");
            mDevice.wait(Until.findObject(button), APP_INIT_WAIT);
            if (mDevice.hasObject(button)) {
                break;
            }
        }
        UiObject2 choice = mDevice.findObject(button);
        Assert.assertNotNull("Unable to find application " + appName, choice);
        choice.click();
        mDevice.wait(Until.gone(button), APP_INIT_WAIT);
        Assert.assertFalse("Failed to exit media menu.", mDevice.hasObject(button));
        mDevice.waitForIdle(APP_INIT_WAIT);
    }

    @Override
    public void openSettingsFacet(String appName) {
        throw new UnsupportedOperationException(
                "The feature not supported on Auto");
    }

    @Override
    public void openMapsFacet(String appName) {
        CommandsHelper.getInstance(mInstrumentation).executeShellCommand(
                "input tap " + MAP_FACET + " " + FACET_APPS);
    }

    @Override
    public void openHomeFacet() {
        UiDevice.getInstance(mInstrumentation).pressHome();
    }

    @Override
    public void openAssistantFacet() {
        CommandsHelper.getInstance(mInstrumentation).executeShellCommand(
                "am start -n com.google.android.googlequicksearchbox/"
                        + "com.google.android.apps.gsa.binaries.auto.app.voiceplate"
                        + ".VoicePlateActivity");
    }

    @Override
    public boolean checkApplicationExists(String appName) {
        CommandsHelper.getInstance(mInstrumentation).executeShellCommand(
                OPEN_APP_GRID_COMMAND);
        BySelector selector = By.scrollable(true);
        UiObject2 scrollable = mDevice.wait(Until.findObject(selector), APP_INIT_WAIT);
        UiObject2 object = mDevice.wait(
                Until.findObject(By.text(appName)), APP_INIT_WAIT);
        boolean flag = true;
        while (flag && object == null) {
            flag = scrollable.scroll(Direction.DOWN, 0.5f);
            object = mDevice.wait(
                Until.findObject(By.text(appName)), APP_INIT_WAIT);
        }
        return object != null ? true : false;
    }

    @Override
    public void openApp(String appName) {
        if (checkApplicationExists(appName)) {
            UiObject2 app = mDevice.wait(
                    Until.findObject(By.text(appName)), APP_INIT_WAIT);
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
        return 0;
    }
}
