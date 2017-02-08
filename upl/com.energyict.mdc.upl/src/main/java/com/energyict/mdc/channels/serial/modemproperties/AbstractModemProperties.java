package com.energyict.mdc.channels.serial.modemproperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * @author sva
 * @since 30/04/13 - 13:59
 */
public abstract class AbstractModemProperties {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "phone_number";     // the PhoneNumber of the device
    public static final String DELAY_BEFORE_SEND = "modem_senddelay";         // delay to wait before we send a command
    public static final String DELAY_AFTER_CONNECT = "modem_delay_after_connect"; // timeout to wait after a connect command has been received
    public static final String COMMAND_TIMEOUT = "modem_command_timeout";  // timeout for regular AT commands
    public static final String CONNECT_TIMEOUT = "modem_connect_timeout";  // timeout for the AT connect command
    public static final String COMMAND_TRIES = "modem_command_tries";      // the number of attempts a command should be send to the modem before
    public static final String MODEM_GLOBAL_INIT_STRINGS = "modem_global_init_string";   // the initialization strings for this modem type (separated by a colon
    public static final String MODEM_INIT_STRINGS = "modem_init_string";   // the initialization strings for this modem type (separated by a colon
    public static final String MODEM_DIAL_PREFIX = "modem_dial_prefix";    // the prefix at command which goes between the "ATD" and the actual phoneNumber
    public static final String MODEM_ADDRESS_SELECTOR = "modem_address_select";     // the address selector to use after a physical connect
    public static final String MODEM_POST_DIAL_COMMANDS = "modem_postdial_command";     // the set of post dial commandos to launch after a physical connect
    public static final String DTR_TOGGLE_DELAY = "disconnect_line_toggle_delay";// the delay between DTR line toggles, which are used to disconnect the active connection.

    protected static final Duration DEFAULT_DTR_TOGGLE_DELAY = Duration.ofSeconds(2);
    protected static final String DEFAULT_MODEM_DIAL_PREFIX = "";
    protected static final Duration DEFAULT_DELAY_BEFORE_SEND = Duration.ofMillis(500);
    protected static final Duration DEFAULT_DELAY_AFTER_CONNECT = Duration.ofMillis(500);

    /**
     * Getter for the the Network User Address of the device
     *
     * @return the PhoneNumber of the device
     */
    public abstract String getPhoneNumber();

    /**
     * Getter for the prefix command to use when performing the actual dial to the modem of the device
     *
     * @return the prefix command to be used when performing the actual dial to the modem of the device
     */
    public abstract String getCommandPrefix();

    /**
     * Getter for the timeout applicable for the connect command
     *
     * @return the timeout for the connect command
     */
    public abstract TemporalAmount getConnectTimeout();

    /**
     * Getter for the delay to wait after a connect command has been received
     *
     * @return the delay to wait after a connect command has been received
     */
    public abstract TemporalAmount getDelayAfterConnect();

    /**
     * Getter for the delay to wait before sending out the next command
     *
     * @return the delay to wait before sending out the next command
     */
    public abstract TemporalAmount getDelayBeforeSend();

    /**
     * Getter for the timeout applicable for regular commands
     *
     * @return the timeout for regular commands
     */
    public abstract TemporalAmount getCommandTimeOut();

    /**
     * Getter for the number of attempts a command should be send to the modem
     *
     * @return the number of attempts a command should be send to the modem
     */
    public abstract BigDecimal getCommandTry();

    /**
     * Getter for the global initialization strings for this modem type
     * <br></br>
     * <b>Note: </b> These global initialization strings will be handled before handling of
     * the regular initialization strings (which are defined in {@link #getModemInitStrings()})
     *
     * @return the global initialization strings for this modem type
     */
    public abstract List<String> getGlobalModemInitStrings();

    /**
     * Getter for the initialization strings for this modem type
     * <br></br>
     * <b>Note: </b> These regular initialization strings will be handled after handling of
     * the global initialization strings (which are defined in {@link #getGlobalModemInitStrings()})
     *
     * @return the initialization strings for this modem type
     */
    public abstract List<String> getModemInitStrings();

    /**
     * Getter for the delay between DTR line toggles, which are used to disconnect the active connection.
     *
     * @return the delay between DTR line toggles
     */
    public abstract TemporalAmount getLineToggleDelay();

}
