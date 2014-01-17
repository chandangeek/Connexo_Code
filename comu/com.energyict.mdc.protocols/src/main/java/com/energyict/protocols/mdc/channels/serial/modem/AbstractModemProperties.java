package com.energyict.protocols.mdc.channels.serial.modem;

import com.energyict.mdc.common.TimeDuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author sva
 * @since 30/04/13 - 13:59
 */
public abstract class AbstractModemProperties {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "phone_number";     // the PhoneNumber of the device

    /**
     * Getter for the the Network User Address of the device
     *
     * @return the PhoneNumber of the device
     */
    protected abstract String getPhoneNumber();

    /**
     * Getter for the prefix command to use when performing the actual dial to the modem of the device
     *
     * @return the prefix command to be used when performing the actual dial to the modem of the device
     */
    protected abstract String getCommandPrefix();

    /**
     * Getter for the timeout applicable for the connect command
     *
     * @return the timeout for the connect command
     */
    protected abstract TimeDuration getConnectTimeout();

    /**
     * Getter for the delay to wait after a connect command has been received
     *
     * @return the delay to wait after a connect command has been received
     */
    protected abstract TimeDuration getDelayAfterConnect();

    /**
     * Getter for the delay to wait before sending out the next command
     *
     * @return the delay to wait before sending out the next command
     */
    protected abstract TimeDuration getDelayBeforeSend();

    /**
     * Getter for the timeout applicable for regular commands
     *
     * @return the timeout for regular commands
     */
    protected abstract TimeDuration getCommandTimeOut();

    /**
     * Getter for the number of attempts a command should be send to the modem
     *
     * @return the number of attempts a command should be send to the modem
     */
    protected abstract BigDecimal getCommandTry();

    /**
     * Getter for the initialization strings for this modem type
     *
     * @return the initialization strings for this modem type
     */
    protected abstract List<String> getModemInitStrings();

    /**
     * Getter for the delay between DTR line toggles, which are used to disconnect the active connection.
     *
     * @return the delay between DTR line toggles
     */
    protected abstract TimeDuration getLineToggleDelay();

}
