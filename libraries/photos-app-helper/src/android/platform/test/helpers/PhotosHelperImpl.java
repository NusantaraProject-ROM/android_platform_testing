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

package android.platform.test.helpers;

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

import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Assert;

public class PhotosHelperImpl extends AbstractPhotosHelper {
    private static final String LOG_TAG = PhotosHelperImpl.class.getSimpleName();

    private static final long APP_LOAD_WAIT = 7500;
    private static final long HACKY_WAIT = 2500;
    private static final long UI_NAVIGATION_WAIT = 5000;

    private static final Pattern UI_PHOTO_DESC = Pattern.compile("^Photo.*");

    private static final String UI_PACKAGE_NAME = "com.google.android.apps.photos";
    private static final String UI_PHOTOS_TEXT = "Photos";
    private static final String UI_PHOTO_VIEW_PAGER_ID = "photo_view_pager";

    public PhotosHelperImpl(Instrumentation instr) {
        super(instr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return "com.google.android.apps.photos";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "Photos";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
        // Target Photos version 1.4.0.102264174
        SystemClock.sleep(APP_LOAD_WAIT);
        // Press 'GET STARTED'
        UiObject2 getStartedButton = null;
        for (int retries = 3; retries > 0; retries--) {
            getStartedButton = mDevice.wait(Until.findObject(
                    By.res(UI_PACKAGE_NAME, "get_started")), HACKY_WAIT);
            if (getStartedButton == null) {
                Pattern getStartedWords = Pattern.compile("GET STARTED", Pattern.CASE_INSENSITIVE);
                getStartedButton = mDevice.wait(Until.findObject(By.text(getStartedWords)), HACKY_WAIT);
            }
            if (getStartedButton != null) {
                getStartedButton.click();
                mDevice.waitForIdle();
                break;
            }
        }
        if (getStartedButton == null) {
            Log.e(LOG_TAG, "Unable to find GET STARTED button.");
        }
        // Address dialogs with an account vs. without an account
        Pattern signInWords = Pattern.compile("Sign in", Pattern.CASE_INSENSITIVE);
        boolean hasAccount = !mDevice.hasObject(By.text(signInWords));
        if (!hasAccount) {
            // Select 'NO THANKS' if no account exists
            Pattern noThanksWords = Pattern.compile("No thanks", Pattern.CASE_INSENSITIVE);
            UiObject2 noThanksButton = mDevice.findObject(By.text(noThanksWords));
            if (noThanksButton != null) {
                noThanksButton.click();
                mDevice.waitForIdle();
            } else {
                Log.e(LOG_TAG, "Unable to find NO THANKS button.");
            }
        } else {
            // Press 'CONTINUE' twice
            for (int repeat = 0; repeat < 2; repeat++) {
                Pattern continueWords = Pattern.compile("Continue", Pattern.CASE_INSENSITIVE);
                UiObject2 continueButton = mDevice.findObject(By.text(continueWords));
                if (continueButton != null) {
                    continueButton.click();
                    mDevice.waitForIdle();
                } else {
                    Log.e(LOG_TAG, "Unable to find CONTINUE button.");
                }
            }
            // Press the next button (arrow and check mark) four consecutive times
            for (int repeat = 0; repeat < 4; repeat++) {
                UiObject2 nextButton = mDevice.findObject(By.desc("Next"));
                if (nextButton != null) {
                    nextButton.click();
                    mDevice.waitForIdle();
                } else {
                    Log.e(LOG_TAG, "Unable to find arrow or check mark buttons.");
                }
            }
            // Dismiss the fullscreen dialog
            openFirstClip();
            mDevice.pressBack();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openFirstClip() {
        for (int i = 0; i < 3; i++) {
            // Select and open the first clip if found
            UiObject2 clip = getFirstClip();
            if (clip != null) {
                clip.click();
                mDevice.wait(Until.findObject(
                        By.res(UI_PACKAGE_NAME, "photos_videoplayer_play_button_holder")), 2000);
                return;
            }

            // No visible clips; scroll DOWN if possible
            if (!scrollContainer(Direction.DOWN)) {
                break;
            }
        }

        Assert.fail("Unable to find any clips to play.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pauseClip() {
        UiObject2 holder = mDevice.findObject(
                By.res(UI_PACKAGE_NAME, "photos_videoplayer_play_button_holder"));
        if (holder != null) {
            holder.click();
        } else {
            Assert.fail("Unable to find pause button holder.");
        }

        UiObject2 pause = mDevice.wait(Until.findObject(
                By.res(UI_PACKAGE_NAME, "photos_videoplayer_pause_button")), 2500);
        if (pause != null) {
            pause.click();
            mDevice.wait(Until.findObject(By.desc("Play video")), 2500);
        } else {
            Assert.fail("Unable to find pause button.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playClip() {
        UiObject2 play = mDevice.findObject(By.desc("Play video"));
        if (play != null) {
            play.click();
            mDevice.wait(Until.findObject(
                    By.res(UI_PACKAGE_NAME, "photos_videoplayer_pause_button")), 2500);
        } else {
            Assert.fail("Unable to find play button.");
        }
    }

    private boolean scrollContainer(Direction dir) {
        UiObject2 container = mDevice.findObject(By.res(UI_PACKAGE_NAME, "photo_container"));
        if (container == null) {
            return false;
        }

        return container.scroll(dir, 1.0f);
    }

    private UiObject2 getFirstClip() {
        return mDevice.findObject(By.descStartsWith("Video"));
    }

    private boolean isOnMainScreen() {
        return mDevice.hasObject(By.pkg(UI_PACKAGE_NAME).text(UI_PHOTOS_TEXT));
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void goToMainScreen() {
        for (int retriesRemaining = 5; retriesRemaining > 0 && !isOnMainScreen();
                --retriesRemaining) {
            mDevice.pressBack();
            mDevice.waitForIdle();
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void openPicture(int index) {
        Assert.assertTrue("Not on home page", isOnMainScreen());

        List<UiObject2> photos = mDevice.findObjects(By.pkg(UI_PACKAGE_NAME).desc(UI_PHOTO_DESC));
        Assert.assertNotNull("Could not find photos", photos);
        Assert.assertTrue("Photo index out of bounds",
                index >= 0 && index < photos.size());

        UiObject2 photo = photos.get(index);
        photo.click();
        mDevice.wait(Until.hasObject(By.res(UI_PACKAGE_NAME, UI_PHOTO_VIEW_PAGER_ID)),
                UI_NAVIGATION_WAIT);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void scrollAlbum(Direction direction) {
        Assert.assertTrue("Scroll direction must be LEFT or RIGHT",
                Direction.LEFT.equals(direction) || Direction.RIGHT.equals(direction));

        UiObject2 scrollContainer = mDevice.findObject(
                By.res(UI_PACKAGE_NAME, UI_PHOTO_VIEW_PAGER_ID));
        Assert.assertNotNull("Could not find scroll container", scrollContainer);

        scrollContainer.scroll(direction, 1.0f);
    }
}
