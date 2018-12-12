package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;

public class HsmException extends ProtocolRuntimeException {

    public HsmException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public HsmException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

    public HsmException(Throwable cause) {
        super(cause);
    }

}
