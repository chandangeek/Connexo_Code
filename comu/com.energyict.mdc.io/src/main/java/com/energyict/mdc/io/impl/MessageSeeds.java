package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.SerialComponentService;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-31 (14:14)
 */
public enum MessageSeeds implements MessageSeed {

    UNEXPECTED_IO_EXCEPTION(1, "unexpectedIOException", "Exception occurred while communication with a device"),
    SERIAL_PORT_LIBRARY_EXCEPTION(2, "underlyingSerialLibraryException", "Exception occurred in underlying serial library"),
    SERIAL_PORT_CONFIGURATION_MISMATCH(3, "serialPortConfigurationMismatch", "Value {1} is not expected for serial port configuration parameter {0}"),
    SERIAL_PORT_DOES_NOT_EXIST(4, "serialPortDoesNotExist", "The ComPort with name {0} does not exist"),
    SERIAL_PORT_IS_IN_USE(5, "serialPortInUse", "The ComPort with name {0} is used by another process {1}"),
    ASYNCHRONOUS_COMMUNICATION_IS_NOT_SUPPORTED(6, "asynchronousCommunicationIsNotSupported", "Asynchronous communication is not supported"),
    MODEM_CONNECT_TIMEOUT(7, "modemConnectTimeout", "Could not connect with modem on COM port {0}, no response within timeout [{1} ms]"),
    MODEM_COULD_NOT_HANG_UP(8, "modemCouldNotHangup", "Could not hangup/close COM port with name {0}"),
    MODEM_READ_TIMEOUT(9, "modemReadTimeout", "Modem on COM port {0} did not answer to [{2}] within configured timeout ({1} ms)"),
    MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE(10, "modemRestoreDefaultProfileError", "Could not restore the default modem profile settings on COM port with name {0}, request [{1}], response [{2}]"),
    MODEM_COULD_NOT_SEND_INIT_STRING(11, "modemSendInitStringError", "Failed to write init string [{1}] to modem on COM port {0}, meter response [{2}]"),
    MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE(12, "modemInitCommandStateError", "Failed to initialize the command state for modem on COM port {0}, meter response [{1}]"),
    AT_MODEM_BUSY(13, "atModemBusy", "Receiver was currently busy, modem on COM port {0} returned BUSY command, last command send [{1}]"),
    AT_MODEM_ERROR(14, "atModemError", "Most likely an invalid command has been sent, modem on COM port {0} returned ERROR command, last command send [{1}]"),
    AT_MODEM_NO_ANSWER(15, "atModemNoAnswer", "Receiver was not reachable, modem on COM port {0} returned NO_ANSWER command, last command send [{1}]"),
    AT_MODEM_NO_CARRIER(16, "atModemNoCarrier", "Receiver was not reachable, modem on COM port {0} returned NO_CARRIER command, last command send [{1}]"),
    AT_MODEM_NO_DIALTONE(17, "atModemNoDialtone", "Could not dial with modem on COM port {0}, a NO_DIALTONE command was returned, last command send [{1}]"),
    MODEM_CALL_ABORTED(18, "modemCallAborted", "Most likely an invalid command has been sent, modem on COM port {0} returned CALL ABORTED command, last command send [{1}]"),
    UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION(19, "unexpectedInboundCommException", "Unexpected inbound communication exception, see stacktrace for more details"),
    COMMUNICATION_INTERRUPTED(20, "communicationInterrupted", "Communication was interrupted: {0}"),
    CONNECTION_TIMEOUT(21, "connectionTimeout", "Connection timeout"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getModule() {
        return SerialComponentService.COMPONENT_NAME;
    }

}