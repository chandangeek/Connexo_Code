package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Provides functionality to create proper exceptions
 * related to an AT modem serial connection.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 15:04
 */
public class ModemException extends CommunicationException {

    public ModemException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}