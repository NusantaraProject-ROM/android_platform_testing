package android.platform.helpers;

/**
 * Helper class for functional tests of App Info settings
 */

public interface IAutoAppInfoSettingsHelper extends IAppHelper {
    /**
     * Setup expectation: App info setting is open
     *
     * This method is to open an application in App info setting
     *
     * @param application - name of the application
     *
     */
    void openAppMenu(String application);

    /**
     * Setup expectation: An application in App info setting is open
     *
     * This method is to enable/disable an application
     *
     * @param enable - true: to enable, false: to disable
     *
     */
    void enableDisableApplication(boolean enable);
}
