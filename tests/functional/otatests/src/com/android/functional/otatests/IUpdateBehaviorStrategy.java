package com.android.functional.otatests;

import android.platform.test.helpers.SystemUpdateHelperImpl;

/**
 * An interface defining the actions required to perform specific update related tasks
 * based on the urgency of the pushed update.
 */
public interface IUpdateBehaviorStrategy {

    /**
     * Returns whether or not any actions need to occur after the notification action before
     * the device will attempt to restart and install.
     * @return true if there are any actions that need to be taken after a notification interaction
     */
    public boolean hasPostNotificationActions();

    /**
     * Return whether or not this behavior will create a notification.
     * @return True if a notification will be created, false otherwise.
     */
    public boolean createsNotification();

    /**
     * Perform actions relating to the OTA notification. In general this will result in clicking
     * on the notification.
     */
    public void interactWithNotification();

    /**
     * Perform System Update app interactions. In general this will result in clicking on
     * the app's main button one or several times. It may perform other functions as well.
     */
    public void interactWithApp();

    /**
     * Return whether or not interactWithApp should install an update at the time.
     * @return true if the update should be installed now, false otherwise
     */
    public boolean shouldInstall();
    /**
     *
     */
    public void setShouldInstall(boolean should);

    public void setHelper(SystemUpdateHelperImpl helper);

    public SystemUpdateHelperImpl getHelper();
}

