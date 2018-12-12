package com.energyict.protocol.exception;

import com.energyict.mdc.upl.nls.MessageSeed;

public class HsmException extends com.energyict.protocol.exceptions.HsmException {

    public HsmException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public HsmException(Exception cause) {
        super(cause, ProtocolExceptionMessageSeeds.COMMUNICATION_WITH_HSM, cause.getMessage());
    }

    public HsmException(String message) {
        super(ProtocolExceptionMessageSeeds.COMMUNICATION_WITH_HSM, message);
    }

}
