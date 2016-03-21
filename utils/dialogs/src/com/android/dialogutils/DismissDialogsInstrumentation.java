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
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.aupt.UiWatchers;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

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

import java.io.File;
import java.io.IOException;
import java.lang.NoSuchMethodException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.lang.ReflectiveOperationException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility to dismiss all predictable, relevant one-time dialogs
 */
public class DismissDialogsInstrumentation extends Instrumentation {
    private static final String LOG_TAG = DismissDialogsInstrumentation.class.getSimpleName();

    // Comma-separated value indicating for which apps to dismiss dialogs
    private static final String PARAM_APP = "apps";
    // Boolean to indicate if this should quit if any failure occurs
    private static final String PARAM_QUIT_ON_ERROR = "quitOnError";
    // Key for status bundles provided when running the preparer
    private static final String BUNDLE_DISMISSED_APP_KEY = "dismissedApp";
    private static final String BUNDLE_APP_ERROR = "appError";

    private Map<String, Class<? extends IStandardAppHelper>> mKeyHelperMap;
    private String[] mApps;
    private boolean mQuitOnError;
    private UiDevice mDevice;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        mKeyHelperMap = new HashMap<String, Class<? extends IStandardAppHelper>>();
        mKeyHelperMap.put("Chrome", ChromeHelperImpl.class);
        mKeyHelperMap.put("GoogleCamera", CameraHelperImpl.class);
        mKeyHelperMap.put("Gmail", GmailHelperImpl.class);
        mKeyHelperMap.put("Maps", MapsHelperImpl.class);
        mKeyHelperMap.put("Photos", PhotosHelperImpl.class);
        mKeyHelperMap.put("PlayMovies", PlayMoviesHelperImpl.class);
        mKeyHelperMap.put("PlayMusic", PlayMusicHelperImpl.class);
        mKeyHelperMap.put("PlayStore", PlayStoreHelperImpl.class);
        //mKeyHelperMap.put("Settings", SettingsHelperImpl.class);
        mKeyHelperMap.put("YouTube", YouTubeHelperImpl.class);

        String appsString = arguments.getString(PARAM_APP);
        if (appsString == null) {
            throw new IllegalArgumentException("Missing 'apps' parameter.");
        }
        mApps = appsString.split(",");

        String quitString = arguments.getString(PARAM_QUIT_ON_ERROR);
        if (quitString == null) {
            Log.e(LOG_TAG, "No 'quitOnError' parameter. Defaulting not to quit on error.");
            mQuitOnError = false;
        } else {
            mQuitOnError = "true".equals(quitString);
        }

        start();
    }

    @Override
    public void onStart() {
        super.onStart();

        UiWatchers watcherManager = new UiWatchers();
        watcherManager.registerAnrAndCrashWatchers(this);

        try {
            UiDevice.getInstance(this).setOrientationNatural();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.toString());
        }

        for (String app : mApps) {
            Log.e(LOG_TAG, String.format("Dismissing dialogs for app, %s", app));
            try {
                if (!dismissDialogs(app)) {
                    throw new IllegalArgumentException(
                            String.format("Unrecognized app \"%s\"", mApps));
                } else {
                    sendStatusUpdate(Activity.RESULT_OK, app);
                }
            } catch (ReflectiveOperationException e) {
                if (mQuitOnError) {
                    quitWithError(app, e.toString());
                } else {
                    sendStatusUpdate(Activity.RESULT_CANCELED, app);
                    Log.e(LOG_TAG, e.toString());
                    throw new RuntimeException("Reflection exception. Please investigate!");
                }
            } catch (RuntimeException e) {
                if (mQuitOnError) {
                    quitWithError(app, e.toString());
                } else {
                    sendStatusUpdate(Activity.RESULT_CANCELED, app);
                    Log.e(LOG_TAG, e.toString());
                    Log.e(LOG_TAG, "Skipping RuntimeException. Proceeding with dialog dismissal.");
                }
            } catch (AssertionError e) {
                if (mQuitOnError) {
                    quitWithError(app, e.toString());
                } else {
                    sendStatusUpdate(Activity.RESULT_CANCELED, app);
                    Log.e(LOG_TAG, e.toString());
                    Log.e(LOG_TAG, "Skipping AssertionError. Proceeding with dialog dismissal.");
                }
            }

            // Always return to the home page after dismissal
            UiDevice.getInstance(this).pressHome();
        }

        watcherManager.removeAnrAndCrashWatchers(this);

        finish(Activity.RESULT_OK, new Bundle());
    }

    private boolean dismissDialogs(String app) throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        if (mKeyHelperMap.containsKey(app)) {
            Class<? extends IStandardAppHelper> appHelperClass = mKeyHelperMap.get(app);
            IStandardAppHelper helper =
                    appHelperClass.getDeclaredConstructor(Instrumentation.class).newInstance(this);

            SystemClock.sleep(5000);
            takeScreenDump(app, "-dialog-img1");
            helper.open();
            SystemClock.sleep(5000);
            takeScreenDump(app, "-dialog-img2");
            helper.dismissInitialDialogs();
            SystemClock.sleep(5000);
            takeScreenDump(app, "-dialog-img3");
            helper.exit();
            SystemClock.sleep(5000);
            takeScreenDump(app, "-dialog-img4");
            return true;
        } else {
            return false;
        }
    }

    private void sendStatusUpdate(int code, String app) {
        Bundle result = new Bundle();
        result.putString(BUNDLE_DISMISSED_APP_KEY, app);
        sendStatus(code, result);
    }

    private void quitWithError(String app, String error) {
        Bundle result = new Bundle();
        result.putString(BUNDLE_DISMISSED_APP_KEY, app);
        result.putString(BUNDLE_APP_ERROR, error);
        finish(Activity.RESULT_CANCELED, result);
    }

    private void takeScreenDump(String app, String suffix) {
        try {
            File scr = new File("/sdcard/" + app + suffix + ".png");
            File uix = new File("/sdcard/" + app + suffix + ".uix");
            UiDevice.getInstance(this).takeScreenshot(scr);
            UiDevice.getInstance(this).dumpWindowHierarchy(uix);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }
}
