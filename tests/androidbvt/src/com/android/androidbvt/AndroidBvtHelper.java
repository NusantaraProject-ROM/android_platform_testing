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

package com.android.androidbvt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DownloadManager;
import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.telecom.TelecomManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Assert;

/**
 * Defines constants & implements common methods to be used by Framework, SysUI, System e2e BVT
 * tests. Also ensures single instance of this object
 */
public class AndroidBvtHelper {
    public static final String CALCULATOR_PACKAGE = "com.google.android.calculator";
    public static final String CALCULATOR_ACTIVITY = "com.android.calculator2.Calculator";
    public static final String CAMERA_PACKAGE = "com.google.android.GoogleCamera";
    public static final String CAMERA2_PACKAGE = "com.android.camera2";
    public static final String DESKCLOCK_PACKAGE = "com.google.android.deskclock";
    public static final String GOOGLE_KB_PACKAGE = "com.google.android.inputmethod.latin";
    public static final String GOOGLE_KB_SVC = "com.android.inputmethod.latin.LatinIME";
    public static final String SETTINGS_PACKAGE = "com.android.settings";
    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";

    public static final int FULLSCREEN = 1;
    public static final int SPLITSCREEN = 3;

    private final static String DEFAULT_PING_SITE = "www.google.com";

    public static final String TEST_TAG = "AndroidBVT";
    public static final int SHORT_TIMEOUT = 1000;
    public static final int LONG_TIMEOUT = 5000;
    public static final String MARLIN = "marlin";
    public static final String SAILFISH = "sailfish";
    // 600dp is the threshold value for 7-inch tablets.
    private static final int TABLET_DP_THRESHOLD = 600;
    private static AndroidBvtHelper sInstance = null;
    private Context mContext = null;
    private UiDevice mDevice = null;
    private UiAutomation mUiAutomation = null;
    private ContentResolver mResolver = null;

    public AndroidBvtHelper(UiDevice device, Context context, UiAutomation uiAutomation) {
        mContext = context;
        mDevice = device;
        mUiAutomation = uiAutomation;
        mResolver = mContext.getContentResolver();
    }

    public static AndroidBvtHelper getInstance(UiDevice device, Context context,
            UiAutomation uiAutomation) {
        if (sInstance == null) {
            sInstance = new AndroidBvtHelper(device, context, uiAutomation);
        }
        return sInstance;
    }

    public TelecomManager getTelecomManager() {
        return (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
    }

    public WifiManager getWifiManager() {
        return (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public DownloadManager getDownloadManager() {
        return (DownloadManager) (DownloadManager) mContext
                .getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public UserManager getUserManager() {
        return (UserManager) (UserManager) mContext
                .getSystemService(Context.USER_SERVICE);
    }

    public InputMethodManager getInputMethodManager() {
        return (InputMethodManager) mContext
                .getSystemService(mContext.INPUT_METHOD_SERVICE);
    }

    /**
     * Only executes 'adb shell' commands that run in the same process as the runner. Converts
     * output of the command from ParcelFileDescriptior to user friendly list of strings
     * https://developer.android.com/reference/android/app/UiAutomation.html#executeShellCommand(
     * java.lang.String)
     */
    public List<String> executeShellCommand(String cmd) {
        if (cmd == null || cmd.isEmpty()) {
            return null;
        }
        List<String> output = new ArrayList<String>();
        ParcelFileDescriptor pfd = mUiAutomation.executeShellCommand(cmd);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(pfd.getFileDescriptor())))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
                Log.i(TEST_TAG, line);
            }
        } catch (IOException e) {
            Log.e(TEST_TAG, e.getMessage());
            return null;
        }
        return output;
    }

    /** Returns true if the device is a tablet */
    public boolean isTablet() {
        // Get screen density & screen size from window manager
        WindowManager wm = (WindowManager) mContext.getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        // Determines the smallest screen width DP which is
        // calculated as ( pixels * density-independent pixel unit ) / density.
        // http://developer.android.com/guide/practices/screens_support.html.
        int screenDensity = metrics.densityDpi;
        int screenWidth = Math.min(
                metrics.widthPixels, metrics.heightPixels);
        int screenHeight = Math.max(
                metrics.widthPixels, metrics.heightPixels);
        int smallestScreenWidthDp = (Math.min(screenWidth, screenHeight)
                * DisplayMetrics.DENSITY_DEFAULT) / screenDensity;
        return smallestScreenWidthDp >= TABLET_DP_THRESHOLD;
    }

    public boolean isNexusExperienceDevice() {
        String result = mDevice.getProductName();
        if (result.indexOf(MARLIN) >= 0 || result.indexOf(SAILFISH) >= 0) {
            return true;
        }
        return false;
    }

    public void launchIntent(String intentName) throws InterruptedException {
        mDevice.pressHome();
        Intent intent = new Intent(intentName);
        launchIntent(intent);
    }

    public void launchIntent(Intent intent) throws InterruptedException {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        Thread.sleep(LONG_TIMEOUT * 2);
    }

    public void launchPackage(String pkgName) throws InterruptedException {
        Intent pkgIntent = mContext.getPackageManager()
                .getLaunchIntentForPackage(pkgName);
        launchIntent(pkgIntent);
    }

    public void launchApp(String packageName, String appName) {
        ILauncherStrategy mLauncherStrategy = LauncherStrategyFactory.getInstance(mDevice)
                .getLauncherStrategy();
        if (!mDevice.hasObject(By.pkg(packageName).depth(0))) {
            mLauncherStrategy.launch(appName, packageName);
        }
    }

    public void removeDir(String dir) {
        String cmd = " rm -rf " + dir;
        executeShellCommand(cmd);
    }

    public String getStringSetting(SettingType type, String sName) {
        switch (type) {
            case SYSTEM:
                return Settings.System.getString(mResolver, sName);
            case GLOBAL:
                return Settings.Global.getString(mResolver, sName);
            case SECURE:
                return Settings.Secure.getString(mResolver, sName);
        }
        return null;
    }

    public void launchQuickSettingsAndWait()throws Exception {
        mDevice.openQuickSettings();
        Thread.sleep(SHORT_TIMEOUT);
    }

    /**
     * Clears apps in overview/recents
     * @throws InterruptedException
     * @throws RemoteException
     */
    public void clearRecents() throws InterruptedException, RemoteException {
        // Launch recents if it's not already
        if (!mDevice.wait(Until.hasObject(By.res(SYSTEMUI_PACKAGE, "recents_view")),
                LONG_TIMEOUT)) {
            mDevice.pressRecentApps();
        }
        // Return if there is no apps in recents
        if (mDevice.wait(Until.hasObject(By.text("No recent items")),
                LONG_TIMEOUT)) {
            return;
        }
        // Get recents items
        int recents = mDevice
                .wait(Until.findObjects(By.res(SYSTEMUI_PACKAGE, "task_view_thumbnail")),
                        LONG_TIMEOUT)
                .size();
        // Clear recents
        for (int i = 0; i < recents; ++i) {
            mDevice.pressKeyCode(KeyEvent.KEYCODE_APP_SWITCH);
            mDevice.pressKeyCode(KeyEvent.KEYCODE_DEL);
            Thread.sleep(SHORT_TIMEOUT);
        }
    }

    /**
     * Multiuser helper methods
     */
    /**
     * Creates a test user
     * @return id for created secondary user
     */
    public int createSecondaryUser(String userName) {
        // Create user
        List<String> cmdOut = executeShellCommand("pm create-user " + userName);
        Assert.assertTrue("Output should have 1 line",
                cmdOut.size() == 1 && cmdOut.get(0).startsWith("Success"));
        // Find user id from user-create output
        // output format : "Success: created user id 10"
        Pattern pattern = Pattern.compile(
                "(.*)(:)(.*?)(\\d+)");
        Matcher matcher = pattern.matcher(cmdOut.get(0));
        if (matcher.find()) {
            Log.i(TEST_TAG, String.format("User Name:%s UserId:%d",
                    matcher.group(1), Integer.parseInt(matcher.group(4))));
            return Integer.parseInt(matcher.group(4));
        }
        return -1;
    }

    /**
     * Returns id for first secondary user
     * @return userid
     */
    public int getSecondaryUserId() {
        List<String> cmdOut = executeShellCommand("pm list users");
        Pattern pattern = Pattern.compile(
                "(.*\\{)(\\d+)(:)(.*?)(:)(\\d+)(\\}.*)"); // 2 = id 6 = flag
        Matcher matcher = pattern.matcher(cmdOut.get(2));
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(2));
        }
        return -1;
    }

    public void removeSecondaryUser(int userId) {
        int prevUserCount = getUserCount();
        List<String> cmdOut = executeShellCommand("pm remove-user " + userId);
        Assert.assertTrue("User hasn't been removed", getUserCount() == (prevUserCount - 1));
    }

    public int getUserCount() {
        return getUserManager().getUserCount();
    }

    /**
     * Multiwindow helper methods
     */
    /**
     * Returns a taskId for a given package and activity
     * @param pkgName
     * @param activityName
     * @return taskId
     */
    public int getTaskIdForActivity(String pkgName, String activityName) {
        int taskId = -1;
        // Find task id for given package and activity
        List<String> cmdOut = executeShellCommand("am stack list");
        for (String line : cmdOut) {
            Pattern pattern = Pattern.compile(String.format(".*taskId=([0-9]+): %s/%s.*",
                    pkgName, activityName));
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                taskId = Integer.parseInt(matcher.group(1));
                break;
            }
        }
        Assert.assertTrue("Taskid hasn't been found", taskId != -1);
        return taskId;
    }

    /**
     * Helper to change window mode between fullscreen and splitscreen for a given task
     * @param taskId
     * @param mode
     * @throws InterruptedException
     */
    public void changeWindowMode(int taskId, int mode) throws InterruptedException {
        mUiAutomation.executeShellCommand(
                String.format("am stack movetask %d %d true", taskId, mode));
        Thread.sleep(SHORT_TIMEOUT * 2);
    }

    /**
     * Backup restore helper methods
     */
    /*
     * Enables backup manager
     */
    public void enableBmgr(boolean enable) {
        List<String> output = executeShellCommand("bmgr enable " + Boolean.toString(enable));
        if (enable) {
            Assert.assertTrue("Bmgr not enabled",
                    output.get(0).equalsIgnoreCase("Backup Manager now enabled"));
        } else {
            Assert.assertTrue("Bmgr not disabled",
                    output.get(0).equalsIgnoreCase("Backup Manager now disabled"));
        }
    }

    /**
     * Checks whether a google account has been enabled in device for backup
     * @return true/false
     * @throws InterruptedException
     */
    public boolean hasDeviceBackupAccount() throws InterruptedException {
        launchIntent(android.provider.Settings.ACTION_PRIVACY_SETTINGS);
        UiObject2 deviceBackup = mDevice.wait(Until.findObject(By.text("Device backup")),
                LONG_TIMEOUT);
        String backupAcct = deviceBackup.getParent().getChildren().get(1).getText();
        if (backupAcct.equals(getRegisteredGoogleAccountOnDevice())) {
            return true;
        } else if (backupAcct.startsWith("No Account")) {
            return false;
        }
        return false;
    }

    /**
     * Get registered accounts ensures there is at least one account registered returns the google
     * account name
     * @return The registered gogole/gmail account on device
     */
    public String getRegisteredGoogleAccountOnDevice() {
        Account[] accounts = AccountManager.get(mContext).getAccounts();
        Assert.assertTrue("Device doesn't have any account registered", accounts.length >= 1);
        for (int i = 0; i < accounts.length; ++i) {
            if (accounts[i].type.equals("com.google")) {
                return accounts[i].name;
            }
        }
        throw new RuntimeException("The device is not registered with a google account");
    }

    /**
     * Settings helper methods for ABVT
     */
    public void setSettingStringValue(SettingType settingType, String settingName, String val)
            throws InterruptedException {
        switch (settingType) {
            case SYSTEM:
                Settings.System.putString(mContext.getContentResolver(), settingName, val);
            case GLOBAL:
                Settings.Global.putString(mContext.getContentResolver(), settingName, val);
            case SECURE:
                Settings.Secure.putString(mContext.getContentResolver(), settingName, val);
        }
        Thread.sleep(LONG_TIMEOUT);
    }

    /**
     * There are 3 setting types in Android as below
     */
    public static enum SettingType {
        SYSTEM, SECURE, GLOBAL
    }

    /**
     * Wifi helper methods
     */
    /**
     * Checks if wifi connection is active by sending an HTTP request, check for HTTP_OK
     */
    public boolean isWifiConnected() throws InterruptedException {
        int counter = 10;
        while (--counter > 0) {
            try {
                String mPingSite = String.format("http://%s", DEFAULT_PING_SITE);
                URL url = new URL(mPingSite);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(LONG_TIMEOUT * 12);
                conn.setReadTimeout(LONG_TIMEOUT * 12);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return true;
                }
                Thread.sleep(SHORT_TIMEOUT);
            } catch (IOException ex) {
                // Wifi being flaky in the lab, test retries 10 times to connect to google.com
                // as IOException is throws connection isn't made and response stream is null
                // so for retrying purpose, exception hasn't been rethrown
                Log.i(TEST_TAG, ex.getMessage());
            }
        }
        return false;
    }

    /**
     * Disconnects and disables network
     */
    public void disconnectWifi() {
        Assert.assertTrue("Wifi not disconnected", getWifiManager().disconnect());
        getWifiManager().disableNetwork(getWifiManager().getConnectionInfo().getNetworkId());
        getWifiManager().saveConfiguration();
    }

    /**
     * Ensures wifi is enabled in device
     * @throws InterruptedException
     */
    public void ensureWifiEnabled() throws InterruptedException {
        // Device already connected to wifi as part of tradefed setup
        if (!getWifiManager().isWifiEnabled()) {
            getWifiManager().enableNetwork(getWifiManager().getConnectionInfo().getNetworkId(),
                    true);
            int counter = 5;
            while (--counter > 0 && !getWifiManager().isWifiEnabled()) {
                Thread.sleep(LONG_TIMEOUT);
            }
        }
        Assert.assertTrue("Wifi should be enabled by now", getWifiManager().isWifiEnabled());
    }
}
