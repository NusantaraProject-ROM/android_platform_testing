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

package android.test.functional.tv.settings;

import static junit.framework.Assert.fail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;
import android.platform.test.helpers.exceptions.UiTimeoutException;
import android.support.test.uiautomator.Until;
import android.test.functional.tv.common.SysUiTestBase;
import android.util.Log;
import android.util.Patterns;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Functional verification tests for managing accounts on TV.
 *
 * adb shell am instrument -w -r \
 * -e account <accountName1,accountName2,...> -e password <password1,password2,...> \
 * -e class android.test.functional.tv.settings.AccountTests \
 * android.test.functional.tv.sysui/android.support.test.runner.AndroidJUnitRunner
 */
public class AccountTests extends SysUiTestBase {
    private static final String TAG = AccountTests.class.getSimpleName();
    private static final String ARGUMENT_ACCOUNT = "account";
    private static final String ARGUMENT_PASSWORD = "password";
    private static final char SEPARATOR = ',';  // used to separate multiple accounts and passwords
    private static final String GOOGLE_ACCOUNT = "com.google";
    private static final String DEFAULT_EMAIL_DOMAIN = "@gmail.com";
    private static final String TITLE_ADD_ACCOUNT = "Add account";
    private static final String TITLE_REMOVE_ACCOUNT = "Remove account";

    private static final long SHORT_SLEEP_MS = 3000;    // 3 seconds
    private static final long LONG_SLEEP_MS = 30000;    // 30 seconds

    private List<String> mAccountNames = new ArrayList<>();
    private List<String> mPasswords = new ArrayList<>();
    private AccountManager mAm = null;


    public AccountTests() {
        mAm = AccountManager.get(mContext);
        parseArguments();
    }

    @Before
    public void setUp() {
        // Remove all accounts
        removeAccounts();
        mLauncherStrategy.open();
    }

    @After
    public void tearDown() {
        mSettingsHelper.exit();
    }

    /**
     * Objective: Able to sign in with an account when no account on TV, and remove the account
     */
    @Test
    public void testSignInOneAccountAndRemove() {
        // Log in with the first account info
        String accountName = getEmailFromAccountName(mAccountNames.get(0));
        String password = mPasswords.get(0);
        openSettingsAndLogin(accountName, password);

        // Verify that the login is successful and the account name appears in summary
        Assert.assertTrue(mSettingsHelper.hasSettingBySummary(accountName));
        Assert.assertEquals(getAccounts().size(), 1);

        // Remove the account in Settings
        Assert.assertTrue(
                mSettingsHelper.clickSettingBySummary(accountName));
        Assert.assertTrue(mSettingsHelper.clickSetting(TITLE_REMOVE_ACCOUNT));
        Assert.assertNotNull(mSettingsHelper.selectGuidedAction("OK"));
        mDPadHelper.pressDPadCenterAndWait(Until.newWindow(), SHORT_SLEEP_MS);

        // Verify that the account is removed
        Assert.assertEquals(getAccounts().size(), 0);
    }

    /**
     * Objective: Verify that user cannot log in the same account
     */
    @Test
    public void testDisallowSignInSameAccount() {
        // Log in with the first account info
        String accountName = mAccountNames.get(0);
        String password = mPasswords.get(0);
        openSettingsAndLogin(accountName, password);
        if (getAccounts().size() != 1) {
            throw new IllegalStateException("The first login was not successful.");
        }

        // Log in with the account already registered should fail
        mSettingsHelper.clickSetting(TITLE_ADD_ACCOUNT);
        mNoTouchAuthHelper.waitForOpen(SHORT_SLEEP_MS);
        if (mNoTouchAuthHelper.loginAccount(accountName, password)) {
            fail("The login with the account already registered should be disallowed.");
        }

        // Verify that the attempt to login with the same account is rejected
        Assert.assertEquals(getAccounts().size(), 1);
    }

    /**
     * Objective: Able to sign in with multiple accounts when no account on TV
     */
    @Test
    public void testSignInWithMultiAccounts() {
        int accountsCount = mAccountNames.size();
        if (accountsCount < 2) {
            throw new IllegalArgumentException("More than one account required");
        }

        // Log in with multiple accounts
        for (int i = 0; i < accountsCount; ++i) {
            openSettingsAndLogin(mAccountNames.get(i), mPasswords.get(i));
        }

        // Verify that the login with multiple accounts is successful
        for (int i = accountsCount - 1; i >= 0; --i) {
            Assert.assertTrue(mSettingsHelper.hasSettingByTitleOrSummary(
                    getEmailFromAccountName(mAccountNames.get(i))));
        }
        Assert.assertEquals(getAccounts().size(), accountsCount);
    }

    /**
     * Objective: Verify that user can switch accounts in the YouTube app.
     */
    @Test
    public void testSwitchAccountsInYouTube() {
        // Clean data, and set up two accounts
        mCmdHelper.executeShellCommand(String.format("pm clear %s", mYouTubeHelper.getPackage()));
        int accountsCount = mAccountNames.size();
        if (accountsCount < 2) {
            throw new IllegalArgumentException("More than one account required");
        }
        for (int i = 0; i < accountsCount; ++i) {
            openSettingsAndLogin(getEmailFromAccountName(mAccountNames.get(i)), mPasswords.get(i));
        }
        if (getAccounts().size() != accountsCount) {
            throw new IllegalStateException(
                    String.format("This test requires to log in with more than one account. "
                            + "%d expected, %d found", accountsCount, getAccounts().size()));
        }

        // Verify that the login is successful in Settings
        Assert.assertEquals(getAccounts().size(), accountsCount);

        // Select the first account to log in YouTube
        mYouTubeHelper.open();
        // Note that the Sign-in page appears only when no account has been set up.
        // Once signed in, it would be no longer prompted even after the signout.
        // Clean app data on top of this test
        String firstAccount = getEmailFromAccountName(mAccountNames.get(0));
        Assert.assertTrue(mYouTubeHelper.signIn(firstAccount));
        mYouTubeHelper.waitForContentLoaded(SHORT_SLEEP_MS);

        // Verify that the account is set up in YouTube
        Assert.assertEquals(mYouTubeHelper.getSignInUserName(), firstAccount);

        // Sign out
        mYouTubeHelper.signOut();

        // Open the Setting, switch to the second account
        mYouTubeHelper.openSettings();
        mYouTubeHelper.openCardInRow("Sign in");
        String secondAccount = getEmailFromAccountName(mAccountNames.get(1));
        Assert.assertTrue(mYouTubeHelper.signIn(secondAccount));
        mYouTubeHelper.waitForContentLoaded(SHORT_SLEEP_MS);

        // Verify that the account is set up in YouTube
        Assert.assertEquals(mYouTubeHelper.getSignInUserName(), secondAccount);
    }

    @Ignore("Not yet implemented")
    @Test
    public void testSwitchAccountsInPlayStore() {

    }

    private void openSettingsAndLogin(String accountName, String password) {
        // Open the sign-in page
        mSettingsHelper.open();
        mSettingsHelper.clickSetting(TITLE_ADD_ACCOUNT);
        mNoTouchAuthHelper.waitForOpen(SHORT_SLEEP_MS);

        // Log in with an account
        mNoTouchAuthHelper.loginAccount(accountName, password);

        // Wait for it to return to the Settings
        if (!mSettingsHelper.waitForOpen(LONG_SLEEP_MS)) {
            throw new UiTimeoutException(
                    "Failed to return to the Settings after attempting to login");
        }
    }

    /**
     * Parse account names and passwords from arguments in following format:
     * -e account accountName1,accountName2,...
     * -e password password1,password2,...
     *
     * @return list of TestArg data, empty list if input is null
     */
    private void parseArguments() {
        mAccountNames.clear();
        mPasswords.clear();
        String accountNamesArg = mArguments.getString(ARGUMENT_ACCOUNT, "");
        for (String accountName : accountNamesArg.split(String.valueOf(SEPARATOR))) {
            // The account name needs to be unique
            if (!"".equals(accountName) && !mAccountNames.contains(accountName)) {
                mAccountNames.add(accountName);
            }
        }
        String passwordsArg = mArguments.getString(ARGUMENT_PASSWORD, "");
        for (String password : passwordsArg.split(String.valueOf(SEPARATOR))) {
            if (!"".equals(password)) {
                mPasswords.add(password);
            }
        }

        if (mAccountNames.size() == 0) {
            throw new IllegalArgumentException(
                    String.format("The argument '%s' required for test not found",
                            ARGUMENT_ACCOUNT));
        } else if (mPasswords.size() == 0) {
            throw new IllegalArgumentException(
                    String.format("The argument '%s' required for test not found",
                            ARGUMENT_PASSWORD));
        } else if (mAccountNames.size() != mPasswords.size()) {
            throw new IllegalArgumentException(String.format(
                    "The number of 'account' and 'password' arguments should be same. %d != %d",
                    mAccountNames.size(), mPasswords.size()));
        }
    }

    // The following helper functions to manage accounts requires to sign the test apk with
    // the platform keys and the system uid.
    private List<String> getAccounts() {
        List<String> accountNames = new ArrayList<>();
        Account[] accounts = mAm.getAccountsByType(GOOGLE_ACCOUNT);
        for (Account account : accounts) {
            Log.i(TAG, String.format("Found account %s", account.name));
            accountNames.add(account.name);
        }
        return accountNames;
    }

    private void removeAccounts() {
        Account[] accounts = mAm.getAccountsByType(GOOGLE_ACCOUNT);
        for (Account account : accounts) {
            Log.i(TAG, String.format("Removing account %s", account.name));
            RemoveCallback callback = new RemoveCallback();
            mAm.removeAccount(account, null, callback, null);
            if (callback.waitForRemoveCompletion() == null) {
                Log.e(TAG, String.format("Failed to remove account %s: Reason: %s",
                        account.name, callback.getErrorMessage()));
                return;
            }
        }
    }

    /**
     * @param accountName the account name passed in arguments. This may or may not be an email
     * @return the account name if the username is an email address or the account name appended
     *         with @gmail.com if not
     */
    private String getEmailFromAccountName(String accountName) {
        StringBuilder sb = new StringBuilder(accountName);
        if (!Patterns.EMAIL_ADDRESS.matcher(sb).matches()) {
            sb.append(DEFAULT_EMAIL_DOMAIN);
        }
        return sb.toString();
    }

    static class RemoveCallback implements AccountManagerCallback<Bundle> {
        // stores the result of account removal. null means not finished
        private Bundle mResult = null;
        private String mErrorMessage = null;

        public synchronized Bundle waitForRemoveCompletion() {
            while (mResult == null) {
                try {
                    wait(LONG_SLEEP_MS);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            return mResult;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            try {
                mResult = future.getResult();
            } catch (OperationCanceledException e) {
                handleException(e);
            } catch (IOException e) {
                handleException(e);
            } catch (AuthenticatorException e) {
                handleException(e);
            }
            synchronized (this) {
                notifyAll();
            }
        }

        public String getErrorMessage() {
            return mErrorMessage;
        }

        private void handleException(Exception e) {
            Log.e(TAG, "Failed to remove account", e);
            mResult = null;
            mErrorMessage = e.toString();
        }
    }
}

