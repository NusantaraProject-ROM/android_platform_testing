package com.android.functional.otatests;

import android.os.SystemClock;
import android.platform.test.helpers.SystemUpdateHelperImpl;

public class RecommendedBehavior implements IUpdateBehaviorStrategy {

    private SystemUpdateHelperImpl mHelper;
    private boolean mShouldInstall;

    @Override
    public boolean hasPostNotificationActions() {
        return true;
    }

    @Override
    public boolean createsNotification() {
        return true;
    }

    @Override
    public void interactWithNotification() {
        // TODO(jestep): Auto-generated method stub
        mHelper.clickOtaNotification();
    }

    @Override
    public void interactWithApp() {
        mHelper.downloadUpdate();
        SystemClock.sleep(15000);
        if (shouldInstall()) {
            mHelper.installOta();
        }
    }

    @Override
    public void setHelper(SystemUpdateHelperImpl helper) {
        mHelper = helper;
    }

    @Override
    public SystemUpdateHelperImpl getHelper() {
        return mHelper;
    }

    @Override
    public void setShouldInstall(boolean shouldInstall) {
        mShouldInstall = shouldInstall;
    }

    @Override
    public boolean shouldInstall() {
        return mShouldInstall;
    }

}

