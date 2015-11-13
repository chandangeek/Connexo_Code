package com.energyict.protocol.exceptions;

/**
 * @author sva
 * @since 9/10/2015 - 11:33
 */
public enum ProtocolExceptionReference {

    NUMERIC_PARAMETER_EXPECTED(100, 2, "The parameter {0} is expected to be numeric but got {1}"),
    UNEXPECTED_RESPONSE(101, 1, "Received an unexpected response from the meter: {0}"),
    UNSUPPORTED_URL_CONTENT_TYPE(102, 1, "Unsupported URL content type: {0}"),
    UNSUPPORTED_VERSION(103, 2, "Version {0} is not supported for {1}"),
    NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION(104, 1, "Device '{0}' is not configured for inbound communication"),
    UNSUPPORTED_DISCOVERY_RESULT_TYPE(105, 1, "Indication of an unsupported Discovery Result type: {0}"),
    NO_INBOUND_DATE(106, 1, "Device with id {0} did not send expected data"),
    PROTOCOL_DISCONNECT(107, 1, "The logical disconnect to a device failed: {0}"),
    CONNECTION_TIMEOUT(108, 1, "Connection timeout after {0} attempts"),
    UNEXPECTED_IO_EXCEPTION(109, 1, "Exception occurred while communicating with a device: {0}"),
    NUMBER_OF_RETRIES_REACHED(110, 2, "Maximum number of retries ({1}) reached: {0}"),
    UNEXPECTED_PROTOCOL_ERROR(111, 1, "Unexpected error during protocol execution: {0}"),
    PROTOCOL_CONNECT(112, 1, "The logical connect to a device failed: {0}"),
    CIPHERING_EXCEPTION(113, 1, "Encountered an exception related to the ciphering of data: {0}"),
    COMMUNICATION_INTERRUPTED(114, 1, "Communication was interrupted: {0}"),
    COMMUNICATION_ABORTED_BY_USER(115, 0, "Communication was aborted by the user"),
    DATA_ENCRYPTION_EXCEPTION(116, 0, "Failure to decrypt encrypted data received from device"),
    DATA_ENCRYPTION_EXCEPTION_WITH_CAUSE(117, 1, "Failure to decrypt encrypted data received from device: {0}"),
    INBOUND_TIMEOUT(118, 1, "A timeout occurred while trying to receive an inbound frame: {0}"),
    INBOUND_UNEXPECTED_FRAME(119, 2, "Received an unexpected inbound frame ({0}): {1}"),
    INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION(120, 1, "Referenced a non-existing index: {0}"),
    PROTOCOL_IO_PARSE_ERROR(121, 1, "Protocol parse error: {0}"),
    CONNECTION_SETUP_ERROR(122, 1, "Connection setup failed: {0}"),
    CONNECTION_DISCONNECT_ERROR(123, 1, "Disconnect of connection failed: {0}"),
    MODEM_COULD_NOT_HANG_UP(124, 1, "Could not hangup/close COM port with name {0}"),
    MODEM_READ_TIMEOUT(125, 3, "Modem on COM port {0} did not answer to [{2}] within configured timeout ({1} ms)"),
    MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE(126, 3, "Could not restore the default modem profile settings on COM port with name {0}, request [{1}], response [{2}]"),
    MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE(127, 2, "Failed to initialize the command state for modem on COM port {0}, meter response [{1}]"),
    MODEM_COULD_NOT_SEND_INIT_STRING(128, 3, "Failed to write init string [{1}] to modem on COM port {0}, meter response [{2}]"),
    MODEM_CONNECT_TIMEOUT(129, 2, "Could not connect with modem on COM port {0}, no response within timeout [{1} ms]"),
    MODEM_COULD_NOT_ESTABLISH_CONNECTION(130, 2, "Failed to establish a connection between modem on COM port {0} and its receiver within timeout [{1} ms]"),
    AT_MODEM_BUSY(131, 2, "Receiver was currently busy, modem on COM port {0} returned BUSY command, last command send [{1}]"),
    AT_MODEM_ERROR(132, 2, "Most likely an invalid command has been sent, modem on COM port {0} returned ERROR command, last command send [{1}]"),
    AT_MODEM_NO_ANSWER(133, 2, "Receiver was not reachable, modem on COM port {0} returned NO_ANSWER command, last command send [{1}]"),
    AT_MODEM_NO_CARRIER(134, 2, "Receiver was not reachable, modem on COM port {0} returned NO_CARRIER command, last command send [{1}]"),
    AT_MODEM_NO_DIALTONE(135, 2, "Could not dial with modem on COM port {0}, a NO_DIALTONE command was returned, last command send [{1}]"),
    GENERAL_PARSE_EXCEPTION(136, 1, "A general parsing error occurred: {0}"),
    SETUP_OF_INBOUND_CALL_FAILED(137, 1, "An IOException occurred during the setup of an inbound call: {0}"),
    CLOSE_OF_INBOUND_CONNECTOR_FAILED(138, 1, "An IOEXception occurred during the close of an inbound connector: {0}"),
    INVALID_PROPERTY_FORMAT(139, 3, "Property {0} (value: {1}) has an unexpected format: {2}"),
    MISSING_PROPERTY(140, 1, "Required property \"{0}\" is missing"),
    MISSING_PROPERTY_FOR_DEVICE(141, 2, "Required property \"{0}\" is missing for device with identifier '{1}'"),
    INVALID_PROPERTY_VALUE(142, 2, "Unexpected value '{1}' for property '{0}'"),
    UNEXPECTED_COM_CHANNEL(143, 2, "Expected a com channel of type '{0}' but received a com channel of type '{1}'"),
    NOT_ALLOWED_TO_EXECUTE_COMMAND(144, 2, "Not allowed to execute command '{0}' due to the following issue: {1}"),
    GENERIC_JAVA_REFLECTION_ERROR(145, 1, "Unable to create an instance of the class {0}"),
    UNSUPPORTED_METHOD(146, 2, "Method {1} is not supported for class {0}"),
    UNRECOGNIZED_ENUM_VALUE(147, 2, "Unrecognized value {0} for enum {1}"),
    DUPLICATE_FOUND(148, 2, "A duplicate '{0}' was found when a unique result was expected for '{1}'"),
    NOT_FOUND(149, 2, "Could not find an object of type '{0}' when a unique result was expected for '{1}'"),
    MODEM_CALL_ABORTED(150, 2, "Most likely an invalid command has been sent, modem on COM port {0} returned CALL ABORTED command, last command send [{1}]");

    private final int code;
    private final int expectedNumberOfArguments;
    private final String defaultFormat;

    ProtocolExceptionReference(int code, int expectedNumberOfArguments, String defaultFormat) {
        this.code = code;
        this.expectedNumberOfArguments = expectedNumberOfArguments;
        this.defaultFormat = defaultFormat;
    }

    public long toNumerical() {
        return code;
    }

    public int getExpectedNumberOfArguments() {
        return this.expectedNumberOfArguments;
    }

    public String getMessageFormat() {
        return defaultFormat;
    }


}