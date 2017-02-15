/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.exception;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;

public class ProtocolEncryptionException extends ComServerRuntimeException{

    public ProtocolEncryptionException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

    public ProtocolEncryptionException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }
}
