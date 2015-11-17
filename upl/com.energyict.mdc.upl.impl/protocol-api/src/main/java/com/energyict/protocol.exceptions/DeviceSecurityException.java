package com.energyict.protocol.exceptions;

/**
 * Should be used in any Security-related issue during a communication session with a Device
 *
 * @author gna
 * @since 28/03/12 - 16:40
 */
public class DeviceSecurityException extends CommunicationException {

    protected DeviceSecurityException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected DeviceSecurityException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private DeviceSecurityException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }
}