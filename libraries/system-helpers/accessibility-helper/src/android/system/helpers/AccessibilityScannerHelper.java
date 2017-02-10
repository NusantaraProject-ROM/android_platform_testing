/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.system.helpers;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;

import android.util.Log;

import java.util.List;

/**
 * Implement common helper functions for Accessibility scanner.
 */
public class AccessibilityScannerHelper {
    public static final String ACCESSIBILITY_SCANNER_PACKAGE
            = "com.google.android.apps.accessibility.auditor";
    public static final String MAIN_ACTIVITY_CLASS = "%s.ui.MainActivity";
    public static final String CHECK_BUTTON_RES_ID = "accessibilibutton";
    private static final int SCANNER_WAIT_TIME = 5000;
    private static final int SHORT_TIMEOUT = 2000;
    private static final String LOG_TAG = AccessibilityScannerHelper.class.getSimpleName();
    private static final String RESULT_TAG = "A11Y_SCANNER_RESULT";
    public static AccessibilityScannerHelper sInstance = null;
    private UiDevice mDevice = null;
    private ActivityHelper mActivityHelper = null;
    private PackageHelper mPackageHelper = null;
    private AccessibilityHelper mAccessibilityHelper = null;

    private AccessibilityScannerHelper(Instrumentation instr) {
        mDevice = UiDevice.getInstance(instr);
        mActivityHelper = ActivityHelper.getInstance();
        mPackageHelper = PackageHelper.getInstance(instr);
        mAccessibilityHelper = AccessibilityHelper.getInstance(instr);
    }

    public static AccessibilityScannerHelper getInstance(Instrumentation instr) {
        if (sInstance == null) {
            sInstance = new AccessibilityScannerHelper(instr);
        }
        return sInstance;
    }

    /**
     * If accessibility scanner installed.
     *
     * @return true/false
     */
    public boolean scannerInstalled() {
        return mPackageHelper.isPackageInstalled(ACCESSIBILITY_SCANNER_PACKAGE);
    }

    /**
     * Click scanner check button and parse and log results.
     *
     * @throws Exception
     */
    public void runScanner(String pageName) throws Exception {
        clickScannerCheck();
        if (testPass() == true) {
            Log.i(RESULT_TAG, String.format("%s: PASS", pageName));
        } else {
            logScannerResult(pageName);
        }
        mDevice.pressBack();
    }

    /**
     * Set Accessibility Scanner setting ON/OFF.
     *
     * @throws Exception
     */
    public void setAccessibilityScannerSetting(AccessibilityHelper.SwitchStatus value)
            throws Exception {
        if (!scannerInstalled()) {
            throw new Exception("Accessibility Scanner not installed.");
        }
        mAccessibilityHelper.launchSpecificAccessibilitySetting("Accessibility Scanner");
        for (int tries = 0; tries < 2; tries++) {
            UiObject2 swt = mDevice.wait(Until.findObject(
                    By.res(AccessibilityHelper.SETTINGS_PACKAGE, "switch_widget")), SHORT_TIMEOUT*2);
            if (swt.getText().equals(value.toString())) {
                break;
            } else if (tries == 1) {
                throw new Exception(String.format("Fail to set scanner to: %s.", value.toString()));
            } else {
                swt.click();
                UiObject2 okBtn = mDevice.wait(Until.findObject(By.text("OK")), SHORT_TIMEOUT);
                if (okBtn != null) {
                    okBtn.click();
                }
                if (initialSetups()) {
                    mDevice.pressBack();
                }
                UiObject2 tapOk = mDevice.wait(Until.findObject(
                        By.pkg(ACCESSIBILITY_SCANNER_PACKAGE).text("OK")), SHORT_TIMEOUT);
                if (tapOk != null) {
                    tapOk.click();
                }
            }
        }
    }

    /**
     * Launch accessibility scanner.
     *
     * @throws UiObjectNotFoundException
     */
    public void launchScannerApp() throws Exception {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        ComponentName settingComponent = new ComponentName(ACCESSIBILITY_SCANNER_PACKAGE,
                String.format(MAIN_ACTIVITY_CLASS, ACCESSIBILITY_SCANNER_PACKAGE));
        intent.setComponent(settingComponent);
        mActivityHelper.launchIntent(intent);
        initialSetups();
    }

    /**
     * Steps for first time launching scanner app.
     *
     * @return true/false return false immediately, if initial setup screen doesn't show up.
     * @throws Exception
     */
    private boolean initialSetups() throws Exception {
        UiObject2 getStartBtn = mDevice.wait(
                Until.findObject(By.text("GET STARTED")), SHORT_TIMEOUT);
        if (getStartBtn != null) {
            getStartBtn.click();
            UiObject2 msg = mDevice.wait(Until.findObject(
                    By.text("Turn on Accessibility Scanner")), SHORT_TIMEOUT);
            if (msg != null) {
                mDevice.findObject(By.text("OK")).click();
                setAccessibilityScannerSetting(AccessibilityHelper.SwitchStatus.ON);
            }
            mDevice.wait(Until.findObject(By.text("OK, GOT IT")), SCANNER_WAIT_TIME).click();
            mDevice.wait(Until.findObject(By.text("DISMISS")), SHORT_TIMEOUT).click();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Clear history of accessibility scanner.
     *
     * @throws InterruptedException
     */
    public void clearHistory() throws Exception {
        launchScannerApp();
        int maxTry = 20;
        while (maxTry > 0) {
            List<UiObject2> historyItemList = mDevice.findObjects(
                    By.res(ACCESSIBILITY_SCANNER_PACKAGE, "history_item_row"));
            if (historyItemList.size() == 0) {
                break;
            }
            historyItemList.get(0).click();
            Thread.sleep(SHORT_TIMEOUT);
            deleteHistory();
            Thread.sleep(SHORT_TIMEOUT);
            maxTry--;
        }
    }

    /**
     * Log results of accessibility scanner.
     *
     * @param pageName
     * @throws Exception
     */
    // TODO: parsing detail results information not only number of suggestions.
    public void logScannerResult(String pageName) throws Exception {
        Log.i(RESULT_TAG, String.format("%s: %s suggestions!", pageName, getNumberOfSuggestions()));
    }

    /**
     * Move scanner button to avoid blocking the object.
     *
     * @param avoidObj object to move the check button away from
     */
    public void adjustScannerButton(UiObject2 avoidObj) throws UiObjectNotFoundException {
        Rect origBounds = getScannerCheckBtn().getVisibleBounds();
        Rect avoidBounds = avoidObj.getVisibleBounds();
        if (origBounds.intersect(avoidBounds)) {
            Point dest = calculateDest(origBounds, avoidBounds);
            moveScannerCheckButton(dest.x, dest.y);
        }
    }

    /**
     * Move scanner check button to a target location.
     *
     * @param locX target location x-axis
     * @param locY target location y-axis
     * @throws UiObjectNotFoundException
     */
    private void moveScannerCheckButton(int locX, int locY) throws UiObjectNotFoundException {
        UiObject2 btn = getScannerCheckBtn();
        Rect bounds = btn.getVisibleBounds();
        int origX = bounds.centerX();
        int origY = bounds.centerY();
        if (locX != origX || locY != origY) {
            btn.drag(new Point(locX, locY));
        }
    }

    /**
     * Calculate the moving destination of check button.
     *
     * @param origRect original bounds of the check button
     * @param avoidRect bounds to move away from
     * @return destination of check button center point.
     */
    private Point calculateDest(Rect origRect, Rect avoidRect) {
        int bufferY = (int)Math.ceil(mDevice.getDisplayHeight() * 0.1);
        int destY = avoidRect.bottom + bufferY + origRect.height()/2;
        if (destY >= mDevice.getDisplayHeight()) {
            destY = avoidRect.top - bufferY - origRect.height()/2;
        }
        return new Point(origRect.centerX(), destY);
    }

    /**
     * Return scanner check button.
     *
     * @return UiObject2
     * @throws UiObjectNotFoundException
     */
    private UiObject2 getScannerCheckBtn() throws UiObjectNotFoundException {
        return mDevice.findObject(By.res(ACCESSIBILITY_SCANNER_PACKAGE, CHECK_BUTTON_RES_ID));
    }

    private void clickScannerCheck() throws UiObjectNotFoundException, InterruptedException {
        UiObject2 accessibilityScannerButton = getScannerCheckBtn();
        if (accessibilityScannerButton != null) {
            accessibilityScannerButton.click();
        } else {
            Log.i(LOG_TAG, "Fail to find accessibility scanner check button.");
            throw new UiObjectNotFoundException(
                    "Fail to find accessibility scanner check button.");
        }
        Thread.sleep(SCANNER_WAIT_TIME);
    }

    /**
     * Check if test pass with no suggestions.
     *
     * @return true/false
     * @throws UiObjectNotFoundException
     */
    private Boolean testPass() throws UiObjectNotFoundException {
        UiObject2 txtView = getToolBarTextView();
        return txtView.getText().equals("No suggestions");
    }

    /**
     * Return accessibility scanner tool bar text view.
     *
     * @return UiObject2
     * @throws UiObjectNotFoundException
     */
    private UiObject2 getToolBarTextView() throws UiObjectNotFoundException {
        UiObject2 toolBar = mDevice.wait(Until.findObject(
                By.res(ACCESSIBILITY_SCANNER_PACKAGE, "toolbar")), SHORT_TIMEOUT);
        if (toolBar != null) {
            return toolBar.findObject(By.clazz(AccessibilityHelper.TEXT_VIEW));
        } else {
            throw new UiObjectNotFoundException("Fail to find text view from toolbar.");
        }
    }

    /**
     * Delete active scanner history.
     */
    private void deleteHistory() {
        UiObject2 moreBtn = mDevice.wait(Until.findObject(By.desc("More options")), SHORT_TIMEOUT);
        if (moreBtn != null) {
            moreBtn.click();
            mDevice.wait(Until.findObject(
                    By.clazz(AccessibilityHelper.TEXT_VIEW).text("Delete")), SHORT_TIMEOUT).click();
        }
    }

    /**
     * Return number suggestions.
     *
     * @return number of suggestions
     * @throws UiObjectNotFoundException
     */
    private int getNumberOfSuggestions() throws UiObjectNotFoundException {
        if (testPass() == true) return 0;
        UiObject2 txtView = getToolBarTextView();
        if (txtView != null) {
            String result = txtView.getText();
            String str = result.split("\\s+")[0];
            return Integer.parseInt(str);
        } else {
            throw new UiObjectNotFoundException("Fail to find toolbar text view");
        }
    }
}
