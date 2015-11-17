package com.energyict.protocol.exceptions.identifier;

import com.energyict.protocol.exceptions.ProtocolExceptionReference;

/**
 * Provides 'Exception' functionality which can be thrown when the object could not be found
 * when a unique result was expected.
 *
 * Copyrights EnergyICT
 * Date: 9/25/13
 * Time: 10:06 AM
 */
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

    public static NotFoundException notFound(Class classType, String identifier){
        return new NotFoundException(ProtocolExceptionReference.NOT_FOUND, classType.getSimpleName(), identifier);
    }
}
