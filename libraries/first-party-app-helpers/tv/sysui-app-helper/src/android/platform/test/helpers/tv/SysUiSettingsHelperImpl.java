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
 * limitations under the License
 */

package android.platform.test.helpers.tv;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.platform.test.helpers.AbstractLeanbackAppHelper;
import android.platform.test.helpers.DPadHelper;
import android.platform.test.helpers.exceptions.UiTimeoutException;
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.provider.Settings;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;

import java.util.regex.Pattern;


/**
 * App helper implementation class for TV Settings
 */
public class SysUiSettingsHelperImpl extends AbstractLeanbackAppHelper {
    private static final String LOG_TAG = SysUiSettingsHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.android.tv.settings";
    private static final String ACTION_SETTINGS = Settings.ACTION_SETTINGS;
    private static final String RES_PKG_ANDROID = "android";
    private static final String RES_TITLE = "title";
    private static final String RES_SUMMARY = "summary";
    private static final long SHORT_SLEEP_MS = 3000;

    public static final int SETTINGS_SYSTEM = 0;
    public static final int SETTINGS_SECURE = 1;
    public static final int SETTINGS_GLOBAL = 2;

    private Context mContext;


    public SysUiSettingsHelperImpl(Instrumentation instrumentation) {
        super(instrumentation);
        mDPadHelper = DPadHelper.getInstance(instrumentation);
        mContext = instrumentation.getContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return UI_PACKAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        throw new UnsupportedOperationException("This method is not supported for Settings");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        open(ACTION_SETTINGS, SHORT_SLEEP_MS);
    }

    /**
     * Setup expectation: On the launcher home screen.
     * <p>
     * Launches the desired application and wait for it to begin running before returning.
     * </p>
     * @param timeoutMs timeout in milliseconds to open an activity
     */
    public void open(String action, long timeoutMs) {
        launchActivity(action);
        if (timeoutMs > 0 && !waitForOpen(timeoutMs)) {
            throw new UiTimeoutException(String.format("Timed out to open a target package %s:"
                    + " %d(ms)", getPackage(), timeoutMs));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        mDevice.pressHome();
        mDevice.waitForIdle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
        // Nothing to do.
    }

    /**
     * Setup expectations: The Settings is open.
     * <p>
     * Selects the main Settings item by title text.
     * </p>
     * @param title the title string of the setting to find
     * @return true if the setting that matches the name is open
     */
    public boolean clickSetting(String title) {
        UiObject2 setting = findSettingByTitle(title);
        if (setting == null) {
            return false;
        }
        mDPadHelper.pressDPadCenterAndWait(Until.newWindow(), SHORT_SLEEP_MS);
        return true;
    }

    /**
     * Setup expectations: The Settings is open. Selects the main Settings item by summary text.
     * @param summary the summary string of the setting to find
     * @return true if the setting that matches the name is open
     */
    public boolean clickSettingBySummary(String summary) {
        UiObject2 setting = findSettingBySummary(summary);
        if (setting == null) {
            return false;
        }
        mDPadHelper.pressDPadCenterAndWait(Until.newWindow(), SHORT_SLEEP_MS);
        return true;
    }

    /**
     * Setup expectations: The Settings is open.
     * @param title the title string of the setting to find
     * @return true if it finds {@link UiObject2} that has a given title text
     */
    public boolean hasSettingByTitle(String title) {
        return (findSettingByTitle(title) != null);
    }

    /**
     * Setup expectations: The Settings is open.
     * @param summary the summary string of the setting to find
     * @return true if it finds {@link UiObject2} that has a given summary text
     */
    public boolean hasSettingBySummary(String summary) {
        return (findSettingBySummary(summary) != null);
    }

    /**
     * Setup expectations: The Settings is open. Finds the setting that matches both
     * a given title and summary
     * @param title the title string of the setting to find
     * @param summary the summary string of the setting to find
     * @return true if it finds {@link UiObject2} that has both title and summary text passed.
     */
    public boolean hasSettingByTitleAndSummary(String title, String summary) {
        return (findSettingByTitleAndSummary(title, summary) != null);
    }

    /**
     * Setup expectations: The Settings is open.
     * @param text the name of the setting to find
     * @return true if it finds {@link UiObject2} that has a given text either in title or summary
     */
    public boolean hasSettingByTitleOrSummary(String text) {
        return (findSettingByTitleOrSummary(text) != null);
    }

    /**
     * Setup expectations: The Settings is open. Checks if the switch bar is turned on.
     * @param title the name of the setting to check
     * @return true if the setting is turned on
     */
    public boolean isSwitchBarOn(String title) {
        return "ON".equals(getSwitchBarText(title));
    }

    /**
     * Setup expectations: The Settings is open. Checks if the switch bar is turned off.
     * @param title the name of the setting to check
     * @return true if the setting is turned off
     */
    public boolean isSwitchBarOff(String title) {
        return "OFF".equals(getSwitchBarText(title));
    }

    /**
     * Setup expectations: The Accessibility Settings is open. Finds the preview text on screen.
     * @return true if the preview text is displayed on screen.
     */
    public boolean hasPreviewText() {
        return mDevice.hasObject(By.res(UI_PACKAGE, "preview_text"));
    }

    /**
     * Setup expectations: The Settings is open.
     * @return {@link UiObject2} of the current focused setting
     */
    public String getCurrentFocusedSettingTitle() {
        return mDevice.wait(Until.findObject(By.focused(true)), SHORT_SLEEP_MS)
                .findObject(By.res(RES_PKG_ANDROID, "title")).getText();
    }

    /**
     * Setup expectations: The Settings is open. Returns the summary text of the selected Settings.
     * @param title the name of the setting to get the summary text
     * @return String of the summary text
     */
    public String getSummaryTextByTitle(String title) {
        UiObject2 settings = findSettingByTitle(title);
        return settings.findObject(By.res(RES_PKG_ANDROID, RES_SUMMARY)).getText();
    }

    /**
     * Setup expectations: The Settings is open.
     *
     * Exit the guided settings by pressing BACK key a given times
     * @param maxDepth The maximum depth to exit the guided settings. Should be greater than 0
     * @return true if the Settings is closed.
     */
    public boolean goBackGuidedSettings(int maxDepth) {
        if (maxDepth < 1) {
            Log.w(LOG_TAG, "maxDepth should be greater than 0");
            maxDepth = 1;
        }
        UiObject2 focused = mDevice.wait(Until.findObject(By.focused(true)), SHORT_SLEEP_MS);
        if (focused == null) {
            throw new IllegalStateException("No focused item is found");
        }
        while (maxDepth-- > 0) {
            mDPadHelper.pressBack();
            if (!waitForOpen(SHORT_SLEEP_MS)) {
                Log.w(LOG_TAG, "Settings is closed.");
                return false;
            } else if (focused.equals(
                    mDevice.wait(Until.findObject(By.focused(true)), SHORT_SLEEP_MS))) {
                Log.w(LOG_TAG, "The focused is the same. Nothing happened in Settings?");
                return false;
            }
        }
        return true;
    }

    /**
     * Setup expectations: On waiting for a guided setting to open.
     * @param title the title string of the guided setting
     * @param timeoutMs timeout in milliseconds to get the header title
     * @return true if the guided setting that has a given title is open in timeout
     */
    public boolean waitForOpenGuidedSetting(String title, long timeoutMs) {
        UiObject2 header = mDevice.wait(
                Until.findObject(By.res(UI_PACKAGE, "decor_title").text(title)), timeoutMs);
        return (header != null);
    }

    /**
     * Setup expectations: The Settings is open.
     * @return the title of the guided settings
     */
    private String getGuidedSettingTitle() {
        UiObject2 header = mDevice.findObject(By.res(UI_PACKAGE, "decor_title"));
        if (header == null) {
            throw new UnknownUiException("Header text is not found");
        }
        return header.getText();
    }

    public String getStringSetting(int type, String name) {
        switch (type) {
            case SETTINGS_SYSTEM:
                return Settings.System.getString(mContext.getContentResolver(), name);
            case SETTINGS_GLOBAL:
                return Settings.Global.getString(mContext.getContentResolver(), name);
            case SETTINGS_SECURE:
                return Settings.Secure.getString(mContext.getContentResolver(), name);
        }
        return "";
    }

    public int getIntSetting(int type, String name, int def) {
        int value = getIntSetting(type, name);
        return value != Integer.MIN_VALUE ? value : def;
    }

    public int getIntSetting(int type, String name) {
        try {
            switch (type) {
                case SETTINGS_SYSTEM:
                    return Settings.System.getInt(mContext.getContentResolver(), name);
                case SETTINGS_GLOBAL:
                    return Settings.Global.getInt(mContext.getContentResolver(), name);
                case SETTINGS_SECURE:
                    return Settings.Secure.getInt(mContext.getContentResolver(), name);
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.w(LOG_TAG, String.format("Settings not found name=%s, type=%d", name, type));
        }
        return Integer.MIN_VALUE;
    }

    public boolean isDeveloperOptionsEnabled() {
        return getIntSetting(SETTINGS_GLOBAL, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                android.os.Build.TYPE.equals("eng") ? 1 : 0) == 1;
    }

    /**
     * Setup expectations: PIN code activity is open. Set a new PIN code
     * @param pinCode the PIN code of 4 digits
     * @return true if a PIN code is set
     */
    public boolean setNewPinCode(String pinCode) {
        return setPinCode("Set a new PIN", pinCode, Direction.DOWN);
    }

    public boolean reenterPinCode(String pinCode) {
        return setPinCode("Re-enter new PIN", pinCode, Direction.DOWN);
    }

    public boolean enterPinCode(String pinCode) {
        return setPinCode("Enter PIN", pinCode, Direction.DOWN);
    }

    protected BySelector getTitleSelector(String text) {
        return By.res(RES_PKG_ANDROID, RES_TITLE).text(text);
    }

    protected BySelector getSummarySelector(String text) {
        return By.res(RES_PKG_ANDROID, RES_SUMMARY).text(text);
    }

    /**
     * Find the focused item in Settings that has a descendant matches the criteria.
     * @param selector for a descendant of the setting to match.
     * @param direction the direction to find, only accepts UP and DOWN.
     * @return {@link UiObject2} of the focusable item that has a given descendant
     */
    private UiObject2 findSettingHasDescendant(BySelector selector, Direction direction) {
        if (!isAppInForeground()) {
            throw new IllegalStateException("Required to open the Settings ahead");
        }
        if (direction != Direction.DOWN && direction != Direction.UP) {
            throw new IllegalArgumentException("Required to go either up or down to find rows");
        }

        UiObject2 currentFocused = mDevice.findObject(By.focused(true));
        UiObject2 prevFocused = null;
        UiObject2 found = null;
        while (!currentFocused.equals(prevFocused)) {
            if ((found = mDevice.findObject(By.focused(true).hasDescendant(selector, 3)))
                    != null) {
                return found;
            }
            if (direction == Direction.DOWN) {
                mDevice.pressDPadDown();
            } else if (direction == Direction.UP) {
                mDevice.pressDPadUp();
            }
            prevFocused = currentFocused;
            currentFocused = mDevice.findObject(By.focused(true));
        }
        Log.d(LOG_TAG, "Failed to find the item until it reaches the end.");
        return found;
    }

    private String getSwitchBarText(String title) {
        UiObject2 setting = findSettingByTitle(title);
        return setting.findObject(By.res(RES_PKG_ANDROID, "switch_widget")).getText();
    }

    private UiObject2 findSettingByTitle(String title) {
        return findSettingBySelector(getTitleSelector(title), true);
    }

    private UiObject2 findSettingBySummary(String summary) {
        return findSettingBySelector(getSummarySelector(summary), true);
    }

    private UiObject2 findSettingByTitleAndSummary(String title, String summary) {
        BySelector titleSelector = getTitleSelector(title);
        BySelector summarySelector = getSummarySelector(summary);
        UiObject2 setting;
        while ((setting = findSettingHasDescendant(titleSelector, Direction.DOWN)) != null) {
            if (setting.hasObject(summarySelector)) {
                return setting;
            }
        }
        while ((setting = findSettingHasDescendant(titleSelector, Direction.UP)) != null) {
            if (setting.hasObject(summarySelector)) {
                return setting;
            }
        }
        return null;
    }

    private UiObject2 findSettingByTitleOrSummary(String text) {
        final Pattern RES_REGEX = Pattern.compile(
                String.format("%s:id/(%s|%s)", RES_PKG_ANDROID, RES_TITLE, RES_SUMMARY));
        return findSettingBySelector(By.res(RES_REGEX).text(text), true);
    }

    private UiObject2 findSettingBySelector(BySelector selector, boolean throwIfFail) {
        UiObject2 setting;
        if ((setting = findSettingHasDescendant(selector, Direction.DOWN)) != null) {
            return setting;
        }
        if ((setting = findSettingHasDescendant(selector, Direction.UP)) != null) {
            return setting;
        }
        if (throwIfFail) {
            throw new UnknownUiException(
                    String.format("No focused setting matches a given selector: %s",
                            selector.toString()));
        }
        return null;
    }

    private boolean setPinCode(String title, String pinCode, Direction direction) {
        if (!isValidPinCode(pinCode)) {
            throw new IllegalArgumentException("4 digits PIN code is valid. pinCode=" + pinCode);
        }
        if (direction != Direction.DOWN && direction != Direction.UP) {
            throw new IllegalArgumentException("Either up or down is allowed");
        }
        if (!mDevice.wait(Until.hasObject(By.res(getPackage(), "title").text(title)),
                SHORT_SLEEP_MS)) {
            throw new IllegalStateException("The title for PIN code not found: " + title);
        }

        // the PIN number starts from 0 and increases by going down
        for (char c : pinCode.toCharArray()) {
            int number = c - '0';
            // Note that the resource ID for the number changes by the direction to search
            String resId = (number == 0) ? "current_number"
                    : (direction == Direction.DOWN) ? "next_number" : "previous_number";
            mDPadHelper.pressDPad(direction, number);
            if (!mDevice.wait(
                    Until.hasObject(By.res(getPackage(), resId).text(String.valueOf(c))),
                    SHORT_SLEEP_MS)) {
                throw new UnknownUiException("Couldn't find the number:" + c);
            }
            mDPadHelper.pressDPadCenter();  // Move next
        }
        return true;
    }

    private boolean isValidPinCode(String pinCode) {
        final String PIN_CODE_FORMAT = "[0-9][0-9][0-9][0-9]";
        return pinCode != null && pinCode.matches(PIN_CODE_FORMAT);
    }

    /**
     * Setup expectations: None
     *
     * Starts the Settings activity
     */
    // TODO Move to a base or utility class that each test could access
    private void launchActivity() {
        launchActivity(ACTION_SETTINGS);
    }

    private void launchActivity(String action) {
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Log.d(LOG_TAG, "launchActivity intent=" + intent.toString());
        mInstrumentation.getContext().startActivity(intent);
    }
}
