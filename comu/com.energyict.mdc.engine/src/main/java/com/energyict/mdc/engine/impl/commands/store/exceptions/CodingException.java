package com.energyict.mdc.engine.impl.commands.store.exceptions;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;
import com.energyict.mdc.exceptions.ComServerModelExceptionReferences;
import com.energyict.mdc.exceptions.ComServerModelReferenceScope;

/**
 * Models the exceptional situations that occur when a developer has
 * forgotten or neglected to comply with coding standards
 * or constraints imposed by model components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (12:29)
 */
public final class CodingException extends ComServerRuntimeException {

    /**
     * Constructs a new CodingException to represent that a second {@link DeviceCommand}
     * was found that needs to be executed {@link com.energyict.comserver.commands.ExecutionTimePreference#FIRST}.
     *
     * @param first The existing DeviceCommand that wants to execute first
     * @param second The second DeviceCommand that wants to execute first
     * @return The CodingException
     */
    public static CodingException onlyOneFirstDeviceCommand (DeviceCommand first, DeviceCommand second) {
        return new CodingException(
                onlyOneDeviceCommandCode(ComServerModelExceptionReferences.ONLY_ONE_FIRST_DEVICE_COMMAND),
                first,
                second);
    }

    /**
     * Constructs a new CodingException to represent that a second {@link DeviceCommand}
     * was found that needs to be executed {@link com.energyict.comserver.commands.ExecutionTimePreference#LAST}.
     *
     * @param last The existing DeviceCommand that wants to execute last
     * @param second The second DeviceCommand that wants to execute last
     * @return The CodingException
     */
    public static CodingException onlyOneLastDeviceCommand (DeviceCommand last, DeviceCommand second) {
        return new CodingException(
                onlyOneDeviceCommandCode(ComServerModelExceptionReferences.ONLY_ONE_LAST_DEVICE_COMMAND),
                last,
                second);
    }

    private static ExceptionCode onlyOneDeviceCommandCode (ComServerModelExceptionReferences exceptionReference) {
        return new ExceptionCode(new ComServerModelReferenceScope(), ExceptionType.CODING, exceptionReference);
    }

    private CodingException (ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    private CodingException (Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

}