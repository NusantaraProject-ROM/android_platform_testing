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
import android.os.RemoteException;
import android.platform.test.helpers.AbstractLeanbackAppHelper;
import android.platform.test.helpers.DPadHelper;
import android.platform.test.helpers.IRecentsHelper;
import android.platform.test.helpers.exceptions.UiTimeoutException;
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;

/**
 * App helper implementation class for Recents activity in System UI
 */
public class SysUiRecentsHelperImpl extends AbstractLeanbackAppHelper implements IRecentsHelper {
    private static final String LOG_TAG = SysUiRecentsHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.android.systemui";

    private static final long RECENTS_SELECTION_TIMEOUT = 5000;
    private static final String TEXT_NO_ITEMS = "No recent items";

    private DPadHelper mDPadHelper;


    public SysUiRecentsHelperImpl(Instrumentation instrumentation) {
        super(instrumentation);
        mDPadHelper = DPadHelper.getInstance(instrumentation);
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
        throw new UnsupportedOperationException("This method is not supported for Recents");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        try {
            mDevice.pressRecentApps();
            mDevice.waitForIdle();
        } catch (RemoteException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        mDevice.pressHome();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flingRecents(Direction dir) {
        throw new UnsupportedOperationException("This method is not supported for Recents "
                + "in leanback library");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAll() {
        final int MAX_ATTEMPTS = 10;
        int attempt = 0;
        while (attempt < MAX_ATTEMPTS) {
            if (dismissTask() == false) {
                break;
            }
            ++attempt;
            // Once all tasks are dismissed, it exits to Home screen
            if (!isAppInForeground()) {
                break;
            }
        }
        Log.i(LOG_TAG, String.format("%d task(s) is dismissed.", attempt));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasContent() {
        if (!isAppInForeground()) {
            throw new IllegalStateException("Recents is not open.");
        }
        return (getTaskCountOnScreen() > 0);
    }

    /**
     * @return True if there is no content and it shows the "No recent items" text on screen.
     */
    public boolean hasNoRecentItems() {
        return mDevice.hasObject(By.text(TEXT_NO_ITEMS));
    }

    /**
     * Setup expectations: The target application is open in Recents.
     * <p>
     * Selects a given application.
     * </p>
     * @return True if it selects a given application.
     */
    public boolean selectTask(String appName) {
        boolean found = (findTask(appName) != null);
        if (!found) {
            Log.w(LOG_TAG, String.format("The application not found in Recents: %s", appName));
        }
        return found;
    }

    /**
     * Setup expectations: "Recents" is open.
     * @return the name for an application currently selected in Recents
     */
    public String getFocusedTaskName() {
        UiObject2 taskView = mDevice.wait(Until.findObject(getTaskViewSelector()),
                RECENTS_SELECTION_TIMEOUT);
        if (taskView == null) {
            throw new UiTimeoutException("No task view found in Recents");
        }
        return taskView.findObject(By.focused(true)).findObject(
                By.res(UI_PACKAGE, "card_title_text")).getText();
    }

    /**
     * Setup expectations: "Recents" is open.
     * @return the number of tasks on the Recents screen. This may differ from the number of
     * the actual tasks.
     */
    public int getTaskCountOnScreen() {
        return mDevice.findObject(getTaskViewSelector()).getChildCount();
    }

    /**
     * Returns a {@link BySelector} describing the task to be dismissed in Recents
     * @return
     */
    private BySelector getTaskDismissSelector() {
        // The text "Dismiss" appears only when the card is selected to be dismissed on leanback.
        return By.pkg(UI_PACKAGE).focused(true)
                .hasChild(By.res(UI_PACKAGE, "card_dismiss_text").text("Dismiss"));
    }

    /**
     * Returns a {@link BySelector} describing the task view in Recents
     * @return
     */
    private BySelector getTaskViewSelector() {
        return By.res(UI_PACKAGE, "task_list");
    }

    /**
     * Setup expectations: "Recents" is open. This method will dismiss a focused task in Recents.
     * @return True if a task is dismissed
     */
    public boolean dismissTask() {
        if (hasNoRecentItems()) {
            Log.i(LOG_TAG, "No recent items found");
            return false;
        }

        if (!mDevice.wait(Until.hasObject(getTaskViewSelector()), RECENTS_SELECTION_TIMEOUT)) {
            throw new UnknownUiException("No task view found in Recents");
        }
        UiObject2 task = mDevice.findObject(getTaskDismissSelector());
        if (task == null) {
            // Select a task to be dismissed again by pressing the down key
            mDevice.pressDPadDown();
            task = mDevice.wait(Until.findObject(getTaskDismissSelector()),
                    RECENTS_SELECTION_TIMEOUT);
            if (task == null) {
                throw new UnknownUiException("Dismiss button not found");
            }
        }
        mDevice.pressDPadCenter();
        // Confirm that the task is dismissed
        return mDevice.wait(Until.gone(getTaskDismissSelector()), RECENTS_SELECTION_TIMEOUT);
    }

    private UiObject2 findTask(String appName) {
        UiObject2 taskView = mDevice.wait(Until.findObject(getTaskViewSelector()),
                RECENTS_SELECTION_TIMEOUT);
        UiObject2 app;
        final int MAX_ATTEMPTS = 10;
        if ((app = findTask(taskView, appName, Direction.LEFT, MAX_ATTEMPTS)) != null) {
            return app;
        }
        if ((app = findTask(taskView, appName, Direction.RIGHT, MAX_ATTEMPTS)) != null) {
            return app;
        }
        return null;
    }

    private UiObject2 findTask(UiObject2 container, String appName, Direction direction,
            int maxAttempts) {
        UiObject2 focused = container.findObject(By.focused(true));
        if (focused == null) {
            throw new UnknownUiException("No focused item found in Recents");
        }
        String currentName = focused.findObject(By.res(UI_PACKAGE, "card_title_text")).getText();
        String nextName;
        int attempt = 0;
        boolean found = false;
        while (!(found = appName.equalsIgnoreCase(currentName)) && attempt++ < maxAttempts) {
            mDPadHelper.pressDPad(direction);
            nextName = container.findObject(By.focused(true)).findObject(
                    By.res(UI_PACKAGE, "card_title_text")).getText();
            if (currentName.equals(nextName)) {
                // It reaches to the end in this direction
                Log.d(LOG_TAG, String.format("%s not found in Recents until it reaches to the end",
                        appName));
                return null;
            } else {
                currentName = nextName;
            }
        }

        if (found) {
            return focused;
        }
        Log.d(LOG_TAG, String.format("%s not found in Recents by moving next %d times",
                appName, maxAttempts));
        return null;
    }
}

