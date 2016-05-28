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

package com.android.functional.applinktests;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.view.accessibility.AccessibilityWindowInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AppLinkTests extends InstrumentationTestCase {
    public final String TEST_TAG = "AppLinkFunctionalTest";
    public final String TEST_PKG_NAME = "com.android.applinktestapp";
    public final String TEST_APP_NAME = "AppLinkTestApp";
    public final String YOUTUBE_PKG_NAME = "com.google.android.youtube";
    public final String HTTP_SCHEME = "http";
    public final String TEST_HOST = "test.com";
    public final int TIMEOUT = 1000;
    private UiDevice mDevice = null;
    private Context mContext = null;
    private UiAutomation mUiAutomation = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        mContext = getInstrumentation().getContext();
        mUiAutomation = getInstrumentation().getUiAutomation();
        mDevice.setOrientationNatural();
    }

    // Ensures that default app link setting set to 'undefined' for 3P apps
    public void testDefaultAppLinkSettting() {
        String out = executeShellCommand("pm get-app-link " + TEST_PKG_NAME);
        assertTrue("Default app link not set to 'undefined' mode", "undefined".equals(out));
        openLink(HTTP_SCHEME, TEST_HOST);
        ensureDisambigPresent();
    }

    // User sets an app to open for a link 'Always' and disambig never shows up
    public void testUserSetToAlways() throws InterruptedException {
        openLink(HTTP_SCHEME, TEST_HOST);
        ensureDisambigPresent();
        mDevice.wait(Until.findObject(By.text("AppLinkTestApp1")), TIMEOUT).click();
        mDevice.wait(Until.findObject(By.res("android:id/button_always")), TIMEOUT).click();
        Thread.sleep(TIMEOUT);
        openLink(HTTP_SCHEME, TEST_HOST);
        List<AccessibilityWindowInfo> windows = mUiAutomation.getWindows();
        boolean success = windows.get(windows.size() - 1).getRoot().getPackageName()
                .equals(TEST_PKG_NAME);
        assertTrue(String.format("%s is not top activity", TEST_APP_NAME), success);
    }

    // User sets an app to open for a link 'Just Once' and disambig shows up next time too
    public void testUserSetToJustOnce() throws InterruptedException {
        openLink(HTTP_SCHEME, TEST_HOST);
        ensureDisambigPresent();
        mDevice.wait(Until.findObject(By.text("AppLinkTestApp1")), TIMEOUT).click();
        mDevice.wait(Until.findObject(By.res("android:id/button_once")), TIMEOUT).click();
        Thread.sleep(TIMEOUT);
        List<AccessibilityWindowInfo> windows = mUiAutomation.getWindows();
        assertTrue(String.format("%s is not top activity", TEST_APP_NAME),
                windows.get(windows.size() - 1).getRoot().getPackageName()
                        .equals(TEST_PKG_NAME));
        openLink(HTTP_SCHEME, TEST_HOST);
        assertTrue("Target app isn't the default choice",
                mDevice.wait(Until.hasObject(By.text("Open with AppLinkTestApp1")), TIMEOUT));
        mDevice.wait(Until.findObject(By.res("android:id/button_once")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        Thread.sleep(TIMEOUT);
        windows = mUiAutomation.getWindows();
        assertTrue(String.format("%s is not top activity", TEST_APP_NAME),
                windows.get(windows.size() - 1).getRoot().getPackageName()
                        .equals(TEST_PKG_NAME));
        mDevice.pressHome();
        // Ensure it doesn't change on second attempt
        openLink(HTTP_SCHEME, TEST_HOST);
        // Ensure disambig is present
        mDevice.wait(Until.findObject(By.res("android:id/button_always")), TIMEOUT).click();
        mDevice.pressHome();
        // User chose to set to always and intent is opened in target direct
        openLink(HTTP_SCHEME, TEST_HOST);
        Thread.sleep(TIMEOUT);
        windows = mUiAutomation.getWindows();
        assertTrue(String.format("%s is not top activity", TEST_APP_NAME),
                windows.get(windows.size() - 1).getRoot().getPackageName()
                        .equals(TEST_PKG_NAME));
    }

    // Ensure verified app always open even candidate but unverified app set to 'always'
    public void testVerifiedAppOpenWhenNotVerifiedSetToAlways() throws InterruptedException {
        executeShellCommand("pm set-app-link " + TEST_PKG_NAME + " always");
        executeShellCommand("pm set-app-link " + YOUTUBE_PKG_NAME + " always");
        Thread.sleep(TIMEOUT);
        openLink(HTTP_SCHEME, "youtube.com");
        Thread.sleep(TIMEOUT);
        List<AccessibilityWindowInfo> windows = mUiAutomation.getWindows();
        assertTrue(String.format("%s is not top activity", "youtube"),
                windows.get(windows.size() - 1).getRoot().getPackageName()
                        .equals("com.google.android.youtube"));
    }

    // Ensure verified app always open even one candidate but unverified app set to 'ask'
    public void testVerifiedAppOpenWhenUnverifiedSetToAsk() throws InterruptedException {
        executeShellCommand("pm set-app-link " + TEST_PKG_NAME + " ask");
        executeShellCommand("pm set-app-link " + YOUTUBE_PKG_NAME + " always");
        String out = executeShellCommand("pm get-app-link " + YOUTUBE_PKG_NAME);
        openLink(HTTP_SCHEME, "youtube.com");
        Thread.sleep(TIMEOUT);
        List<AccessibilityWindowInfo> windows = mUiAutomation.getWindows();
        assertTrue(String.format("%s is not top activity", "youtube"),
                windows.get(windows.size() - 1).getRoot().getPackageName()
                        .equals("com.google.android.youtube"));
    }

    // Ensure disambig is shown if verified app set to 'never' and unverified app set to 'ask'
    public void testUserChangeVerifiedLinkHandler() throws InterruptedException {
        executeShellCommand("pm set-app-link " + TEST_PKG_NAME + " ask");
        executeShellCommand("pm set-app-link " + YOUTUBE_PKG_NAME + " never");
        Thread.sleep(TIMEOUT);
        openLink(HTTP_SCHEME, "youtube.com");
        ensureDisambigPresent();
        executeShellCommand("pm set-app-link " + YOUTUBE_PKG_NAME + " always");
        Thread.sleep(TIMEOUT);
        openLink(HTTP_SCHEME, "youtube.com");
        Thread.sleep(TIMEOUT * 2);
        List<AccessibilityWindowInfo> windows = mUiAutomation.getWindows();
        assertTrue(String.format("%s is not top activity", "youtube"),
                windows.get(windows.size() - 1).getRoot().getPackageName()
                        .equals(YOUTUBE_PKG_NAME));
    }

    // Ensure unverified app always open when unverified app set to always but verified app set to
    // never
    public void testCandidateSetToAlwaysVerifiedSetToNever() throws InterruptedException {
        executeShellCommand("pm set-app-link " + TEST_PKG_NAME + " always");
        executeShellCommand("pm set-app-link " + YOUTUBE_PKG_NAME + " never");
        Thread.sleep(TIMEOUT);
        openLink(HTTP_SCHEME, "youtube.com");
        Thread.sleep(TIMEOUT);
        List<AccessibilityWindowInfo> windows = mUiAutomation.getWindows();
        assertTrue(String.format("%s is not top activity", TEST_APP_NAME),
                windows.get(windows.size() - 1).getRoot().getPackageName()
                        .equals(TEST_PKG_NAME));
    }

    // Test user can modify 'App Link Settings'
    public void testSettingsChangeUI1() throws InterruptedException {
        Intent intent_as = new Intent(
                android.provider.Settings.ACTION_APPLICATION_SETTINGS);
        mContext.startActivity(intent_as);
        Thread.sleep(TIMEOUT);
        mDevice.wait(Until.findObject(By.res("com.android.settings:id/advanced")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        mDevice.wait(Until.findObject(By.text("Opening links")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        mDevice.wait(Until.findObject(By.text("AppLinkTestApp1")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        mDevice.wait(Until.findObject(By.text("Open supported links")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        mDevice.wait(Until.findObject(By.text("Open in this app")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        String out = executeShellCommand("pm get-app-link " + TEST_PKG_NAME);
        Thread.sleep(TIMEOUT);
        assertTrue(String.format("Default app link not set to 'always ask' rather set to %s", out),
                "always".equals(out));
        mDevice.wait(Until.findObject(By.text("Open supported links")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        mDevice.wait(Until.findObject(By.text("Don’t open in this app")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        out = executeShellCommand("pm get-app-link " + TEST_PKG_NAME);
        Thread.sleep(TIMEOUT);
        assertTrue(String.format("Default app link not set to 'never' rather set to %s", out),
                "never".equals(out));
        mDevice.wait(Until.findObject(By.text("Open supported links")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        mDevice.wait(Until.findObject(By.text("Ask every time")), TIMEOUT)
                .clickAndWait(Until.newWindow(), TIMEOUT);
        out = executeShellCommand("pm get-app-link " + TEST_PKG_NAME);
        Thread.sleep(TIMEOUT);
        assertTrue(String.format("Default app link not set to 'always ask' rather set to %s", out),
                "always ask".equals(out));
    }

    // Ensure system apps that claim to open always for set to always
    public void ztestSysappAppLinkSettings() {
        // List of system app that are set to 'Always' for certain urls
        List<String> alwaysOpenApps = new ArrayList<String>();
        alwaysOpenApps.add("com.google.android.apps.docs.editors.docs"); // Docs
        alwaysOpenApps.add("com.google.android.apps.docs.editors.sheets"); // Sheets
        alwaysOpenApps.add("com.google.android.apps.docs.editors.slides"); // Slides
        alwaysOpenApps.add("com.google.android.apps.docs"); // Drive
        alwaysOpenApps.add("com.google.android.youtube"); // YouTube
        for (String s : alwaysOpenApps) {
            String out = executeShellCommand(String.format("pm get-app-link %s", s));
            assertTrue(String.format("App link for %s should be set to 'Always'", s),
                    "always".equalsIgnoreCase(out));
        }

    }

    @Override
    protected void tearDown() throws Exception {
        executeShellCommand("pm clear com.android.applinktestapp");
        executeShellCommand("pm set-app-link " + TEST_PKG_NAME + " undefined");
        executeShellCommand("pm set-app-link " + YOUTUBE_PKG_NAME + " always");
        Thread.sleep(TIMEOUT);
        mDevice.unfreezeRotation();
        super.tearDown();
    }

    private void openLink(String scheme, String host) {
        String out = executeShellCommand(String.format(
                "am start -a android.intent.action.VIEW -d %s://%s/", scheme, host));
    }

    private void ensureDisambigPresent() {
        assertNotNull("Disambig dialog is not shown",
                mDevice.wait(Until.hasObject(By.res("android:id/resolver_list")),
                        TIMEOUT));
        List<UiObject2> resolverApps = mDevice.wait(Until.findObjects(By.res("android:id/text1")),
                TIMEOUT);
        assertTrue("There aren't exactly 2 apps to resolve", resolverApps.size() == 2);
        assertTrue("Resolver apps aren't correct",
                "AppLinkTestApp1".equals(resolverApps.get(0).getText()) &&
                        "Chrome".equals(resolverApps.get(1).getText()));
    }

    private String executeShellCommand(String command) {
        ParcelFileDescriptor pfd = mUiAutomation.executeShellCommand(command);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(pfd.getFileDescriptor())))) {
            String str = reader.readLine();
            Log.d(TEST_TAG, String.format("Executing command: %s", command));
            return str;
        } catch (IOException e) {
            Log.e(TEST_TAG, e.getMessage());
        }

        return null;
    }
}
