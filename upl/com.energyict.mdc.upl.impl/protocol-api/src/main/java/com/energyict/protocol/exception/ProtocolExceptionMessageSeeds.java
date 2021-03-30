/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception;

import com.energyict.mdc.upl.nls.MessageSeed;

import java.util.logging.Level;

/**
 * @author sva
 * @since 9/10/2015 - 11:33
 */
public enum ProtocolExceptionMessageSeeds implements MessageSeed {

    NUMERIC_PARAMETER_EXPECTED("numericParameterExpected", "The parameter {0} is expected to be numeric but got {1}"),
    UNEXPECTED_RESPONSE("unexpected.response", "Received an unexpected response from the meter: {0}"),
    UNEXPECTED_PROPERTY_VALUE("unexpected.propertyValue", "Unexpected value '{1}' for property '{0}', expected'{2}'"),
    UNSUPPORTED_URL_CONTENT_TYPE("unsupportedURLContentType", "Unsupported URL content type: {0}"),
    UNSUPPORTED_VERSION("unsupportedVersion", "Version {0} is not supported for {1}"),
    NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION("deviceNotConfiguredForInboundCommunication", "Device ''{0}'' is not configured for inbound communication"),
    UNSUPPORTED_DISCOVERY_RESULT_TYPE("unsupportedDiscoveryResultType", "Indication of an unsupported Discovery Result type: {0}"),
    NO_INBOUND_DATA("noInboundData", "Device with id {0} did not send expected data"),
    PROTOCOL_DISCONNECT_FAILED("protocolDisconnectFailed", "The logical disconnect to a device failed: {0}"),
    CONNECTION_TIMEOUT("connection.timeout", "Connection timeout after {0} attempts"),
    UNEXPECTED_IO_EXCEPTION("unexpectedIOException", "Exception occurred while communicating with a device: {0}"),
    NUMBER_OF_RETRIES_REACHED("number.of.retries.reached", "Maximum number of tries ({1}) reached: {0}"),
    UNEXPECTED_PROTOCOL_ERROR("unexpectedProtocolErrror", "Unexpected error during protocol execution: {0}"),
    PROTOCOL_CONNECT("protocolConnect.failed", "The logical connect to a device failed: {0}"),
    CIPHERING_EXCEPTION("cipheringException", "Encountered an exception related to the ciphering of data: {0}"),
    COMMUNICATION_INTERRUPTED("communicationInterrupted", "Communication was interrupted: {0}"),
    DATA_ENCRYPTION_EXCEPTION("dataEncryptionException", "Failure to decrypt encrypted data received from device"),
    DATA_ENCRYPTION_EXCEPTION_WITH_CAUSE("dataEncryptionExceptionWithCause", "Failure to decrypt encrypted data received from device: {0}"),
    INBOUND_TIMEOUT("inboundTimeout", "A timeout occurred while trying to receive an inbound frame: {0}"),
    INBOUND_UNEXPECTED_FRAME("unexpectedInboundFrame", "Received an unexpected inbound frame ({0}): {1}"),
    FRAME_COUNTER_CACHE_NOT_SUPPORTED("notSupportedFrameCounterCache", "Device configured to support frame counter cache but no frame cache implementation found"),
    FRAME_COUNTER_NOT_AVAILABLE("deviceFrameCounterNotAvailable", "Could not read device frame counter"),
    INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION("dataParseException.indexOutOfBounds", "Referenced a non-existing index: {0}"),
    PROTOCOL_IO_PARSE_ERROR("dataParseException.protocolIOError", "Protocol parse error: {0}"),
    CONNECTION_SETUP_ERROR("conectionSetupError", "Connection setup failed: {0}"),
    CONNECTION_DISCONNECT_ERROR("disconnectFailed", "Disconnect of connection failed: {0}"),
    MODEM_COULD_NOT_HANG_UP("modemexception.modem.hangup.failed", "Could not hangup/close COM port with name {0}"),
    MODEM_READ_TIMEOUT("modemexception.modem.read.timeout", "Modem on COM port {0} did not answer to [{2}] within configured timeout ({1} ms)"),
    MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE("modemexception.modem.restore.default.profile.failed", "Could not restore the default modem profile settings on COM port with name {0}, request [{1}], response [{2}]"),
    MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE("modemexception.modem.initialize.command.state.failure", "Failed to initialize the command state for modem on COM port {0}, meter response [{1}]"),
    MODEM_COULD_NOT_SEND_INIT_STRING("modemexception.modem.send.init.string.failed", "Failed to write init string [{1}] to modem on COM port {0}, meter response [{2}]"),
    MODEM_CONNECT_TIMEOUT("modemexception.modem.connect.timeout", "Could not connect with modem on COM port {0}, no response within timeout [{1} ms]"),
    MODEM_COULD_NOT_ESTABLISH_CONNECTION("modemexception.modem.connection.failed", "Failed to establish a connection between modem on COM port {0} and its receiver within timeout [{1} ms]"),
    AT_MODEM_BUSY("modemexception.modem.busy", "Receiver was currently busy, modem on COM port {0} returned BUSY command, last command send [{1}]"),
    AT_MODEM_ERROR("modemexception.modem.general.error", "Most likely an invalid command has been sent, modem on COM port {0} returned ERROR command, last command send [{1}]"),
    AT_MODEM_NO_ANSWER("modemexception.modem.no.answer", "Receiver was not reachable, modem on COM port {0} returned NO_ANSWER command, last command send [{1}]"),
    AT_MODEM_NO_CARRIER("modemexception.modem.no.carrier", "Receiver was not reachable, modem on COM port {0} returned NO_CARRIER command, last command send [{1}]"),
    AT_MODEM_NO_DIALTONE("modemexception.modem.no.dialtone", "Could not dial with modem on COM port {0}, a NO_DIALTONE command was returned, last command send [{1}]"),
    MODEM_CALL_ABORTED("modemexception.modem.call aborted", "Most likely an invalid command has been sent, modem on COM port {0} returned CALL ABORTED command, last command send [{1}]"),
    GENERAL_PARSE_EXCEPTION("generalParseException", "A general parsing error occurred: {0}"),
    SETUP_OF_INBOUND_CALL_FAILED("setupOfInboundCallFailed", "An IOException occurred during the setup of an inbound call: {0}"),
    CLOSE_OF_INBOUND_CONNECTOR_FAILED("closeOfInboundConnectorFailed", "An IOEXception occurred during the close of an inbound connector: {0}"),
    INVALID_PROPERTY_FORMAT("invalidPropertyFormat", "Property {0} (value: {1}) has an unexpected format: {2}"),
    MISSING_PROPERTY("missingProperty", "Required property \"{0}\" is missing"),
    MISSING_PROPERTY_FOR_DEVICE("missingPropertyForDevice", "Required property \"{0}\" is missing for device with identifier ''{1}''"),
    INVALID_PROPERTY_VALUE("invalidPropertyValue", "Unexpected value ''{1}'' for property ''{0}''"),
    UNEXPECTED_COM_CHANNEL("unexpectedComChannel", "Expected a com channel of type ''{0}'' but received a com channel of type ''{1}''"),
    NOT_ALLOWED_TO_EXECUTE_COMMAND("notAllowedToExecuteCommand", "Not allowed to execute command ''{0}'' due to the following issue: {1}"),
    GENERIC_JAVA_REFLECTION_ERROR("genericReflectionError", "Unable to create an instance of the class {0}"),
    UNSUPPORTED_METHOD("unsupportedMethod", "Method {1} is not supported for class {0}"),
    UNRECOGNIZED_ENUM_VALUE("unrecognizedEnumValue", "Unrecognized value {0} for enum {1}"),
    DUPLICATE_FOUND("duplicateFound", "A duplicate ''{0}'' was found when a unique result was expected for ''{1}''"),
    NOT_FOUND("notFound", "Could not find an object of type ''{0}'' when a unique result was expected for ''{1}''"),
    SERIAL_NUMBER_NOT_SUPPORTED("serialNumberNotSupported", "The legacy protocol does not support readout of the serial number"),
    PROTOCOL_IMPLEMENTATION_ERROR("protocolImplementationError", "Protocol implementation error: {0}"),
    SIGNATURE_VERIFICATION_ERROR("signatureVerificationError", "Verification of the received digital signature (using the server signing certificate) failed"),
    NUMBER_OF_RETRIES_REACHED_CONNECTION_STILL_INTACT("numberOfRetriesReached", "Maximum number of retries ({1}) reached: {0}. The connection is still intact though."),
    INVALID_PROPERTY_VALUE_WITH_REASON("invalidPropertyValueWithReason", "Unsupported value ''{1}'' for property ''{0}'': {2}"),
    INVALID_PROPERTY_VALUE_LENGTH_WITH_REASON("invalidPropertyValueLengthWIthReason", "Unsupported value length ''{1}'' for property ''{0}'': {2}"),
    COMMUNICATION_WITH_HSM("communicationWithHsmFailed", "An exception occurred during communication with the HSM: '{0}'"),
    UNEXPECTED_HSM_KEY_FORMAT("unexpectedHSMKeyFormat", "HSM key has an invalid format. Expected format is 'keyLabel:irreversibleKey'"),
    FAILED_TO_SETUP_HSM_KEY_MANAGER("failedToSetupKeyManager", "Failed to setup HSM Key Manager, TLS connection will not be setup."),
    EMPTY_MBUS_SET("emptyMBus", "MBus set shouldn't be empty. Please update topology"),
    NOT_FOUND_MBUS_SERIAL_NUMBER("notFoundMBusSerialNumber", "Not found device {1} in MBus set: {2}.");

    private final String key;
    private final String defaultTranslation;
    private final Level level;

    ProtocolExceptionMessageSeeds(String key, String defaultTranslation) {
        this(key, defaultTranslation, Level.SEVERE);
    }

    ProtocolExceptionMessageSeeds(String key, String defaultTranslation, Level level) {
        this.key = key;
        this.defaultTranslation = defaultTranslation;
        this.level = level;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public int getNumber() {
        return this.ordinal();
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public String getModule() {
        return "PR1";
    }
}