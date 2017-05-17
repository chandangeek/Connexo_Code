/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception.identifier;

import com.energyict.protocol.exception.ProtocolExceptionReference;

public final class DuplicateException extends IdentifierResolvingException {

    protected DuplicateException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected DuplicateException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private DuplicateException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    public static DuplicateException duplicateFoundFor(Class duplicateClassType, String identifier) {
        return new DuplicateException(ProtocolExceptionReference.DUPLICATE_FOUND, duplicateClassType.getSimpleName(), identifier);
    }
}
