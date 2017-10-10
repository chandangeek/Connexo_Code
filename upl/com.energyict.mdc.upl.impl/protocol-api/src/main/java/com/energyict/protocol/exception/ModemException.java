/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception;

import com.energyict.mdc.upl.nls.MessageSeed;

/**
 * @author Stijn Vanhoorelbeke
 * @since 05.10.17 - 11:02
 */
public class ModemException extends com.energyict.protocol.exceptions.ModemException {

    public enum Type {
        MODEM_READ_TIMEOUT(ProtocolExceptionMessageSeeds.MODEM_READ_TIMEOUT),
        MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE(ProtocolExceptionMessageSeeds.MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE),
        MODEM_COULD_NOT_HANG_UP(ProtocolExceptionMessageSeeds.MODEM_COULD_NOT_HANG_UP),
        MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE(ProtocolExceptionMessageSeeds.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE),
        MODEM_COULD_NOT_SEND_INIT_STRING(ProtocolExceptionMessageSeeds.MODEM_COULD_NOT_SEND_INIT_STRING),
        MODEM_CONNECT_TIMEOUT(ProtocolExceptionMessageSeeds.MODEM_CONNECT_TIMEOUT),
        MODEM_COULD_NOT_ESTABLISH_CONNECTION(ProtocolExceptionMessageSeeds.MODEM_COULD_NOT_ESTABLISH_CONNECTION),
        MODEM_CALL_ABORTED(ProtocolExceptionMessageSeeds.MODEM_CALL_ABORTED),
        AT_MODEM_BUSY(ProtocolExceptionMessageSeeds.AT_MODEM_BUSY),
        AT_MODEM_ERROR(ProtocolExceptionMessageSeeds.AT_MODEM_ERROR),
        AT_MODEM_NO_ANSWER(ProtocolExceptionMessageSeeds.AT_MODEM_NO_ANSWER),
        AT_MODEM_NO_CARRIER(ProtocolExceptionMessageSeeds.AT_MODEM_NO_CARRIER),
        AT_MODEM_NO_DIALTONE(ProtocolExceptionMessageSeeds.AT_MODEM_NO_DIALTONE);

        private final MessageSeed messageSeed;

        Type(MessageSeed messageSeed) {
            this.messageSeed = messageSeed;
        }

        public MessageSeed getMessageSeed() {
            return messageSeed;
        }
    }

    public enum DialErrorType {
        MODEM_CALL_ABORTED,
        AT_MODEM_BUSY,
        AT_MODEM_ERROR,
        AT_MODEM_NO_ANSWER,
        AT_MODEM_NO_CARRIER,
        AT_MODEM_NO_DIALTONE;

        public Type getExceptionType() {
            return Type.valueOf(this.name());
        }
    }

    private final Type type;

    protected ModemException(Type type, Object... messageArguments) {
        super(type.getMessageSeed(), messageArguments);
        this.type = type;
    }

    protected ModemException(Type type, Throwable cause, Object... messageArguments) {
        super(cause, type.getMessageSeed(), messageArguments);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    /**
     * Creates an {@link ModemException} indicating that the collection system could
     * not <i>hang up</i> the modem, no new connection could be setup before old ones are closed.
     *
     * @param name the name of the port
     * @return the newly create exception
     */
    public static ModemException couldNotHangup(String name) {
        return new ModemException(Type.MODEM_COULD_NOT_HANG_UP, name);
    }

    /**
     * Creates an {@link ModemException} indicating that the read of a answer from the
     * modem took longer then configured.
     *
     * @param name the name of the ComPort where the modem is connected
     * @param milliSeconds the configured read timeout
     * @param command the command that causes the timeOut
     */
    public static ModemException commandTimeoutExceeded(String name, long milliSeconds, String command) {
        return new ModemException(Type.MODEM_READ_TIMEOUT, name, milliSeconds, command);
    }

    /**
     * Creates an {@link ModemException} indicating that the reset of the default
     * modem profile failed.
     *
     * @param name the name of the ComPort where the modem is connected
     * @return the newly created exception
     */
    public static ModemException couldNotRestoreProfile(String name, String lastCommandSend, String lastResponseReceived) {
        return new ModemException(Type.MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE, name, lastCommandSend, lastResponseReceived);
    }

    /**
     * Creates an {@link ModemException} indicating that the command state
     * could not be correctly initialized by the modem
     *
     * @param comPortName the comPort to which the modem is connected
     * @param lastResponseReceived the response from the modem
     * @return the newly create exception
     */
    public static ModemException failedToInitializeCommandStateString(String comPortName, String lastResponseReceived) {
        return new ModemException(Type.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE, comPortName, lastResponseReceived);
    }

    /**
     * Creates an {@link ModemException} indicating that the given InitString
     * could not be correctly interpreted by the modem
     *
     * @param comPortName the comPort to which the modem is connected
     * @param lastCommandSend the last command send (which should be the init string)
     * @param lastResponseReceived the response from the modem on the init string
     * @return the newly create exception
     */
    public static ModemException failedToWriteInitString(String comPortName, String lastCommandSend, String lastResponseReceived) {
        return new ModemException(Type.MODEM_COULD_NOT_SEND_INIT_STRING, comPortName, lastCommandSend, lastResponseReceived);
    }

    /**
     * Creates an {@link ModemException} indicating that on the modem for the given comPort, a dial error occurred.
     *
     * @param comPortName The comPort on which the error occurred
     * @param type The Type for this ModemException
     * @return the newly created exception
     */
    public static ModemException dialingError(String comPortName, DialErrorType type, String lastCommandSend) {
        return new ModemException(type.getExceptionType(), comPortName, lastCommandSend);
    }

    /**
     * Creates an {@link ModemException} indicating that the connection failed due to a timeout
     *
     * @param comPortName the comPort to which the modem is connected
     * @param milliSeconds the milliseconds we waited before failing
     * @return the newly created exception
     */
    public static ModemException connectTimeOutException(String comPortName, long milliSeconds) {
        return new ModemException(Type.MODEM_CONNECT_TIMEOUT, comPortName, milliSeconds);
    }

    /**
     * Creates an {@link ModemException} indicating that the modem could not establish a connection to its receiver, due to a timeout.
     *
     * @param comPortName the comPort to which the modem is connected
     * @param milliSeconds the milliseconds we waited before failing
     * @return the newly created exception
     */
    public static ModemException couldNotEstablishConnection(String comPortName, long milliSeconds) {
        return new ModemException(Type.MODEM_COULD_NOT_ESTABLISH_CONNECTION, comPortName, milliSeconds);
    }
}