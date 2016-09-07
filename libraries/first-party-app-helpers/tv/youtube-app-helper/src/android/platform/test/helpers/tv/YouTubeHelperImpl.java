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

package android.platform.test.helpers.tv;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.SystemClock;
import android.platform.test.helpers.AbstractLeanbackAppHelper;
import android.platform.test.helpers.exceptions.UiTimeoutException;
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class YouTubeHelperImpl extends AbstractLeanbackAppHelper {

    private static final String TAG = YouTubeHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.google.android.youtube.tv";
    private static final String RES_MAIN_ACTIVITY_ID = "top_layout";
    private static final long SHORT_SLEEP_MS = 5000;    // 5 seconds
    private static final long LONG_SLEEP_MS = 30000;    // 30 seconds
    private static final long LOADING_CONTENT_TIMEOUT_MS = 5000;


    public YouTubeHelperImpl(Instrumentation instrumentation) {
        super(instrumentation);
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
        return "YouTube";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BySelector getMainActivitySelector() {
        return By.res(UI_PACKAGE, RES_MAIN_ACTIVITY_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BySelector getBrowseHeadersSelector() {
        return By.res(UI_PACKAGE, "guide").hasChild(By.selected(true));
    }

    /**
     * Selects search orb.
     */
    public void selectSearchOrb() {
        returnToMainActivity();
        UiObject2 searchOrb = null;

        final int MAX_ATTEMPTS_SEARCH_ORB = 20;
        int attempt = 0;
        // Wait until the search orb appears at runtime.
        while (attempt++ < MAX_ATTEMPTS_SEARCH_ORB) {
            searchOrb = mDevice.wait(Until.findObject(
                    By.res(UI_PACKAGE, "title_orb").clickable(true)), SHORT_SLEEP_MS);
            if (searchOrb == null) {
                // Search orb could be found at the top of activity
                mDPadHelper.pressDPad(Direction.UP);
                continue;
            }
        }
        if (attempt == MAX_ATTEMPTS_SEARCH_ORB) {
            throw new UnknownUiException("Failed to select search orb");
        }
        searchOrb.click();
    }

    /**
     * Search for a given query text in YouTube app
     */
    public void search(String query) {
        selectSearchOrb();

        UiObject2 editText = mDevice.wait(
                Until.findObject(By.res(UI_PACKAGE, "lb_search_text_editor")),
                5 * 60 * 1000);
        if (editText == null) {
            throw new UnknownUiException("Search text editor not found");
        }
        if (!editText.isFocused()) {
            Log.d(TAG, "Search text editor is getting focus");
            mDevice.pressDPadRight();
            SystemClock.sleep(SHORT_SLEEP_MS);
        }
        editText.setText(query);
        mDevice.waitForIdle();
        mDevice.pressEnter();
        if (!waitForContentLoaded(SHORT_SLEEP_MS)) {
            throw new UiTimeoutException(
                    String.format("Failed to find the search results in %d (ms)", SHORT_SLEEP_MS));
        }
    }

    /**
     * Setup expectations: YouTube search result is open and the first result is focused.
     *
     * Open the first visible search result in the list and block until the search result
     * comes in the foreground.
     */
    public void openFirstSearchResult() {
        openSearchResultByIndex(0);
    }

    /**
     * Setup expectations: YouTube search result is open and the first result is focused.
     *
     * Open the (index)'th visible search result in the list and block until the search result
     * comes in the foreground.
     */
    public void openSearchResultByIndex(int index) {
        if (!isInSearchPage()) {
            throw new IllegalStateException("Must be in search page to select search results");
        }
        UiObject2 rowContent = mDevice.wait(Until.findObject(
                By.res(UI_PACKAGE, "row_content")), SHORT_SLEEP_MS);
        if (rowContent == null) {
            throw new IllegalStateException("No search results found");
        }

        // Select a content by index
        UiObject2 focused = rowContent.findObject(By.focused(true));
        if (focused == null) {
            throw new IllegalStateException("The search result is not selected");
        }
        UiObject2 current;
        for (int i = 0; i < index; ++i) {
            mDevice.pressDPadRight();
            SystemClock.sleep(SHORT_SLEEP_MS);
            current = rowContent.findObject(By.focused(true));
            if (focused.equals(current)) {
                Log.w(TAG, "openSearchResultByIndex: the index is out of bounds.");
                break;
            } else {
                focused = current;
            }
        }
        mDevice.pressDPadCenter();

        // Wait until the content is open
        if (!mDevice.wait(Until.gone(By.res(UI_PACKAGE, "lb_search_bar_items")),
                LOADING_CONTENT_TIMEOUT_MS)) {
            throw new UiTimeoutException("Opening search result timed out");
        }
    }

    /**
     * Setup expectations: Loading content in the app
     *
     * This method blocks until the content is loaded in a content row or a search row
     *
     * @param timeout wait timeout in milliseconds
     * @return true if the content is loaded within timeout, false otherwise
     */
    public boolean waitForContentLoaded(long timeout) {
        return mDevice.wait(
                Until.hasObject(By.res(UI_PACKAGE, "row_content").hasChild(By.selected(true))),
                timeout);
    }

    /**
     * Setup expectations: Sign-in page is open.
     *
     * Selects the account to use if no account has been set up.
     */
    public boolean signIn(String account) {
        if (!"Sign in".equals(getGuidanceTitleText())) {
            throw new IllegalStateException("This method should be called in the Sign-in page.");
        }
        if (selectGuidedAction(account) == null) {
            Log.e(TAG, String.format("No account matches: %s", account));
            return false;
        }
        mDPadHelper.pressDPadCenter();
        return mDevice.wait(Until.hasObject(getMainActivitySelector()), SHORT_SLEEP_MS);
    }

    /**
     * Setup expectations: On browse fragment. Sign out
     */
    public void signOut() {
        openSettings();
        if (!hasCardInRow("Sign out")) {
            throw new UnknownUiException("Sign out is not found");
        }
        mDPadHelper.pressDPadCenter();
        mDevice.wait(Until.findObject(By.res(getPackage(), "title_text").text("Sign in")),
                SHORT_SLEEP_MS);
    }

    /**
     * Setup expectations: The main activity is open.
     *
     * Returns true if no user is signed in the app.
     */
    public boolean isNoUserSignedIn() {
        // Some sections "Subscriptions", "History", "Purchases" are not available
        // with no user signed in
        final String[] SECTIONS_FOR_SIGNED_IN_USER = {"Subscriptions", "History", "Purchases"};
        for (String section : SECTIONS_FOR_SIGNED_IN_USER) {
            if (mDevice.hasObject(By.res(getPackage(), "row_header").text(section))) {
                Log.d(TAG, "The section for a signed in user is found: " + section);
                return false;
            }
        }

        // Open Settings and confirm that the Sign-in card is shown
        openSettings();
        return hasCardInRow("Sign in");
    }

    /**
     * Setup expectations: On browse fragment
     *
     * Returns the name of account currently signed in
     */
    public String getSignInUserName() {
        openSettings();
        return getCardContentText("Sign out");
    }

    private boolean isInSearchPage() {
        return mDevice.hasObject(By.res(UI_PACKAGE, "search_fragment"));
    }

    /**
     * @return true if YouTube plays a video in the foreground
     */
    public boolean isInVideoPlayback() {
        return isInVideoPlayback(0);
    }

    private boolean isInVideoPlayback(long timeoutMs) {
        if (!isAppInForeground()) {
            Log.w(TAG, "YouTube was closed.");
            return false;
        }
        return mDevice.wait(Until.hasObject(By.res(UI_PACKAGE, "watch_player")), timeoutMs);
    }

    /**
     * Open the Popular on YouTube section
     */
    public void openPopularOnYouTube() {
        openHeader("Popular on YouTube");
    }

    public void openHome() {
        openHeader("Home");
    }

    public void openSettings() {
        openHeader("Settings");
    }

    private UiObject2 getFocusedVideoCard() {
        BySelector cardSelector = By.focused(true).hasChild(
                By.res(getPackage(), "image_card"));
        return mDevice.wait(Until.findObject(cardSelector), SHORT_SLEEP_MS);
    }

    /**
     * Setup expectations: YouTube is open with a focused video.
     * @return the duration in milliseconds of a focused video
     */
    public long getFocusedVideoDuration() {
        UiObject2 card = getFocusedVideoCard();
        if (card == null) {
            throw new IllegalStateException("Could not find the video card");
        }
        // Get video length
        UiObject2 length = card.findObject(By.res(getPackage(), "duration"));
        if (length == null) {
            throw new UnknownUiException("Could not find an object of video duration");
        }
        String durationText = length.getText();
        if (durationText == null || "".equals(durationText)) {
            throw new UnknownUiException("Could not find length of the selected video");
        }

        String formatString = (durationText.split(":").length == 3) ? "HH:mm:ss" : "mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        long durationMs;
        try {
            durationMs = format.parse(length.getText()).getTime();
            Log.d(TAG, String.format("Video length is %d in milliseconds", durationMs));
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Failed to parse video length '%s'",
                    length.getText()));
        }
        return durationMs;
    }

    /**
     * Setup expectations: YouTube is open with a focused video.
     * @return the title text of a focused video
     */
    public String getFocusedVideoTitleText() {
        UiObject2 card = getFocusedVideoCard();
        if (card == null) {
            throw new IllegalStateException("Could not find the video card");
        }
        return card.findObject(By.res(getPackage(), "title_text")).getText();
    }

    /**
     * Setup expectations: YouTube is open with a focused video.
     * @return the content text of a focused video
     */
    public String getFocusedVideoContentText() {
        UiObject2 card = getFocusedVideoCard();
        if (card == null) {
            throw new IllegalStateException("Could not find the video card");
        }
        return card.findObject(By.res(getPackage(), "content_text")).getText();
    }

    /**
     * Setup expectations: YouTube is open with a focused video.
     * @param timeoutMs Timeout in milliseconds to play a video. Set to 0 if it plays until the
     *                  end.
     * @return true if it plays without an error during a given duration.
     */
    public boolean playFocusedVideo(long timeoutMs) {
        long durationMs = getFocusedVideoDuration();
        Log.i(TAG, String.format("Playing a video for %d (ms)", timeoutMs));

        // Play the video
        mDevice.pressDPadCenter();
        if (!isInVideoPlayback(SHORT_SLEEP_MS)) {
            throw new IllegalStateException("Must be in video playback");
        }

        // Wait for the given duration
        if (timeoutMs <= 0 || timeoutMs > durationMs) {
            timeoutMs = durationMs;
        }
        SystemClock.sleep(timeoutMs);
        return true;
    }

    // TODO Move to a base or utility class that each test could access.
    public void launchActivity() {
        Intent intent = mInstrumentation.getContext().getPackageManager()
                .getLaunchIntentForPackage(UI_PACKAGE);
        Log.d(TAG, "launchActivity intent=" + intent.toString());
        mInstrumentation.getContext().startActivity(intent);
    }
}
