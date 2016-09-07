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

import android.app.Instrumentation;
import android.app.WallpaperManager;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Point;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;
import android.view.KeyEvent;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

public class WallpaperHelper {

    private static final int TIMEOUT = 3000;
    private static final int LONG_TIMEOUT = 10000;
    private static final String WALLPAPER_PACKAGE = "com.google.android.apps.wallpaper";
    private static final String SET_WALLPAPER = "set_wallpaper";
    private UiDevice mDevice;
    private Instrumentation mInstrumentation;
    private WallpaperManager wallpaperManager;

    public enum WallpaperScreen {
        HOME, LOCK, BOTH, LIVE
    }

    public WallpaperHelper(UiDevice device, Instrumentation inst, WallpaperManager wpManager) {
        this.mDevice = device;
        mInstrumentation = inst;
        wallpaperManager = wpManager;
    }

    public String getLauncherPackage() {
        return mDevice.getLauncherPackageName();
    }

    public void launchWallpaperPickerWithIntent() {
        Intent appIntent = new Intent()
                .setClassName("com.google.android.apps.wallpaper",
                        "com.google.android.apps.wallpaper.picker.CategoryPickerActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mInstrumentation.getContext().startActivity(appIntent);
        SystemClock.sleep(TIMEOUT);
    }

    public void setWallpaperFromGivenCategory(String category, String wallpaperName,
            WallpaperScreen wallpaperScreen) throws Exception {
        clearAllWallpapers();
        int[] oldWallpaperIds = returnWallpaperIdsForHomeAndLockScreen();
        launchWallpaperPickerWithIntent();
        findAndClickOnObjectInGrid(category, "category_grid");
        // Pick the wallpaper
        findAndClickOnObjectInGrid(wallpaperName, "wallpaper_grid");
        // If live wallpaper, then there's no need to pick Home/Lockscreen
        // since for security reasons, live wallpaper cannot be set on just
        // the lockscreen.
        switch (wallpaperScreen) {
            case HOME:
                mDevice.wait(Until.findObject(By.res(WALLPAPER_PACKAGE, SET_WALLPAPER)),
                        LONG_TIMEOUT).click();
                mDevice.wait(Until.findObject(By.res(WALLPAPER_PACKAGE,
                        "set_home_wallpaper_button")), TIMEOUT).click();
                break;
            case LOCK:
                mDevice.wait(Until.findObject(By.res(WALLPAPER_PACKAGE, SET_WALLPAPER)),
                        LONG_TIMEOUT).click();
                mDevice.wait(Until.findObject(By.res(WALLPAPER_PACKAGE,
                        "set_lock_wallpaper_button")), TIMEOUT).click();
                break;
            case BOTH:
                mDevice.wait(Until.findObject(By.res(WALLPAPER_PACKAGE, SET_WALLPAPER)),
                        LONG_TIMEOUT).click();
                mDevice.wait(Until.findObject(By.res(WALLPAPER_PACKAGE,
                        "set_both_wallpaper_button")), TIMEOUT).click();
                break;
            // Note: The 'Live' case doesn't show a 'Home' OR 'Home and Lock' dialog box
            // if both the home and the lockscreen wallpaper are set to the same option.
            // This is why clearing the wallpaper manager flags at the beginning of
            // this method is crucial to this option.
            case LIVE:
                mDevice.wait(Until.findObject
                        (By.res("com.android.wallpaper.livepicker:id/set_wallpaper")),
                        LONG_TIMEOUT).click();
                break;
        }
        mDevice.waitForIdle();
        Thread.sleep(TIMEOUT);
        // Assert test is on Home screen
        UiObject2 homeWorkspace = mDevice.wait(Until.findObject
                (By.res(getLauncherPackage(), "workspace")), TIMEOUT);
        Assert.assertNotNull("Test is not on Home screen after setting wallpaper", homeWorkspace);
        // Assert the wallpaper property
        int[] newWallpaperIds = returnWallpaperIdsForHomeAndLockScreen();
        checkIfWallpaperIdChanged(wallpaperScreen, oldWallpaperIds, newWallpaperIds);
    }

    public int[] returnWallpaperIdsForHomeAndLockScreen() {
        int[] wallpaperIds = {-1, -1};
        wallpaperIds[0] = wallpaperManager.getWallpaperId(wallpaperManager.FLAG_SYSTEM);
        wallpaperIds[1] = wallpaperManager.getWallpaperId(wallpaperManager.FLAG_LOCK);
        return wallpaperIds;
    }

    public void checkIfWallpaperIdChanged(WallpaperScreen wallpaperScreen,
            int[] oldWallpapers, int[] newWallpapers) {
        switch (wallpaperScreen) {
            case HOME:
                Assert.assertNotEquals("Home wallpaper not changed", oldWallpapers[0],
                        newWallpapers[0]);
                break;
            case LOCK:
                Assert.assertNotEquals("Lockscreen wallpaper not changed", oldWallpapers[1],
                        newWallpapers[1]);
                break;
            // The lockscreen wallpaper is set to -1 when the SYSTEM and LOCKSCREEN wallpaper
            // are the same. The general wallpaper (FLAG_SYSTEM) applies to both screens in
            // this case.
            case BOTH:
            case LIVE:
                Assert.assertNotEquals("Home wallpaper not changed", oldWallpapers[0],
                        newWallpapers[0]);
                Assert.assertEquals("Lockscreen wallpaper not changed to match System wallpaper",
                        -1, newWallpapers[1]);
                break;
        }
    }

    public void clearAllWallpapers() throws IOException {
        wallpaperManager.clear(wallpaperManager.FLAG_SYSTEM);
        wallpaperManager.clear(wallpaperManager.FLAG_LOCK);
    }

    public void findAndClickOnObjectInGrid(String objectText, String gridResId)
            throws IOException {
        UiObject2 gridObject = mDevice.wait(Until.findObject
                (By.res(WALLPAPER_PACKAGE, gridResId)), TIMEOUT);
        UiObject2 wantedObject = null;
        int scrollCount = 0;
        // Scroll to the top of the grid object if not already on top
        while ((scrollCount < 5) && (gridObject != null) &&
                gridObject.scroll(Direction.UP, 10.0f)) {
            scrollCount++;
        }
        scrollCount = 0;
        // Tap on specific category
        while (scrollCount < 8) {
            if (gridResId == "category_grid" || objectText.contains("IMG_")) {
                wantedObject = mDevice.wait(Until.findObject(By.textContains(objectText)),
                        TIMEOUT);
            }
            else {
                wantedObject = mDevice.wait(Until.findObject(By.descContains(objectText)),
                        TIMEOUT);
            }
            if (wantedObject == null) {
                gridObject.scroll(Direction.DOWN, 0.5f);
                mDevice.waitForIdle();
                scrollCount++;
            }
            else {
                break;
            }
        }
        Assert.assertNotNull("The given object " + objectText +
                " wasn't found in the grid " + gridResId, wantedObject);
        wantedObject.click();
        mDevice.waitForIdle();
    }
}
