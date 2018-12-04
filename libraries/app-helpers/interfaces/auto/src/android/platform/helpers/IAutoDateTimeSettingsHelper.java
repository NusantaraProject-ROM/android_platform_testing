package android.platform.helpers;

/**
 * Helper class for functional tests of date & time
 */
import android.support.test.uiautomator.UiObject2;
import java.time.LocalDate;

public interface IAutoDateTimeSettingsHelper extends IAppHelper {
    /**
     * Setup expectation: Date & time setting is open
     *
     * This method is to set the device date manually
     *
     * @param date - input LocalDate object
     *
     */
    void setDate(LocalDate date);

    /**
     * Setup expectation: Date & time setting is open
     *
     * This method is to get the current date displayed on the UI in LocalDate object
     *
     */
    LocalDate getDate();

    /**
     * Setup expectation: Date & time setting is open
     *
     * This method is to set the time manually
     *
     * @param hour - input hour
     * @param minute - input minute
     * @param AM_PM - input am/pm
     *
     */
    void setTime(int hour, int minute, boolean is_am);

    /**
     * Setup expectation: Date & time setting is open
     *
     * This method is to get the current time displayed on the UI
     * The return string format will match the UI format exactly
     *
     */
    String getTime();

    /**
     * Setup expectation: Date & time setting is open
     *
     * This method is to set the timezone manually
     *
     * @param timezone - city selected for timezone
     *
     */
    void setTimeZone(String timezone);

    /**
     * Setup expectation: Date & time setting is open
     *
     * This method is to get the current time displayed on the UI
     *
     */
    String getTimeZone();

    /**
     * Setup expectation: Date & time setting is open
     *
     * This method is to get the 24 hour format menu switch widget object
     *
     */
    UiObject2 getUseTwentyFourHourFormatSwitchWidget();

    /**
     * Setup expectation: Date & time setting is open
     *
     * This method is to toggle on/off 24 hour format
     *
     *
     */
    boolean toggleTwentyFourHourFormatSwitch();
}
