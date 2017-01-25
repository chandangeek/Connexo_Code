package com.energyict.protocols.exception;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;

/**
 * Serves as an exception indicator for any encryption related errors
 *
 * Copyrights EnergyICT
 * Date: 12/3/14
 * Time: 3:05 PM
 */
public class ProtocolEncryptionException extends ComServerRuntimeException{

    public ProtocolEncryptionException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

    public ProtocolEncryptionException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }
}
