/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception.identifier;

import com.energyict.protocol.exception.ProtocolExceptionReference;

public final class NotFoundException extends IdentifierResolvingException {

    protected NotFoundException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected NotFoundException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private NotFoundException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    public static NotFoundException notFound(Class classType, String identifier) {
        return new NotFoundException(ProtocolExceptionReference.NOT_FOUND, classType.getSimpleName(), identifier);
    }
}
