package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReference;

/**
 * Specifies the possible error references for all exceptions that
 * can occur in the ComServer model module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (17:39)
 */
public enum ComServerModelExceptionReferences implements ExceptionReference<ComServerModelReferenceScope> {

    UNRECOGNIZED_DISCRIMINATOR(100, 2),
    SQL_ERROR(101, 0),
    NO_RESOURCES_ACQUIRED(102, 0),
    /**
     * Indicates that multiple {@link com.energyict.mdc.engine.impl.commands.store.DeviceCommand}s were found
     * that want to be executed first.
     */
    ONLY_ONE_FIRST_DEVICE_COMMAND(103, 2),
    /**
     * Indicates that multiple {@link com.energyict.mdc.engine.impl.commands.store.DeviceCommand}s were found
     * that want to be executed last.
     */
    ONLY_ONE_LAST_DEVICE_COMMAND(104, 2),

    /**
     * Indication of a scenario where the serial port with the given name does not exist
     */
    SERIAL_PORT_DOES_NOT_EXIST(200, 1),
    /**
     * Indication of a scenario where the serial port with the given name is used by another process
     */
    SERIAL_PORT_IS_IN_USE(201, 2),
    /**
     * Indication of a scenario where an exception occurred in the underlying serial library
     */
    SERIAL_PORT_LIBRARY_EXCEPTION(202, 0),
    /**
     * Indication of a scenario where an serial port configuration does not match the expected value
     */
    SERIAL_PORT_CONFIGURATION_MISMATCH(203, 2),
    /**
     * Indication of a scenario where we could not close/hangup the modem
     */
    MODEM_COULD_NOT_HANG_UP(204, 1),
    /**
     * Indication of a scenario where the read timeout exceeded
     */
    MODEM_READ_TIMEOUT(205, 3),
    /**
     * Indication of a scenario where the default profile could not be restored
     */
    MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE(206, 3),
    /**
     * Indication of a scenario where we could not write the given init string
     */
    MODEM_COULD_NOT_SEND_INIT_STRING(207, 3),
    /**
     * Indication of a case where the modem returned 'BUSY'
     */
    AT_MODEM_BUSY(208, 2),
    /**
     * Indication of a case where the modem returned 'ERROR'
     */
    AT_MODEM_ERROR(209, 2),
    /**
     * Indication of a case where the modem returned 'NO_ANSWER'
     */
    AT_MODEM_NO_ANSWER(210, 2),
    /**
     * Indication of a case where the modem returned 'NO_CARRIER'
     */
    AT_MODEM_NO_CARRIER(211, 2),
    /**
     * Indication of a case where the modem returned 'NO_DIALTONE'
     */
    AT_MODEM_NO_DIALTONE(212, 2),
    /**
     * Indication of a case where the connect of the modem failed
     */
    MODEM_CONNECT_TIMEOUT(213, 2),

    /**
     * Indication of a case where the modem could not establish a connection with its receiver.
     */
    MODEM_COULD_NOT_ESTABLISH_CONNECTION(214,2),
    /**
     * Indication of a scenario where we could not write the given init string
     */
    MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE(215, 2),
    /**
     * Indication of a case where the modem returned 'CALL ABORTED'
     */
    MODEM_CALL_ABORTED(216, 2);

    public long toNumerical() {
        return code;
    }

    @Override
    public int expectedNumberOfArguments() {
        return this.expectedNumberOfArguments;
    }


    ComServerModelExceptionReferences(long code, int expectedNumberOfArguments) {
        this.code = code;
        this.expectedNumberOfArguments = expectedNumberOfArguments;
    }

    private long code;
    private int expectedNumberOfArguments;

}