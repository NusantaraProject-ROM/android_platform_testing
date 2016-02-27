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

package android.launcher.functional;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

public class HomeScreenTests extends InstrumentationTestCase {

    private static final int TIMEOUT = 3000;
    private static final String HOTSEAT = "hotseat";
    private UiDevice mDevice;
    private PackageManager mPackageManager;
    private ILauncherStrategy mLauncherStrategy = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mPackageManager = getInstrumentation().getContext().getPackageManager();
        try {
            mDevice.setOrientationNatural();
        } catch (RemoteException e) {
            throw new RuntimeException("failed to freeze device orientaion", e);
        }
        mLauncherStrategy = LauncherStrategyFactory.getInstance(mDevice).getLauncherStrategy();
    }

    @Override
    protected void tearDown() throws Exception {
        mDevice.pressHome();
        super.tearDown();
    }

    public String getLauncherPackage() {
        return mDevice.getLauncherPackageName();
    }

    public void launchAppWithIntent(String appPackageName) {
        Intent appIntent = mPackageManager.getLaunchIntentForPackage(appPackageName);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getInstrumentation().getContext().startActivity(appIntent);
        SystemClock.sleep(TIMEOUT);
    }

    @MediumTest
    public void testGoHome() {
        launchAppWithIntent("com.android.chrome");
        mDevice.pressHome();
        UiObject2 hotseat = mDevice.findObject(By.res(getLauncherPackage(), HOTSEAT));
        assertNotNull("Hotseat could not be found", hotseat);
    }

    @MediumTest
    public void testHomeToRecentsNavigation() throws Exception {
        mDevice.pressRecentApps();
        assertNotNull("Recents not found when navigating from hotseat",
                mDevice.wait(Until.hasObject(By.res("com.android.systemui:id/recents_view")),
                TIMEOUT));
    }
}
