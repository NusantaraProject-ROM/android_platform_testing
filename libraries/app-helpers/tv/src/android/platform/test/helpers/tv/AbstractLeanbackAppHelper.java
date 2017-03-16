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

package android.platform.test.helpers;

import android.app.Instrumentation;
import android.platform.test.helpers.exceptions.UiTimeoutException;
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.platform.test.utils.DPadUtil;
import android.support.test.launcherhelper.ILeanbackLauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;

/**
 *  This app helper handles the following important widgets for TV apps:
 *  BrowseFragment, DetailsFragment, SearchFragment and PlaybackOverlayFragment
 */
public abstract class AbstractLeanbackAppHelper extends AbstractStandardAppHelper {

    private static final String TAG = AbstractLeanbackAppHelper.class.getSimpleName();
    private static final long OPEN_ROW_CONTENT_WAIT_TIME_MS = 5000;
    private static final long OPEN_HEADER_WAIT_TIME_MS = 5000;
    private static final int OPEN_SIDE_PANEL_MAX_ATTEMPTS = 5;
    private static final long MAIN_ACTIVITY_WAIT_TIME_MS = 250;
    private static final long SELECT_WAIT_TIME_MS = 5000;
    private static final long FOCUS_WAIT_TIME_MS = 100;

    // The notable widget classes in Leanback Library
    public enum Widget {
        BROWSE_HEADERS_FRAGMENT,
        BROWSE_ROWS_FRAGMENT,
        DETAILS_FRAGMENT,
        SEARCH_FRAGMENT,
        VERTICAL_GRID_FRAGMENT,
        GUIDED_STEP_FRAGMENT,
        PLAYBACK_OVERLAY_FRAGMENT,
        ERROR_FRAGMENT
    }

    protected DPadUtil mDPadUtil;
    public ILeanbackLauncherStrategy mLauncherStrategy;


    public AbstractLeanbackAppHelper(Instrumentation instr) {
        super(instr);
        mDPadUtil = new DPadUtil(instr);
        mLauncherStrategy = LauncherStrategyFactory.getInstance(
                mDevice).getLeanbackLauncherStrategy();
        mLauncherStrategy.setInstrumentation(instr);
    }

    /**
     * @return {@link BySelector} describing the row headers (in the left pane) in
     * the Browse fragment
     */
    protected BySelector getBrowseHeadersSelector() {
        return By.res(getPackage(), "browse_headers").hasChild(By.selected(true));
    }

    /**
     * @return {@link BySelector} describing a row content (in the right pane) selected in
     * the Browse fragment
     */
    protected BySelector getBrowseRowsSelector() {
        return By.res(getPackage(), "row_content").hasChild(By.selected(true));
    }

    /**
     * @return {@link BySelector} describing the Details fragment
     */
    protected BySelector getDetailsFragmentSelector() {
        return By.res(getPackage(), "details_fragment");
    }

    /**
     * @return {@link BySelector} describing the Search fragment
     */
    protected BySelector getSearchFragmentSelector() {
        return By.res(getPackage(), "lb_search_frame");
    }

    /**
     * @return {@link BySelector} describing the Vertical grid fragment
     */
    protected BySelector getVerticalGridFragmentSelector() {
        return By.res(getPackage(), "grid_frame");
    }

    /**
     * @return {@link BySelector} describing the Guided step fragment
     */
    protected BySelector getGuidedStepFragmentSelector() {
        return By.res(getPackage(), "guidedactions_list");
    }

    /**
     * @return {@link BySelector} describing the Playback overlay fragment
     */
    protected BySelector getPlaybackOverlayFragmentSelector() {
        return By.res(getPackage(), "playback_controls_dock");
    }

    /**
     * @return {@link BySelector} describing the Error fragment
     */
    protected BySelector getErrorFragmentSelector() {
        return By.res(getPackage(), "error_frame");
    }

    /**
     * @return {@link BySelector} describing the main activity (mostly the Browse fragment).
     * Note that not every application has its main activity, so the override is optional.
     */
    protected BySelector getMainActivitySelector() {
        return null;
    }

    // TODO Move waitForOpen and open to AbstractStandardAppHelper
    /**
     * Setup expectation: None. Waits for the application to begin running.
     * @param timeoutMs
     * @return true if the application is open successfully
     */
    public boolean waitForOpen(long timeoutMs) {
        return mDevice.wait(Until.hasObject(By.pkg(getPackage()).depth(0)), timeoutMs);
    }

    /**
     * Setup expectation: On the launcher home screen.
     * <p>
     * Launches the desired application and wait for it to begin running before returning.
     * </p>
     * @param timeoutMs
     */
    public void open(long timeoutMs) {
        open();
        if (!waitForOpen(timeoutMs)) {
            throw new UiTimeoutException(String.format("Timed out to open a target package %s:"
                    + " %d(ms)", getPackage(), timeoutMs));
        }
    }

    /**
     * Setup expectation: Side panel is selected on the Browse fragment
     * <p>
     * Best effort attempt to go to the row headers, and open the selected header.
     * </p>
     */
    public void openHeader(String headerName) {
        openBrowseHeaders();
        // header is focused; it should not be after pressing the DPad
        selectHeader(headerName);
        mDevice.pressDPadCenter();

        // Test for focus change and selection result
        BySelector rowContent = getBrowseRowsSelector();
        if (!mDevice.wait(Until.hasObject(rowContent), OPEN_ROW_CONTENT_WAIT_TIME_MS)) {
            throw new UnknownUiException(
                    String.format("Failed to find row content that matches the header: %s",
                            headerName));
        }
        Log.v(TAG, "Successfully opened header");
    }

    /**
     * Setup expectation: On navigation screen on the Browse fragment
     *
     * Best effort attempt to open the row headers in the Browse fragment.
     * @param onMainActivity True if it opens the side panel on app's main activity.
     */
    public void openBrowseHeaders(boolean onMainActivity) {
        if (onMainActivity) {
            returnToMainActivity();
        }
        int attempts = 0;
        while (!waitForBrowseHeadersSelected(OPEN_HEADER_WAIT_TIME_MS)
                && attempts++ < OPEN_SIDE_PANEL_MAX_ATTEMPTS) {
            mDevice.pressDPadLeft();
        }
        if (attempts == OPEN_SIDE_PANEL_MAX_ATTEMPTS) {
            throw new UnknownUiException("Failed to open side panel");
        }
    }

    public void openBrowseHeaders() {
        openBrowseHeaders(false);
    }

    /**
     * Select target item through the container in the given direction.
     * @param container
     * @param target
     * @param direction
     * @return the focused object
     */
    public UiObject2 select(UiObject2 container, BySelector target, Direction direction) {
        if (container == null) {
            throw new IllegalArgumentException("The container should not be null.");
        }
        UiObject2 focus = waitForFocus(container, FOCUS_WAIT_TIME_MS, true);
        while (!focus.hasObject(target)) {
            UiObject2 prev = focus;
            mDPadUtil.pressDPad(direction);
            focus = container.findObject(By.focused(true));
            if (focus == null) {
                mDPadUtil.pressDPad(direction);
                focus = container.findObject(By.focused(true));
            }
            if (focus.equals(prev)) {
                // It reached at the end, but no target is found.
                return null;
            }
        }
        return focus;
    }

    public UiObject2 waitForFocus(UiObject2 object) {
        // By default this method returns immediately and throws an Exception if no focus is found
        return waitForFocus(object, 0, true);
    }

    /**
     * Wait for given object to have an element that is focused.
     * @param object {@link UiObject2} under which it searches for an element that is focused.
     *               Null if it searches through the entire hierarchy of accessibility nodes
     * @param timeoutMs Maximum amount of time to wait in milliseconds
     * @param throwIfFail Throw {@link IllegalStateException} if no element under the object is
     *                    focused in a given timeout
     * @return The {@link UiObject2} that has a focused element, or null if no focus
     */
    public UiObject2 waitForFocus(UiObject2 object, long timeoutMs, boolean throwIfFail) {
        UiObject2 focused;
        if (object == null) {
            focused = mDevice.wait(Until.findObject(By.focused(true)), timeoutMs);
        } else {
            focused = object.wait(Until.findObject(By.focused(true)), timeoutMs);
        }
        if (throwIfFail && focused == null) {
            throw new IllegalStateException("The object should have a focused descendant.");
        }
        return focused;
    }

    /**
     * Setup expectation: On guided fragment.
     * <p>
     * Best effort attempt to select a given guided action.
     * </p>
     */
    public UiObject2 selectGuidedAction(String action) {
        assertWidgetEquals(Widget.GUIDED_STEP_FRAGMENT);
        UiObject2 container = mDevice.wait(
                Until.findObject(
                        By.res(getPackage(), "guidedactions_list").hasChild(By.focused(true))),
                SELECT_WAIT_TIME_MS);
        // Search down, then up
        BySelector selector = By.res(getPackage(), "guidedactions_item_title").text(action);
        UiObject2 focused = select(container, selector, Direction.DOWN);
        if (focused != null) {
            return focused;
        }
        focused = select(container, selector, Direction.UP);
        if (focused != null) {
            return focused;
        }
        throw new UnknownUiException(String.format("Failed to select guided action: %s", action));
    }

    /**
     * Setup expectation: On guided fragment. Return the string in guidance title.
     */
    public String getGuidanceTitleText() {
        assertWidgetEquals(Widget.GUIDED_STEP_FRAGMENT);
        UiObject2 object = mDevice.wait(
                Until.findObject(By.res(getPackage(), "guidance_title")), SELECT_WAIT_TIME_MS);
        return object.getText();
    }

    /**
     * Setup expectation: On row fragment.
     * @param title of the card
     * @return UIObject2 for the focusable card that matches a given name in title
     */
    private UiObject2 getCardInRowByTitle(String title) {
        assertWidgetEquals(Widget.BROWSE_ROWS_FRAGMENT);
        return mDevice.wait(Until.findObject(
                By.focused(true).hasDescendant(By.res(getPackage(), "title_text").text(title))),
                SELECT_WAIT_TIME_MS);
    }

    /**
     * Setup expectation: On row fragment.
     * @param title of the card
     * @return String text of content in a card that has a given name in title
     */
    public String getCardContentText(String title) {
        UiObject2 card = getCardInRowByTitle(title);
        if (card == null) {
            throw new IllegalStateException("Failed to find a card in row content " + title);
        }
        return card.findObject(By.res(getPackage(), "content_text")).getText();
    }

    /**
     * Setup expectation: On row fragment.
     * @param title of the card
     * @return true if it finds a card that matches a given name in title
     */
    public boolean hasCardInRow(String title) {
        return (getCardInRowByTitle(title) != null);
    }

    /**
     * Setup expectation: On row fragment.
     * <p>
     * Open a card that matches a given title in row content
     * </p>
     * @param title of the card
     */
    public void openCardInRow(String title) {
        assertWidgetEquals(Widget.BROWSE_ROWS_FRAGMENT);
        UiObject2 card = getCardInRowByTitle(title);
        if (card == null) {
            throw new IllegalStateException("Failed to find a card in row content " + title);
        }
        if (!card.isFocused()) {
            card.click();   // move a focus
            card = getCardInRowByTitle(title);
            if (card == null) {
                throw new IllegalStateException("Failed to find a card in row content " + title);
            }
        }
        mDPadUtil.pressDPadCenter();
        mDevice.wait(Until.gone(By.res(getPackage(), "title_text").text(title)),
                SELECT_WAIT_TIME_MS);
    }

    /**
     * Attempts to return to main activity with getMainActivitySelector()
     * by pressing the back button repeatedly and sleeping briefly to allow for UI slowness.
     */
    public void returnToMainActivity() {
        int maxBackAttempts = 10;
        BySelector selector = getMainActivitySelector();
        if (selector == null) {
            throw new IllegalStateException("getMainActivitySelector() should be overridden.");
        }
        while (!mDevice.wait(Until.hasObject(selector), MAIN_ACTIVITY_WAIT_TIME_MS)
                && maxBackAttempts-- > 0) {
            mDevice.pressBack();
        }
    }

    /**
     * Setup expectation: None.
     * <p>
     * Asserts that a given widget provided by the Support Library is shown on TV app.
     * </p>
     */
    public void assertWidgetEquals(Widget expected) {
        if (!hasWidget(expected)) {
            throw new UnknownUiException("No widget matches " + expected.name());
        }
    }

    private boolean hasWidget(Widget expected) {
        switch (expected) {
            case BROWSE_HEADERS_FRAGMENT:
                return mDevice.hasObject(getBrowseHeadersSelector());
            case BROWSE_ROWS_FRAGMENT:
                return mDevice.hasObject(getBrowseRowsSelector());
            case DETAILS_FRAGMENT:
                return mDevice.hasObject(getDetailsFragmentSelector());
            case SEARCH_FRAGMENT:
                return mDevice.hasObject(getSearchFragmentSelector());
            case VERTICAL_GRID_FRAGMENT:
                return mDevice.hasObject(getVerticalGridFragmentSelector());
            case GUIDED_STEP_FRAGMENT:
                return mDevice.hasObject(getGuidedStepFragmentSelector());
            case PLAYBACK_OVERLAY_FRAGMENT:
                return mDevice.hasObject(getPlaybackOverlayFragmentSelector());
            case ERROR_FRAGMENT:
                return mDevice.hasObject(getErrorFragmentSelector());
            default:
                Log.w(TAG, "Unable to find the widget in the list: " + expected.name());
                return false;
        }
    }

    @Override
    public void dismissInitialDialogs() {
        return;
    }

    private boolean waitForBrowseHeadersSelected(long timeoutMs) {
        return mDevice.wait(Until.hasObject(getBrowseHeadersSelector()), timeoutMs);
    }

    /**
     * Attempts to select a given header text three times with the backoff timeout each retry.
     * The timeout needs to be long enough if it runs under low bandwidth environments.
     */
    protected UiObject2 selectHeader(String headerName) {
        long waitMs = 10 * 1000;    // 10 sec
        int maxAttempts = 3;
        UiObject2 header;
        while (maxAttempts-- > 0) {
            header = selectHeader(headerName, waitMs);
            if (header != null) {
                return header;
            }
            waitMs = 2 * waitMs;
        }
        throw new UnknownUiException("Failed to select header : " + headerName);
    }

    protected UiObject2 selectHeader(String headerName, long waitMs) {
        UiObject2 container = mDevice.wait(
                Until.findObject(getBrowseHeadersSelector()), OPEN_HEADER_WAIT_TIME_MS);
        BySelector header = By.clazz(".TextView").text(headerName);

        // Wait until the row header text appears on screen at runtime.
        mDevice.wait(Until.findObject(header), waitMs);

        // Search down, then up to select the header
        UiObject2 focused = select(container, header, Direction.DOWN);
        if (focused != null) {
            return focused;
        }
        focused = select(container, header, Direction.UP);
        if (focused != null) {
            return focused;
        }
        return null;
    }

}