package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Provides functionality to create proper exceptions related to a ServerSerialPort.
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/08/12
 * Time: 16:08
 */
public final class SerialPortException extends ComServerRuntimeException {

    private SerialPortException(ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    private SerialPortException(Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    /**
     * Creates a {@link SerialPortException} indicating that the SerialPort with the given name does not exist on the ComServer
     *
     * @param name the name of the ComPort that does not exist
     * @return the newly create exception
     */
    public static SerialPortException serialPortDoesNotExist(final String name) {
        return new SerialPortException(generateExceptionCodeByReference(ProtocolExceptionReferences.SERIAL_PORT_DOES_NOT_EXIST), name);
    }

    /**
     * Creates a {@link SerialPortException} indicating that the SerialPort with the given name is used by another process
     *
     * @param name         the name of the ComPort which is used by another process
     * @param currentOwner the process that currently uses the ComPort
     * @return the newly created exception
     */
    public static SerialPortException serialPortIsInUse(final String name, final String currentOwner) {
        return new SerialPortException(generateExceptionCodeByReference(ProtocolExceptionReferences.SERIAL_PORT_IS_IN_USE), name, currentOwner);
    }

    /**
     * Wraps an exception which occurred in the underlying serial library
     *
     * @param cause the underlying exception
     * @return the newly created exception
     */
    public static SerialPortException serialLibraryException(Throwable cause) {
        return new SerialPortException(cause, generateExceptionCodeByReference(ProtocolExceptionReferences.SERIAL_PORT_LIBRARY_EXCEPTION));
    }

    /**
     * Creates a {@link SerialPortException} indicating that the value of some configuration property is not as expected
     *
     * @param propertyName   the name of the property
     * @param invalidValue the invalid value of the property
     * @return the newly crated exception
     */
    public static SerialPortException configurationMisMatch(String propertyName, String invalidValue) {
        return new SerialPortException(generateExceptionCodeByReference(ProtocolExceptionReferences.SERIAL_PORT_CONFIGURATION_MISMATCH), propertyName, invalidValue);
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(ProtocolExceptionReferences reference) {
        return new ExceptionCode(new ProtocolsExceptionReferenceScope(), ExceptionType.COMMUNICATION, reference);
    }

}
