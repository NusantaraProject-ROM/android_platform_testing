/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.support.test.helpers;

import android.app.Instrumentation;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;

import junit.framework.Assert;

public class YouTubeHelperImpl extends AbstractYouTubeHelper {
    private static final String UI_CONTAINER_ID = "results";
    private static final String UI_HEADER_ID = "heading";
    private static final String UI_PACKAGE_NAME = "com.google.android.youtube";
    private static final String UI_PROGRESS_ID = "load_progress";
    private static final String UI_VIDEO_CARD_ID = "video_info_view";

    private static final long MAX_HOME_LOAD_WAIT = 30 * 1000;
    private static final long MAX_VIDEO_LOAD_WAIT = 30 * 1000;

    private static final long STANDARD_DIALOG_WAIT = 5000;

    public YouTubeHelperImpl(Instrumentation instr) {
        super(instr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return "com.google.android.youtube";
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
    public void dismissInitialDialogs() {
        BySelector dialog1 = By.text("OK");
        // Dismiss the splash screen that might appear on first start.
        UiObject2 splash = mDevice.wait(Until.findObject(dialog1), STANDARD_DIALOG_WAIT);
        if (splash != null) {
            splash.click();
        }
        mDevice.wait(Until.gone(dialog1), STANDARD_DIALOG_WAIT);

        UiObject2 laterButton = mDevice.wait(Until.findObject(
                By.res(UI_PACKAGE_NAME, "later_button")), STANDARD_DIALOG_WAIT);
        if (laterButton != null) {
            laterButton.clickAndWait(Until.newWindow(), STANDARD_DIALOG_WAIT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playFirstVideo() {
        if (!isHomePage()) {
            if (isLoading()) {
                Assert.fail("Difficulty loading search results due to poor WiFi");
            } else {
                Assert.fail("YouTube does not support playing videos from this page or an " +
                        "unexpected automation failure occurred.");
            }
        } else {
            for (int i = 0; i < 3; i++) {
                UiObject2 video = getFirstVideo();
                if (video != null) {
                    video.click();
                    waitForVideoToLoad();
                    return;
                } else {
                    scrollHomePage(Direction.DOWN);
                }
            }
        }
    }

    private void scrollHomePage(Direction dir) {
        UiObject2 scrollContainer = getHomePageContainer();
        if (scrollContainer != null) {
            scrollContainer.scroll(dir, 1.0f);
            mDevice.waitForIdle();
        } else {
            Assert.fail("No valid scrolling mechanism found.");
        }
    }

    private boolean isLoading() {
        return mDevice.hasObject(By.res(UI_PACKAGE_NAME, UI_PROGRESS_ID));
    }

    private boolean isHomePage() {
        return (getHomePageContainer() != null);
    }

    private UiObject2 getHomePageContainer() {
        return mDevice.wait(Until.findObject(By.res(UI_PACKAGE_NAME, UI_CONTAINER_ID)),
                MAX_HOME_LOAD_WAIT);
    }

    private UiObject2 getFirstVideo() {
        return mDevice.findObject(By.res(UI_PACKAGE_NAME, UI_VIDEO_CARD_ID));
    }

    private void waitForVideoToLoad() {
        mDevice.wait(Until.findObject(By.res(UI_PACKAGE_NAME, UI_HEADER_ID)), MAX_VIDEO_LOAD_WAIT);
    }
}
