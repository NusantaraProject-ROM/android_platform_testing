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

package com.android.support.test.helpers;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;

import junit.framework.Assert;

public class SettingsAppHelper extends AbstractSettingsHelper {

    private static final int SETTINGS_DASH_TIMEOUT = 3000;
    private static final String UI_PACKAGE_NAME = "com.android.settings";
    private static final BySelector SETTINGS_DASHBOARD = By.res(UI_PACKAGE_NAME,
            "dashboard_container");
    private static final String LOG_TAG = SettingsAppHelper.class.getSimpleName();

    public SettingsAppHelper(Instrumentation instr) {
        super(instr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return "com.android.settings";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "Settings";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public void scrollThroughSettings(int numberOfFlings) throws Exception {
        UiObject2 settingsList = loadAllSettings();
        int count = 0;
        while (count <= numberOfFlings && settingsList.fling(Direction.DOWN)) {
            count++;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flingSettingsToStart() throws Exception {
        UiObject2 settingsList = loadAllSettings();
        while (settingsList.fling(Direction.UP));
    }

    /**
     * On N, the settingsDashboard is initially collapsed, and the user can see the "See all"
     * element. On hitting "See all", the same settings dashboard element is now scrollable. For
     * pre-N, the settings Dashboard is always scrollable, hence the check in the while loop. All
     * this method does is expand the Settings list if needed, before returning the element.
     */
    private UiObject2 loadAllSettings() throws Exception {
        UiObject2 settingsDashboard = mDevice.wait(Until.findObject(SETTINGS_DASHBOARD),
                SETTINGS_DASH_TIMEOUT);
        Assert.assertNotNull("Could not find the settings dashboard object.", settingsDashboard);
        int count = 0;
        while (!settingsDashboard.isScrollable() && count <= 2) {
            mDevice.wait(Until.findObject(By.text("SEE ALL")), SETTINGS_DASH_TIMEOUT).click();
            settingsDashboard = mDevice.wait(Until.findObject(SETTINGS_DASHBOARD),
                    SETTINGS_DASH_TIMEOUT);
            count++;
        }
        return settingsDashboard;
    }
}
