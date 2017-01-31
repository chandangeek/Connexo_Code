/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author sva
 * @since 30/04/13 - 13:59
 */
@ProviderType
public interface ModemProperties {

    String PHONE_NUMBER_PROPERTY_NAME = "phone_number";     // the PhoneNumber of the device

    /**
     * Gets the Network User Address of the device.
     *
     * @return the PhoneNumber of the device
     */
    String getPhoneNumber();

    /**
     * Gets the prefix command to use when performing the actual dial to the modem of the device.
     *
     * @return the prefix command to be used when performing the actual dial to the modem of the device
     */
    String getCommandPrefix();

    /**
     * Gets the timeout applicable for the connect command.
     *
     * @return the timeout for the connect command
     */
    TimeDuration getConnectTimeout();

    /**
     * Gets the delay to wait after a connect command has been received.
     *
     * @return the delay to wait after a connect command has been received
     */
    TimeDuration getDelayAfterConnect();

    /**
     * Gets the delay to wait before sending out the next command.
     *
     * @return the delay to wait before sending out the next command
     */
    TimeDuration getDelayBeforeSend();

    /**
     * Gets the timeout applicable for regular commands.
     *
     * @return the timeout for regular commands
     */
    TimeDuration getCommandTimeOut();

    /**
     * Gets the number of attempts a command should be send to the modem.
     *
     * @return the number of attempts a command should be send to the modem
     */
    BigDecimal getCommandTry();

    /**
     * Gets the global initialization strings for this modem type.
     * <br></br>
     * <b>Note: </b> These global initialization strings will be handled before handling of
     * the regular initialization strings (which are defined in {@link #getModemInitStrings()})
     *
     * @return the global initialization strings for this modem type
     */
    List<String> getGlobalModemInitStrings();

    /**
     * Gets the initialization strings for this modem type.
     * <br></br>
     * <b>Note: </b> These regular initialization strings will be handled after handling of
     * the global initialization strings (which are defined in {@link #getGlobalModemInitStrings()})
     *
     * @return the initialization strings for this modem type
     */
    List<String> getModemInitStrings();


    /**
     * Gets the delay between DTR line toggles, which are used to disconnect the active connection.
     *
     * @return the delay between DTR line toggles
     */
    TimeDuration getLineToggleDelay();

}