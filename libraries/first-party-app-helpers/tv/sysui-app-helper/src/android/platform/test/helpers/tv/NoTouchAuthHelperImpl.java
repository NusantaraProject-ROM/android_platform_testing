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

package android.platform.test.helpers.tv;

import android.app.Instrumentation;
import android.content.Context;
import android.platform.test.helpers.AbstractLeanbackAppHelper;
import android.platform.test.helpers.DPadHelper;
import android.platform.test.helpers.exceptions.UnknownUiException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;


/**
 * App helper implementation class for the NoTouchAuthDelegate UI
 * to add an account to non-touch device like TV.
 */
public class NoTouchAuthHelperImpl extends AbstractLeanbackAppHelper {
    private static final String LOG_TAG = NoTouchAuthHelperImpl.class.getSimpleName();
    private static final String UI_PACKAGE = "com.google.android.gsf.notouch";

    private static final String TITLE_SIGN_IN_ONBOARDING = "Sign in to your account";
    private static final String TITLE_SIGN_IN_ACCOUNT = "Enter your account email address";
    private static final String TITLE_SIGN_IN_PASSWORD = "Enter your account password";
    private static final String TITLE_SIGN_IN_ACCOUNT_ALREADY_EXISTS =
            "This account already exists on your device";
    private static final String TEXT_SIGN_IN_SECOND_SCREEN = "Use your phone or laptop";
    private static final String TEXT_SIGN_IN_PASSWORD = "Use your password";

    private static final long SHORT_SLEEP_MS = 3000;


    private Context mContext;


    public NoTouchAuthHelperImpl(Instrumentation instrumentation) {
        super(instrumentation);
        mDPadHelper = DPadHelper.getInstance(instrumentation);
        mContext = instrumentation.getContext();
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
        throw new UnsupportedOperationException("This method is not supported for NoTouchAuth");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        throw new UnsupportedOperationException("This method is not supported for NoTouchAuth");
    }

    /**
     * Setup expectations: The sign-in page is open.
     * <p>
     * Attempts to login with an account.
     * </p>
     * @return true if the attempt to login is successful. However this doesn't guarantee that
     * the account is registered in AccountManager.
     */
    public boolean loginAccount(String accountName, String password) {
        selectUseYourPassword();

        // Enter the account name
        if (!isSignInAccountPage()) {
            throw new UnknownUiException("Failed to find the page to enter account");
        }
        setTextForSignIn(accountName);

        // Check if the account already exists
        if (isSignInAccountAlreadyExists()) {
            Log.w(LOG_TAG, "Failed to log in with the account already registered.");
            return false;
        }

        // Enter the password
        if (!isSignInPasswordPage()) {
            throw new UnknownUiException("Failed to find the page to enter password");
        }
        setTextForSignIn(password);
        return true;
    }

    /**
     * Setup expectations: The sign-in page is open.
     * <p>
     * Selects "Use Your Password".
     * </p>
     * @return
     */
    private void selectUseYourPassword() {
        selectSignInOptions(TEXT_SIGN_IN_PASSWORD);
        // Wait for it to open the page to enter account name
        mDevice.waitForIdle();
        if (!isSignInAccountPage()) {
            throw new UnknownUiException("Failed to find the page to enter account name");
        }
    }

    /**
     * Setup expectations: The sign-in page is open. Selects "Use your phone or laptop" for
     * Second Screen Setup.
     * @return
     */
    private void selectUseYourPhoneOrLaptop() {
        selectSignInOptions(TEXT_SIGN_IN_SECOND_SCREEN);
    }

    private boolean isSignInOnboardingPage() {
        return TITLE_SIGN_IN_ONBOARDING.equals(getTitleText());
    }

    private boolean isSignInAccountPage() {
        return TITLE_SIGN_IN_ACCOUNT.equals(getTitleText());
    }

    private boolean isSignInPasswordPage() {
        return TITLE_SIGN_IN_PASSWORD.equals(getTitleText());
    }

    private boolean isSignInAccountAlreadyExists() {
        return TITLE_SIGN_IN_ACCOUNT_ALREADY_EXISTS.equals(getTitleText());
    }

    private void selectSignInOptions(String optionString) {
        if (!isSignInOnboardingPage()) {
            throw new IllegalStateException("Should be on the sign in onboarding page");
        }
        UiObject2 action = mDevice.wait(Until.findObject(By.res(UI_PACKAGE, "action")),
                SHORT_SLEEP_MS);
        if (action == null) {
            throw new UnknownUiException("The container 'action' for sign-in not found");
        }
        UiObject2 button = select(action,
                By.res(UI_PACKAGE, "list_item_text").text(optionString),
                Direction.DOWN);
        if (button == null) {
            throw new UnknownUiException("The button not found " + optionString);
        }
        mDPadHelper.pressDPadCenterAndWait(Until.newWindow(), SHORT_SLEEP_MS);
    }

    private String getTitleText() {
        return mDevice.findObject(By.res(UI_PACKAGE, "title_text")).getText();
    }

    private void setTextForSignIn(String text) {
        UiObject2 editText = mDevice.wait(Until.findObject(By.res(UI_PACKAGE, "text_input")),
                SHORT_SLEEP_MS);
        editText.setText(text);
        mDPadHelper.pressEnterAndWait(Until.newWindow(), SHORT_SLEEP_MS);
    }
}

