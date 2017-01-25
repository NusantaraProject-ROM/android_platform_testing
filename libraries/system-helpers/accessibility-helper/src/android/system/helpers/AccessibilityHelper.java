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
import android.content.Context;
import android.provider.Settings;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

/**
 * Implement common helper functions for accessibility.
 */
public class AccessibilityHelper {
    public static final String SETTINGS_PACKAGE = "com.android.settings";
    public static final String BUTTON = "android.widget.Button";
    public static final String IMAGE_BUTTON = "android.widget.ImageButton";
    public static final String TEXT_VIEW = "android.widget.TextView";
    public static final String SWITCH = "android.widget.Switch";
    public static final int SHORT_TIMEOUT = 2000;
    public static final int LONG_TIMEOUT = 5000;
    public static AccessibilityHelper sInstance = null;
    private Context mContext = null;
    private Instrumentation mInstrumentation = null;
    private UiDevice mDevice = null;
    private SettingsHelper mSettingsHelper = null;

    public enum SwitchStatus {
        ON,
        OFF;
    }

    private AccessibilityHelper(Instrumentation instr) {
        mInstrumentation = instr;
        mSettingsHelper = SettingsHelper.getInstance();
        mDevice = UiDevice.getInstance(instr);
        mContext = mInstrumentation.getTargetContext();
    }

    public static AccessibilityHelper getInstance(Instrumentation instr) {
        if (sInstance == null) {
            sInstance = new AccessibilityHelper(instr);
        }
        return sInstance;
    }

    /**
     * Set Talkback "ON"/"OFF".
     *
     * @param value "ON"/"OFF"
     * @throws Exception
     */
    public void setTalkBackSetting(SwitchStatus value) throws Exception {
        launchSpecificAccessibilitySetting("TalkBack");
        setSwitchBarValue(SETTINGS_PACKAGE, value);
        mDevice.pressBack();
    }

    /**
     * Set high contrast "ON"/"OFF".
     *
     * @param value "ON"/"OFF"
     * @throws Exception
     */
    public void setHighContrast(SwitchStatus value) throws Exception {
        launchSpecificAccessibilitySetting("Accessibility");
        setSwitchValue(SETTINGS_PACKAGE, "High contrast text", value);
    }

    /**
     * Launch specific accessibility setting page.
     *
     * @param settingName Specific accessibility setting name
     * @throws Exception
     */
    public void launchSpecificAccessibilitySetting(String settingName) throws Exception {
        mSettingsHelper.launchSettingsPage(mContext, Settings.ACTION_ACCESSIBILITY_SETTINGS);
        int maxTry = 3;
        while (maxTry-- >= 0) {
            Thread.sleep(SHORT_TIMEOUT);
            UiObject2 actionBar = mDevice.wait(Until.findObject(
                    By.res(SETTINGS_PACKAGE, "action_bar").enabled(true)), SHORT_TIMEOUT);
            if (actionBar == null) {
                mSettingsHelper.launchSettingsPage(mContext,
                        Settings.ACTION_ACCESSIBILITY_SETTINGS);
            } else {
                String actionBarText = actionBar.findObject(By.clazz(TEXT_VIEW)).getText();
                if (actionBarText.equals(settingName)) {
                    break;
                } else if (actionBarText.equals("Accessibility")) {
                getItemFromList(SETTINGS_PACKAGE, settingName).click();
                } else {
                    mDevice.wait(Until.findObject(By.res(SETTINGS_PACKAGE, "action_bar")
                            .enabled(true)), SHORT_TIMEOUT)
                            .findObject(By.clazz(IMAGE_BUTTON)).click();
                }
            }
        }
    }

    /**
     * Set switch "ON"/"OFF".
     *
     * @param pkg package
     * @param settingTag setting name
     * @param value "ON"/"OFF"
     * @return true/false
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    private boolean setSwitchValue(String pkg, String settingTag, SwitchStatus value)
            throws UiObjectNotFoundException, InterruptedException {
        UiObject2 cellSwitch = getItemFromList(pkg, settingTag)
                .getParent().getParent().findObject(By.clazz(SWITCH));
        if (cellSwitch != null) {
            if (!cellSwitch.getText().equals(value.toString())) {
                cellSwitch.click();
                UiObject2 okBtn = mDevice.wait(Until.findObject(
                        By.res("android:id/button1")), LONG_TIMEOUT);
                if (okBtn != null) {
                    okBtn.click();
                }
            }
            return cellSwitch.getText().equals(value.toString());
        }
        return false;
    }

    /**
     * Set the switch widget value on the switch bar.
     *
     * @param pkg package
     * @param value "ON"/"OFF"
     * @return true/false
     */
    private boolean setSwitchBarValue(String pkg, SwitchStatus value) throws InterruptedException {
        int tries = 2; // Sometimes enable failed for the 1st time.
        while (tries > 0) {
            UiObject2 switchBar = mDevice.wait(Until.findObject(
                    By.res(pkg, "switch_bar")), SHORT_TIMEOUT);
            UiObject2 switchWidget = switchBar.findObject(By.res(pkg, "switch_widget"));
            if (switchWidget != null) {
                if (!switchWidget.getText().equals(value.toString())) {
                    switchWidget.click();
                    UiObject2 okBtn = mDevice.wait(Until.findObject(
                            By.res("android:id/button1")), LONG_TIMEOUT);
                    if (okBtn != null) {
                        okBtn.click();
                    }
                } else {
                    return true;
                }
            }
            tries--;
        }
        return false;
    }

    /**
     * Get object from list.
     *
     * @param pkg package
     * @param itemText item name/text
     * @return UiObject2
     * @throws UiObjectNotFoundException
     */
    public UiObject2 getItemFromList(String pkg, String itemText)
            throws UiObjectNotFoundException {
        UiScrollable listScrollable = new UiScrollable(
                new UiSelector().resourceId(pkg+":id/list"));
        if (listScrollable != null) {
            listScrollable.scrollToBeginning(100);
            listScrollable.scrollTextIntoView(itemText);
            return mDevice.findObject(By.text(itemText));
        } else {
            throw new UiObjectNotFoundException("Fail to get scrollable list %s.");
        }
    }
}
