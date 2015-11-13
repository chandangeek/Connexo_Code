package com.energyict.protocol.exceptions.identifier;

import com.energyict.protocol.exceptions.ProtocolExceptionReference;

/**
 * Provides 'Exception' functionality which can be thrown when Duplicate objects are
 * found when a unique result was expected.
 *
 * Copyrights EnergyICT
 * Date: 9/25/13
 * Time: 10:06 AM
 */
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

    public static DuplicateException duplicateFoundFor(Class duplicateClassType, String identifier){
        return new DuplicateException(ProtocolExceptionReference.DUPLICATE_FOUND, duplicateClassType.getSimpleName(), identifier);
    }
}
