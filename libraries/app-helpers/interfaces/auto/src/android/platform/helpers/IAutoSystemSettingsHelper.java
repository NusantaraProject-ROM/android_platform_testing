package android.platform.helpers;

/**
 * Helper class for functional tests of system settings
 */

import java.util.Date;

public interface IAutoSystemSettingsHelper extends IAppHelper {
    /**
     * Setup expectation: System setting is open
     *
     * This method is to set display language
     *
     * @param language - input language
     *
     */
    void setDisplayLanguage(String language);

    /**
     * Setup expectation: System setting is open
     *
     * This method is to get current display language
     *
     */
    String getCurrentLanguage();

    /**
     * Setup expectation: System setting is open
     *
     * This method is to get device model number from UI
     *
     */
    String getDeviceModel();

    /**
     * Setup expectation: System setting is open
     *
     * This method is to get android version from UI
     *
     */
    String getAndroidVersion();

    /**
     * Setup expectation: System setting is open
     *
     * This method is to get android security patch level from UI
     *
     */
    Date getAndroidSecurityPatchLevel();

    /**
     * Setup expectation: System setting is open
     *
     * This method is to get kernel version from UI
     *
     */
    String getKernelVersion();

    /**
     * Setup expectation: System setting is open
     *
     * This method is to get build number from UI
     *
     */
    String getBuildNumber();

    /**
     * Setup expectation: System setting is open
     *
     * This method is to reset network connection. [ Wifi & Bluetooth ]
     *
     */
    void resetNetwork();


    /**
     * Setup expectation: System setting is open
     *
     * This method is to reset application preferences
     *
     */
    void resetAppPreferences();
}
