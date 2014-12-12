package com.energyict.protocols.exception;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;

/**
 * Serves as an exception indicator for any authentication related errors
 *
 * Copyrights EnergyICT
 * Date: 12/3/14
 * Time: 3:00 PM
 */
public class ProtocolAuthenticationException extends ComServerRuntimeException {

    public ProtocolAuthenticationException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

    public ProtocolAuthenticationException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }
}
