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

package com.android.functional.externalstoragetests;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

public class AdoptableStorageTests extends InstrumentationTestCase {
    public final String SETTINGS_PKG = "com.android.settings";
    private UiDevice mDevice = null;
    private Context mContext = null;
    private UiAutomation mUiAutomation = null;
    private ExternalStorageHelper storageHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mContext = getInstrumentation().getContext();
        mUiAutomation = getInstrumentation().getUiAutomation();
        storageHelper = ExternalStorageHelper.getInstance(mDevice, mContext, mUiAutomation,
                getInstrumentation());
    }

    /**
     * Tests external storage adoption and move data later flow via UI
     */
    @LargeTest
    public void testAdoptAsAdoptableMoveDataLaterUIFlow() throws InterruptedException {
        // ensure there is a storage to be adopted
        storageHelper.partitionDisk("public");
        initiateAdoption();
        mDevice.wait(Until.findObject(By.text("Move later")), storageHelper.TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("Next")), storageHelper.TIMEOUT).clickAndWait(
                Until.newWindow(), storageHelper.TIMEOUT);
        mDevice.wait(Until.findObject(By.text("Done")), storageHelper.TIMEOUT).clickAndWait(
                Until.newWindow(), storageHelper.TIMEOUT);
        assertNotNull(storageHelper.getAdoptionVolumeId("private"));
        // ensure data dirs have not moved
        Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        mDevice.wait(Until.findObject(By.textContains("SD card")), 2 * storageHelper.TIMEOUT)
                .clickAndWait(Until.newWindow(), storageHelper.TIMEOUT);
        assertTrue(mDevice.wait(Until.hasObject(By.res("android:id/title").text("Apps")),
                        storageHelper.TIMEOUT));
    }

    // Adoptable storage settings
    /**
     * tests to ensure that adoptable storage has setting options rename, eject, format as portable
     */
    @LargeTest
    public void testAdoptableOverflowSettings() throws InterruptedException {
        storageHelper.partitionDisk("private");
        storageHelper.openSDCard();
        UiObject2 moreOptions = mDevice.wait(Until.findObject(By.desc("More options")),
                storageHelper.TIMEOUT);
        assertNotNull(moreOptions);
        moreOptions.click();
        assertTrue(mDevice.wait(Until.hasObject(By.text("Rename")), storageHelper.TIMEOUT));
        assertTrue(mDevice.wait(Until.hasObject(By.text("Eject")), storageHelper.TIMEOUT));
        assertTrue(mDevice.wait(Until.hasObject(By.text("Format as portable")),
                storageHelper.TIMEOUT));
    }

    /**
     * tests to ensure that adoptable storage can be renamed
     */
    @LargeTest
    public void testRenameAdoptable() throws InterruptedException {
        storageHelper.partitionDisk("private");
        storageHelper.openSDCard();
        mDevice.wait(Until.findObject(By.desc("More options")), storageHelper.TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("Rename")), storageHelper.TIMEOUT).click();
        mDevice.wait(Until.findObject(By.res("com.android.settings:id/edittext")),
                storageHelper.TIMEOUT)
                .setText("My SD card");
        mDevice.wait(Until.findObject(By.text("Save")), storageHelper.TIMEOUT).clickAndWait(
                Until.newWindow(),
                storageHelper.TIMEOUT);
        assertTrue(mDevice.wait(Until.hasObject(By.text("My SD card")), storageHelper.TIMEOUT));
    }

    /**
     * tests to ensure that adoptable storage can be ejected
     */
    @LargeTest
    public void testEjectAdoptable() throws InterruptedException {
        storageHelper.partitionDisk("private");
        storageHelper.openSDCard();
        mDevice.wait(Until.findObject(By.desc("More options")), storageHelper.TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("Eject")), storageHelper.TIMEOUT).click();
        assertTrue(mDevice.wait(Until.hasObject(By.res("com.android.settings:id/body")),
                        storageHelper.TIMEOUT));
        mDevice.wait(Until.findObject(By.res("com.android.settings:id/confirm").text("Eject")),
                storageHelper.TIMEOUT).clickAndWait(Until.newWindow(), storageHelper.TIMEOUT);
        assertTrue(mDevice.wait(Until.hasObject(By.res("android:id/summary").text("Ejected")),
                storageHelper.TIMEOUT));
        mDevice.wait(Until.findObject(By.textContains("SD card")), storageHelper.TIMEOUT).click();
        mDevice.wait(Until.findObject(By.res("android:id/button1").text("Mount")),
                2 * storageHelper.TIMEOUT).clickAndWait(Until.newWindow(), storageHelper.TIMEOUT);
    }

    /**
     * tests to ensure that adoptable storage can be formated back as portable from settings
     */
    @LargeTest
    public void testFormatAdoptableAsPortable() throws InterruptedException {
        storageHelper.partitionDisk("private");
        storageHelper.openSDCard();
        mDevice.wait(Until.findObject(By.desc("More options")), storageHelper.TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("Format as portable")), storageHelper.TIMEOUT)
                .clickAndWait(Until.newWindow(), storageHelper.TIMEOUT);
        mDevice.wait(Until.hasObject(
                By.textContains("After formatting, you can use this")), storageHelper.TIMEOUT);
        assertTrue(mDevice.wait(Until.hasObject(By.text("Format")), storageHelper.TIMEOUT));
    }

    public void initiateAdoption() throws InterruptedException {
        storageHelper.openSdCardSetUpNotification().clickAndWait(Until.newWindow(),
                storageHelper.TIMEOUT);
        UiObject2 adoptFlowUi = mDevice.wait(Until.findObject(
                By.res("com.android.settings:id/storage_wizard_init_internal_title")),
                storageHelper.TIMEOUT);
        adoptFlowUi.click();
        adoptFlowUi = mDevice.wait(Until.findObject(
                By.res("com.android.settings:id/suw_navbar_next").text("Next")),
                storageHelper.TIMEOUT);
        adoptFlowUi.clickAndWait(Until.newWindow(), storageHelper.TIMEOUT);
        adoptFlowUi = mDevice.wait(Until.findObject(By.text("Erase & format")),
                storageHelper.TIMEOUT);
        adoptFlowUi.clickAndWait(Until.newWindow(), storageHelper.TIMEOUT);
        adoptFlowUi = mDevice.wait(
                Until.findObject(By.res("com.android.settings:id/storage_wizard_progress")),
                storageHelper.TIMEOUT);
        assertNotNull(adoptFlowUi);
        if ((mDevice.wait(Until.findObject(By.res("android:id/message")),
                60 * storageHelper.TIMEOUT)) != null) {
            mDevice.wait(Until.findObject(By.text("OK")), storageHelper.TIMEOUT).clickAndWait(
                    Until.newWindow(), storageHelper.TIMEOUT);
        }
    }

    /**
     * System apps can't be moved to adopted storage
     */
    @LargeTest
    public void testTransferSystemApp() throws InterruptedException, NameNotFoundException {
        storageHelper.partitionDisk("private");
        storageHelper.executeShellCommand( "pm move-package " + SETTINGS_PKG + " "
                        + storageHelper.getAdoptionVolumeId("private"));
        assertTrue(storageHelper.getInstalledLocation(SETTINGS_PKG).startsWith("/data/user/0"));
    }

    @Override
    protected void tearDown() throws Exception {
        mDevice.pressBack();
        mDevice.pressHome();
        super.tearDown();
    }
}