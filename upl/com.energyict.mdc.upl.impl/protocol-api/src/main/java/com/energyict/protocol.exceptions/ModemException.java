package com.energyict.protocol.exceptions;

/**
 * Provides functionality to create proper exceptions
 * related to an AT modem serial connection
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 15:04
 */
public final class ModemException extends CommunicationException {

    protected ModemException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected ModemException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private ModemException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    /**
     * Creates an {@link ModemException} indicating that the collection system could
     * not <i>hang up</i> the modem, no new connection could be setup before old ones are closed
     *
     * @param name the name of the ComPort that does not exist
     * @return the newly create exception
     */
    public static ModemException couldNotHangup(final String name) {
        return new ModemException(ProtocolExceptionReference.MODEM_COULD_NOT_HANG_UP, name);
    }

    /**
     * Creates an {@link ModemException} indicating that the read of a answer from the
     * modem took longer then configured.
     *
     * @param name         the name of the ComPort where the modem is connected
     * @param milliSeconds the configured read timeout
     * @param command      the command that causes the timeOut
     */
    public static ModemException commandTimeoutExceeded(String name, long milliSeconds, String command) {
        return new ModemException(ProtocolExceptionReference.MODEM_READ_TIMEOUT, name, milliSeconds, command);
    }

    /**
     * Creates an {@link ModemException} indicating that the reset of the default
     * modem profile failed.
     *
     * @param name the name of the ComPort where the modem is connected
     * @return the newly created exception
     */
    public static ModemException couldNotRestoreProfile(String name, String lastCommandSend, String lastResponseReceived) {
        return new ModemException(ProtocolExceptionReference.MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE, name, lastCommandSend, lastResponseReceived);
    }

    /**
     * Creates an {@link ModemException} indicating that the command state
     * could not be correctly initialized by the modem
     *
     * @param comPortName          the comPort to which the modem is connected
     * @param lastResponseReceived the response from the modem
     * @return the newly create exception
     */
    public static ModemException failedToInitializeCommandStateString(String comPortName, String lastResponseReceived) {
        return new ModemException(ProtocolExceptionReference.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE, comPortName, lastResponseReceived);
    }

    /**
     * Creates an {@link ModemException} indicating that the given InitString
     * could not be correctly interpreted by the modem
     *
     * @param comPortName          the comPort to which the modem is connected
     * @param lastCommandSend      the last command send (which should be the init string)
     * @param lastResponseReceived the response from the modem on the init string
     * @return the newly create exception
     */
    public static ModemException failedToWriteInitString(String comPortName, String lastCommandSend, String lastResponseReceived) {
        return new ModemException(ProtocolExceptionReference.MODEM_COULD_NOT_SEND_INIT_STRING, comPortName, lastCommandSend, lastResponseReceived);
    }

    /**
     * Creates an {@link ModemException} indicating that on the modem for the given comPort, a dial error occurred.
     * In general these references should be used:
     * <ul>
     * <li>{@link ProtocolExceptionReference#AT_MODEM_BUSY}</li>
     * <li>{@link ProtocolExceptionReference#AT_MODEM_ERROR}</li>
     * <li>{@link ProtocolExceptionReference#AT_MODEM_NO_ANSWER}</li>
     * <li>{@link ProtocolExceptionReference#AT_MODEM_NO_CARRIER}</li>
     * <li>{@link ProtocolExceptionReference#AT_MODEM_NO_DIALTONE}</li>
     * </ul>
     *
     * @param comPortName         the comPort on which the error occurred
     * @param exceptionReferences the exceptionReference for this atModemException
     * @return the newly created exception
     */
    public static ModemException dialingError(String comPortName, ProtocolExceptionReference exceptionReferences, String lastCommandSend) {
        return new ModemException(exceptionReferences, comPortName, lastCommandSend);
    }

    /**
     * Creates an {@link ModemException} indicating that the connection failed due to a timeout
     *
     * @param comPortName  the comPort to which the modem is connected
     * @param milliSeconds the milliseconds we waited before failing
     * @return the newly created exception
     */
    public static ModemException connectTimeOutException(String comPortName, long milliSeconds) {
        return new ModemException(ProtocolExceptionReference.MODEM_CONNECT_TIMEOUT, comPortName, milliSeconds);
    }

    /**
     * Creates an {@link ModemException} indicating that the modem could not establish a connection to its receiver, due to a timeout.
     *
     * @param comPortName  the comPort to which the modem is connected
     * @param milliSeconds the milliseconds we waited before failing
     * @return the newly created exception
     */
    public static ModemException couldNotEstablishConnection(String comPortName, long milliSeconds) {
        return new ModemException(ProtocolExceptionReference.MODEM_COULD_NOT_ESTABLISH_CONNECTION, comPortName, milliSeconds);
    }
}