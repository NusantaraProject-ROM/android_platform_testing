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
import android.os.SystemClock;
import android.platform.test.helpers.AbstractLeanbackAppHelper;
import android.platform.test.helpers.CommandHelper;
import android.platform.test.helpers.exceptions.UiTimeoutException;
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlayMoviesHelperImpl extends AbstractLeanbackAppHelper {

    private static final String LOG_TAG = PlayMoviesHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.google.android.videos";
    private static final String RES_MAIN_ACTIVITY_ID = "browse_container_dock";
    private static final String RES_SEARCH_ORB_ID = "title_orb";
    private static final String RES_SEARCH_BOX_ID = "lb_search_text_editor";

    private static final String TEXT_MOVIES = "Movies";
    private static final String TEXT_MY_LIBRARY = "My library";
    private static final String TEXT_PLAY_TRAILER = "PLAY TRAILER";

    private static final long SHORT_SLEEP_MS = 5000;    // 5 seconds
    private static final long LONG_SLEEP_MS = 30000;    // 30 seconds

    private CommandHelper mCmdHelper;


    public PlayMoviesHelperImpl(Instrumentation instrumentation) {
        super(instrumentation);
        mCmdHelper = new CommandHelper(instrumentation);
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
        return "Play Movies & TV";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BySelector getMainActivitySelector() {
        return By.res(UI_PACKAGE, RES_MAIN_ACTIVITY_ID);
    }

    /**
     * Selects search orb. The app should be opened beforehand by calling open().
     */
    public void selectSearchOrb() {
        returnToMainActivity();

        // Wait until the search orb appears at runtime.
        UiObject2 searchOrb = mDevice.wait(
                Until.findObject(By.res(UI_PACKAGE, RES_SEARCH_ORB_ID).clickable(true)),
                SHORT_SLEEP_MS);
        if (searchOrb == null) {
            throw new UiTimeoutException("Failed to select search orb");
        }
        searchOrb.click();
    }

    /**
     * Searches for the given query and keep the search result open.
     * Play Movies app should be opened beforehand by calling open().
     *
     * @param query a search query string typed in Play Movies' search box.
     */
    public void search(String query) {
        selectSearchOrb();
        mDevice.waitForIdle();

        Log.v(LOG_TAG, "Searching for the movie: " + query);
        UiObject2 editText = mDevice.wait(Until.findObject(
                By.res(UI_PACKAGE, RES_SEARCH_BOX_ID)), SHORT_SLEEP_MS);
        if (editText == null) {
            throw new UnknownUiException("Search text editor not found");
        }

        int retries = 4;
        while(!editText.isFocused() && retries > 0) {
            mDevice.pressDPadRight();
            mDevice.waitForIdle();
            retries--;
        }

        // Set query and search
        editText.setText(query);
        SystemClock.sleep(SHORT_SLEEP_MS);

        mDevice.pressEnter();
        SystemClock.sleep(SHORT_SLEEP_MS);
    }

    /**
     * Finds a movie with the trailer from the search result and start playing.
     * search() should be called right before calling this method.
     */
    public UiObject2 searchForMovieWithTrailer() {
        mDevice.wait(Until.findObject(By.text(TEXT_MOVIES)), SHORT_SLEEP_MS);
        mDevice.pressDPadCenter();
        mDevice.waitForIdle();

        // Skip until a trailer is found from the result
        UiObject2 trailerButton = null;
        final int MAX_ATTEMPTS_SEARCH_TRAILERS = 5;
        for (long i = 0; i < MAX_ATTEMPTS_SEARCH_TRAILERS; i++) {
            trailerButton = getTrailerButton();
            if (trailerButton == null) {
                // The trailer was not found for the movie,
                // back and open the detail of the next movie
                mDevice.pressBack();
                mDevice.wait(Until.findObject(By.text(TEXT_MOVIES)), SHORT_SLEEP_MS);

                mDevice.pressDPadRight();
                SystemClock.sleep(SHORT_SLEEP_MS);

                mDevice.pressDPadCenter();
                mDevice.waitForIdle();
            } else {
                // The trailer was found for the movie
                break;
            }

        }

        SystemClock.sleep(SHORT_SLEEP_MS);
        return trailerButton;
    }

    public UiObject2 getTrailerButton() {
        return mDevice.wait(Until.findObject(By.text(TEXT_PLAY_TRAILER)), SHORT_SLEEP_MS);
    }

    /**
     * Setup expectations: Trailer is selected, and shown in details fragment.
     *
     * Play a trailer
     */
    public void playTrailerInDetails(long durationMs) {
        UiObject2 trailerButton = getTrailerButton();
        if (trailerButton == null) {
            throw new UnknownUiException("Trailer action not found");
        }
        trailerButton.click();

        // Using "Play trailer" to wait for the playback to start
        mDevice.wait(Until.gone(By.text(TEXT_PLAY_TRAILER)),
                SHORT_SLEEP_MS);

        // Using "Play trailer" button to wait until the trailer finishes
        trailerButton = mDevice.wait(
                Until.findObject(By.text(TEXT_PLAY_TRAILER)), durationMs);
        if (trailerButton == null) {
            throw new RuntimeException("Trailer too long or something went wrong");
        }
    }

    /**
     * Open My Library section
     */
    public void openMyLibrary() {
        returnToMainActivity();
        openHeader(TEXT_MY_LIBRARY);
    }

    /**
     * Setup expectations: None.
     * Open My Movies in My library section, wait for the list of movies to come.
     */
    public void openMyMoviesList() {
        openMyLibrary();
        if (getCardByNameInRowContent(TEXT_MOVIES) == null) {
            throw new UnknownUiException("Movies in My library not found");
        }
        mDevice.performActionAndWait(new Runnable() {
            @Override
            public void run() {
                mDevice.pressDPadCenter();
            }
        }, Until.newWindow(), SHORT_SLEEP_MS);
    }

    /**
     * Get a card with the given name in row_content
     *
     * @param title of the card
     * @return UIObject2 for the focusable button
     */
    private UiObject2 getCardByNameInRowContent(String title) {
        UiObject2 container = mDevice.findObject(
                By.res(getPackage(), "row_content").hasDescendant(By.focused(true)));
        return select(container, By.res(getPackage(), "title_text").text(title),
                Direction.RIGHT);
    }

    /**
     * Setup expectations: The movie(s) is listed in the Vertical grid fragment
     */
    public void selectTheFocusedMovieInVerticalGrid() {
        assertWidgetEquals(Widget.VERTICAL_GRID_FRAGMENT);
        mDevice.performActionAndWait(new Runnable() {
            @Override
            public void run() {
                mDevice.pressDPadCenter();
            }
        }, Until.newWindow(), SHORT_SLEEP_MS);
    }

    /**
     * Setup expectations: The movie to play is listed in the Details fragment
     *
     * Play the selected movies from beginning
     */
    public void playFromBeginning() {
        assertWidgetEquals(Widget.DETAILS_FRAGMENT);

        // Play from beginning
        UiObject2 actionButton = mDevice.wait(Until.findObject(By.clazz(".Button")),
                LONG_SLEEP_MS);
        if (actionButton == null) {
            throw new UnknownUiException("action button not found");
        }
        String selectedText = actionButton.getText();
        Log.v(LOG_TAG, String.format("Selected text is: %s", selectedText));
        while (!(selectedText.toLowerCase().equals("play from beginning") ||
                selectedText.toLowerCase().equals("play movie"))) {
            String prevText = selectedText;

            // Select the next item
            mDevice.pressDPadRight();

            // Make sure the text has changed
            selectedText = mDevice.findObject(By.clazz(".Button").focused(true)).getText();
            if (selectedText.equals(prevText)) {
                throw new UnknownUiException("'Play from beginning' or 'Play movie' not found");
            }
        }
        mDevice.pressDPadCenter();

        // Dismiss confirmation dialog if it's a rental movie
        UiObject2 yesButton = mDevice.wait(
                Until.findObject(By.res(UI_PACKAGE, "guidedactions_list")), SHORT_SLEEP_MS);
        if (yesButton != null) {
            mDevice.pressDPadCenter();
        }
    }

    /**
     * Get the current playback state for a given package that owns the media session.
     * @param packageName the package name of media session owner
     * @return
     * 0 = PlaybackState.STATE_NONE
     * 1 = PlaybackState.STATE_STOPPED
     * 2 = PlaybackState.STATE_PAUSED
     * 3 = PlaybackState.STATE_PLAYING
     */
    public int getPlaybackState(String packageName) {
        String output = mCmdHelper.executeDumpsysMediaSession();
        // Parse the output of dumpsys media_session.
        // Example :
        // LeanbackSampleApp com.example.android.tvleanback/LeanbackSampleApp
        //   package=com.example.android.tvleanback
        //   ...
        //   state=PlaybackState {state=3, position=0, buffered position=0, speed=1.0, updated=...}
        int playbackState = 0;
        int index = output.indexOf(String.format("package=%s", packageName));
        if (index == -1) {
            Log.w(LOG_TAG, String.format("No media session found for the package: %s", packageName));
            return playbackState;
        }
        final Pattern PLAYBACKSTATE_REGEX = Pattern.compile(
                "\\s*state=PlaybackState \\{state=(\\d+),.*");
        Matcher matcher = PLAYBACKSTATE_REGEX.matcher(output.substring(index));
        if (matcher.find()) {
            playbackState = Integer.parseInt(matcher.group(1));
            Log.i(LOG_TAG, String.format("PlaybackState=%s package=%s", playbackState, packageName));
        }
        return playbackState;
    }
}

