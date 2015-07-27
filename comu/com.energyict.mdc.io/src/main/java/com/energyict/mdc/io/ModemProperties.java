package com.energyict.mdc.io;

import com.elster.jupiter.time.TimeDuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author sva
 * @since 30/04/13 - 13:59
 */
public interface ModemProperties {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "phone_number";     // the PhoneNumber of the device

    /**
     * Getter for the the Network User Address of the device
     *
     * @return the PhoneNumber of the device
     */
    public String getPhoneNumber();

    /**
     * Getter for the prefix command to use when performing the actual dial to the modem of the device
     *
     * @return the prefix command to be used when performing the actual dial to the modem of the device
     */
    public String getCommandPrefix();

    /**
     * Getter for the timeout applicable for the connect command
     *
     * @return the timeout for the connect command
     */
    public TimeDuration getConnectTimeout();

    /**
     * Getter for the delay to wait after a connect command has been received
     *
     * @return the delay to wait after a connect command has been received
     */
    public TimeDuration getDelayAfterConnect();

    /**
     * Getter for the delay to wait before sending out the next command
     *
     * @return the delay to wait before sending out the next command
     */
    public TimeDuration getDelayBeforeSend();

    /**
     * Getter for the timeout applicable for regular commands
     *
     * @return the timeout for regular commands
     */
    public TimeDuration getCommandTimeOut();

    /**
     * Getter for the number of attempts a command should be send to the modem
     *
     * @return the number of attempts a command should be send to the modem
     */
    public BigDecimal getCommandTry();

    /**
     * Getter for the global initialization strings for this modem type
     * <br></br>
     * <b>Note: </b> These global initialization strings will be handled before handling of
     * the regular initialization strings (which are defined in {@link #getModemInitStrings()})
     *
     * @return the global initialization strings for this modem type
     */
    public List<String> getGlobalModemInitStrings();

    /**
     * Getter for the initialization strings for this modem type
     * <br></br>
     * <b>Note: </b> These regular initialization strings will be handled after handling of
     * the global initialization strings (which are defined in {@link #getGlobalModemInitStrings()})
     *
     * @return the initialization strings for this modem type
     */
    public List<String> getModemInitStrings();


    /**
     * Getter for the delay between DTR line toggles, which are used to disconnect the active connection.
     *
     * @return the delay between DTR line toggles
     */
    public TimeDuration getLineToggleDelay();

}
