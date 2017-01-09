package com.energyict.mdc.io;

/**
 * Models exceptional situation that occur while working with a {@link com.energyict.mdc.channels.serial.ServerSerialPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-09 (09:57)
 */
public class SerialPortException extends RuntimeException {

    private final Type type;
    private final Object[] messageArguments;

    public enum Type {
        SERIAL_PORT_LIBRARY_EXCEPTION,
        SERIAL_PORT_CONFIGURATION_MISMATCH,
        SERIAL_PORT_DOES_NOT_EXIST,
        SERIAL_PORT_IS_IN_USE;
    }

    /**
     * Wraps an exception which occurred in the underlying serial library.
     *
     * @param cause the underlying exception
     * @return the newly created exception
     */
    public static SerialPortException serialLibraryException(Throwable cause) {
        return new SerialPortException(Type.SERIAL_PORT_LIBRARY_EXCEPTION, cause);
    }

    /**
     * Creates a {@link SerialPortException} indicating that the value of some configuration property is not as expected
     *
     * @param propertyName   the name of the property
     * @param invalidValue the invalid value of the property
     * @return the newly crated exception
     */
    public static SerialPortException configurationMisMatch(String propertyName, String invalidValue) {
        return new SerialPortException(Type.SERIAL_PORT_CONFIGURATION_MISMATCH, propertyName, invalidValue);
    }

    /**
     * Creates a {@link SerialPortException} indicating that the SerialPort with the given name does not exist.
     *
     * @param name the name of the ComPort that does not exist
     * @return the newly create exception
     */
    public static SerialPortException serialPortDoesNotExist(String name) {
        return new SerialPortException(Type.SERIAL_PORT_DOES_NOT_EXIST, name);
    }

    /**
     * Creates a {@link SerialPortException} indicating that the SerialPort with the given name is used by another process.
     *
     * @param name the name of the ComPort that is used by another process
     * @param currentOwner the process that currently uses the port
     * @return the newly created exception
     */
    public static SerialPortException serialPortIsInUse(String name, String currentOwner) {
        return new SerialPortException(Type.SERIAL_PORT_IS_IN_USE, name, currentOwner);
    }

    public Type getType() {
        return type;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

    private SerialPortException(Type type, Object... messageArguments) {
        this.type = type;
        this.messageArguments = messageArguments;
    }

    private SerialPortException(Type type, Throwable t, Object... messageArguments) {
        super(t);
        this.type = type;
        this.messageArguments = messageArguments;
    }

}