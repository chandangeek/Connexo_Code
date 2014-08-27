package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Provides functionality to create proper exceptions related to a ServerSerialPort.
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/08/12
 * Time: 16:08
 */
public final class SerialPortException extends ComServerRuntimeException {

    public SerialPortException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}