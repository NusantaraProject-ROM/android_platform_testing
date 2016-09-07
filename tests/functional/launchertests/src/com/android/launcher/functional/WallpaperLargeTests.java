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

import android.app.WallpaperManager;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Point;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.platform.test.helpers.SettingsHelperImpl;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.KeyEvent;

import junit.framework.TestCase;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;

public class WallpaperLargeTests extends TestCase {
    private static final int TIMEOUT = 3000;
    private static final String WALLPAPER_PACKAGE = "com.google.android.apps.wallpaper";
    private UiDevice mDevice;
    private PackageManager mPackageManager;
    private ILauncherStrategy mLauncherStrategy = null;
    private WallpaperManager wallpaperManager;
    private WallpaperHelper mWallpaperHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mPackageManager = InstrumentationRegistry
                .getInstrumentation().getContext().getPackageManager();
        mDevice.setOrientationNatural();
        mLauncherStrategy = LauncherStrategyFactory
                .getInstance(mDevice).getLauncherStrategy();
        wallpaperManager = WallpaperManager.getInstance
                 (InstrumentationRegistry.getInstrumentation().getContext());
        mWallpaperHelper = new WallpaperHelper(mDevice,
                 InstrumentationRegistry.getInstrumentation(), wallpaperManager);
    }

    @Override
    protected void tearDown() throws Exception {
        mWallpaperHelper.clearAllWallpapers();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressHome();
        mDevice.unfreezeRotation();
        mDevice.waitForIdle();
        super.tearDown();
    }

    /*
    @LargeTest
    public void testSetWallpaperPhoto() throws Exception {
       // mCameraHelper.goToCameraMode();
       // mCameraHelper.capturePhoto();
       // Thread.sleep(LONG_TIMEOUT * 2);
       mWallpaperHelper.setWallpaperFromGivenCategory("My photos",
                 "IMG_", WallpaperHelper.WallpaperScreen.BOTH);
    }
    */

    @LargeTest
    public void testSetWallpaperLandscape() throws Exception {
       mWallpaperHelper.setWallpaperFromGivenCategory("Landscape",
                 "Photo by ", WallpaperHelper.WallpaperScreen.BOTH);
    }

    @LargeTest
    public void testSetWallpaperCityScape() throws Exception {
       mWallpaperHelper.setWallpaperFromGivenCategory("Cityscape",
                 "Photo by ", WallpaperHelper.WallpaperScreen.BOTH);
    }

    @LargeTest
    public void testSetWallpaperTextures() throws Exception {
       mWallpaperHelper.setWallpaperFromGivenCategory("Textures",
                 "Photo by ", WallpaperHelper.WallpaperScreen.BOTH);
    }

    @LargeTest
    public void testSetWallpaperLife() throws Exception {
       mWallpaperHelper.setWallpaperFromGivenCategory("Life",
                 "Photo by ", WallpaperHelper.WallpaperScreen.BOTH);
    }
}
