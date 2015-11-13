package com.energyict.protocol.exceptions.identifier;

import com.energyict.protocol.exceptions.ProtocolExceptionReference;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

/**
 * Provides 'Exception' functionality which can be thrown when the object could not be found or
 * could not be uniquely identified.
 *
 * @author sva
 * @since 12/10/2015 - 12:15
 */
public abstract class IdentifierResolvingException extends ProtocolRuntimeException {

    public IdentifierResolvingException(ProtocolExceptionReference exceptionReference, Object... messageArguments) {
        super(exceptionReference, messageArguments);
    }

    public IdentifierResolvingException(Throwable cause, ProtocolExceptionReference exceptionReference, Object... messageArguments) {
        super(cause, exceptionReference, messageArguments);
    }
}
