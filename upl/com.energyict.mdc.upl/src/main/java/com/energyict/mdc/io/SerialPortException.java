package com.energyict.mdc.io;

/**
 * Models exceptional situation that occur while working with a {@link com.energyict.mdc.channels.serial.ServerSerialPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-09 (09:57)
 */
public class SerialPortException extends RuntimeException {

    private final Type type;

    public enum Type {
        SERIAL_PORT_LIBRARY_EXCEPTION;
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

    public Type getType() {
        return type;
    }

    private SerialPortException(Type type) {
        this.type = type;
    }

    private SerialPortException(Type type, Throwable t) {
        super(t);
        this.type = type;
    }

}