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

package com.android.test.util.dismissdialogs;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;

import com.android.support.test.helpers.IStandardAppHelper;
import com.android.support.test.helpers.ChromeHelperImpl;
import com.android.support.test.helpers.CameraHelperImpl;
import com.android.support.test.helpers.GmailHelperImpl;
import com.android.support.test.helpers.MapsHelperImpl;
import com.android.support.test.helpers.PhotosHelperImpl;
import com.android.support.test.helpers.PlayMoviesHelperImpl;
import com.android.support.test.helpers.PlayMusicHelperImpl;
import com.android.support.test.helpers.PlayStoreHelperImpl;
import com.android.support.test.helpers.YouTubeHelperImpl;

/**
 * A utility to dismiss all predictable, relevant one-time dialogs
 */
public class DismissDialogsInstrumentation extends Instrumentation {
    private static final String CHROME_KEY = "Chrome";
    private static final String GOOGLE_CAMERA_KEY = "GoogleCamera";
    private static final String GMAIL_KEY = "Gmail";
    private static final String MAPS_KEY = "Maps";
    private static final String PHOTOS_KEY = "Photos";
    private static final String PLAY_MOVIES_KEY = "PlayMovies";
    private static final String PLAY_MUSIC_KEY = "PlayMusic";
    private static final String PLAY_STORE_KEY = "PlayStore";
    private static final String SETTINGS_KEY = "Settings";
    private static final String YOUTUBE_KEY = "YouTube";

    // Comma-separated value indicating for which apps to dismiss dialogs
    private static final String PARAM_APP = "apps";

    private String[] mApps;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        String appsString = arguments.getString(PARAM_APP);
        if (appsString == null) {
            throw new IllegalArgumentException("Missing 'apps' parameter");
        }
        mApps = appsString.split(",");

        start();
    }

    @Override
    public void onStart() {
        super.onStart();

        for (String app : mApps) {
            if (!dismissDialogs(app)) {
                throw new IllegalArgumentException(
                        String.format("Unrecognized app \"%s\"", mApps));
            }
        }

        finish(Activity.RESULT_OK, new Bundle());
    }

    private boolean dismissDialogs(String app) {
        IStandardAppHelper helper = null;

        switch (app) {
            case CHROME_KEY:
                helper = new ChromeHelperImpl(this);
                break;

            case GOOGLE_CAMERA_KEY:
                helper = new CameraHelperImpl(this);
                break;

            case GMAIL_KEY:
                helper = new GmailHelperImpl(this);
                break;

            case MAPS_KEY:
                helper = new MapsHelperImpl(this);
                break;

            case PHOTOS_KEY:
                helper = new PhotosHelperImpl(this);
                break;

            case PLAY_MOVIES_KEY:
                helper = new PlayMoviesHelperImpl(this);
                break;

            case PLAY_MUSIC_KEY:
                helper = new PlayMusicHelperImpl(this);
                break;

            case PLAY_STORE_KEY:
                helper = new PlayStoreHelperImpl(this);
                break;

            case SETTINGS_KEY:
                // TODO: Implement (@ashitas)
                break;

            case YOUTUBE_KEY:
                helper = new YouTubeHelperImpl(this);
                break;
        }

        if (helper != null) {
            helper.open();
            helper.dismissInitialDialogs();
            helper.exit();
            return true;
        } else {
            return false;
        }
    }
}
