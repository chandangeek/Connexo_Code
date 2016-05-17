package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 16/05/14
 * Time: 09:12
 */
public enum MessageSeeds implements MessageSeed {

    PROTOCOL_IO_PARSE_ERROR(206, "dataParseException.protocolIOError", "Protocol parse error: {0}.", Level.SEVERE),
    INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION(207, "dataParseException.indexOutOfBounds", "Referenced a non-existing index: {0}.", Level.SEVERE),
    PROTOCOL_CONNECT_FAILED(208, "protocolConnect.failed", "The logical connect to a device failed: {0}", Level.SEVERE),
    NUMERIC_PARAMETER_EXPECTED(209, "numericParameterExpected", "The parameter {0} is expected to be numeric but got {1}", Level.SEVERE),
    UNSUPPORTED_URL_CONTENT_TYPE(210, "unsupportedURLContentType", "Unsupported URL content type: {0}", Level.SEVERE),
    UNSUPPORTED_VERSION(211, "unsupportedVersion", "Version {0} is not supported for {1}", Level.SEVERE),
    NO_INBOUND_DATE(212, "noInboundData", "Device with id {0} did not send expected data", Level.SEVERE),
    NUMBER_OF_RETRIES_REACHED(213, "numberOfRetriesReached", "Maximum number of retries ({0}) reached.", Level.SEVERE),
    GENERIC_JAVA_REFLECTION_ERROR(214, "genericJavaReflectionError", "Unable to create an instance of the class {0}", Level.SEVERE),
    UNSUPPORTED_LEGACY_PROTOCOL_TYPE(215, "unsupportedLegacyProtocolType", "The legacy protocol class {0} is not or no longer supported", Level.SEVERE),
    UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS(216, "unknownDeviceSecuritySupportClass", "The DeviceSecuritySupport class '{0}' is not known on the classpath", Level.SEVERE),
    UNKNOWN_DEVICE_MESSAGE_CONVERTER_CLASS(217, "unknownDeviceMessageConverterClass", "The DeviceSecuritySupport class '{0}' is not known on the classpath", Level.SEVERE),
    NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION(218, "deviceNotConfiguredForInboundCommunication", "Device '{0}' is not configured for inbound communication", Level.SEVERE),
    DUPLICATE_FOUND(219, "duplicateFound", "A duplicate '{0}' was found when a unique result was expected for '{1}'", Level.SEVERE),
    GENERAL_PARSE_ERROR(220, "generalParseError", "A general parsing error occured\\: {0}", Level.SEVERE),
    MISSING_MODULE(221, "missingModule", "Module supporting class '{0}' could not be found", Level.SEVERE),
    ENCRYPTION_ERROR(222, "encryptionError", "Failure to decrypt encrypted data received from device", Level.SEVERE),
    INBOUND_UNEXPECTED_FRAME(223, "unexpectedInboundFrame", "Received an unexpected first inbound frame\\: '{0}'. {1}", Level.SEVERE),
    INBOUND_TIMEOUT(224, "inboundTimeout", "A timeout occurred while trying to receive an inbound frame\\: {0}", Level.SEVERE),
    UNEXPECTED_IO_EXCEPTION(226, "unexpectedIOException", "Exception occurred while communication with a device", Level.SEVERE),
    RETRIES(243, "protocol.retries.message", "Retries", Level.INFO),
    EVENT_VALUE(245, "protocol.eventvalue", "Value", Level.INFO),
    UNSUPPORTED_AUTHENTICATION_TYPE(246, "authentication.unsupported", "This is an unsupported authentication level : {0}", Level.SEVERE),
    UNKNOWN_ENCRYPTION_ALGORITHM(247, "encryption.unknown.algorithm", "Unknown encryption algorithm", Level.SEVERE),
    INCORRECT_AUTHENTICATION_RESPONSE(248, "authentication.response.incorrect", "Received an incorrect authentication response", Level.SEVERE),
    INCORRECT_FRAMECOUNTER_RECEIVED(249, "framecounter.incorrect", "Received an incorrect framecounter", Level.SEVERE),
    AUTHENTICATION_FAILED(250, "authentication.failed", "The authentication to the device failed", Level.SEVERE),
    PROTOCOL_DISCONNECT_FAILED(251, "protocol.disconnect.failed", "The logical disconnect of a device failed", Level.SEVERE),
    UNEXPECTED_COMCHANNEL(252, "unexpected.comchannel", "Unexpected ComChannel, expected {0}, but was {1}", Level.SEVERE),
    INTERRUPTED_DURING_COMMUNICATION(253, "communication.interrupted", "The communication thread got interrupted, communication ended", Level.SEVERE),
    UNSUPPORTED_METHOD(254, "unsupportedMethod", "Method {1} is not supported for class {0}", Level.SEVERE),
    IPv6_SETUP(255, "IPv6Setup", "", Level.SEVERE),
    SAP_ASSIGNMENT(256, "SAPAssignment", "", Level.SEVERE),
    PLC_OFDM_TYPE2MAC_SETUP(257, "PLCOFDMType2MACSetup", "", Level.SEVERE),
    SIX_LOW_PAN_ADAPTATION_LAYER_SETUP(258, "SixLowPanAdaptationLayerSetup", "", Level.SEVERE),
    G3_NETWORK_MANAGEMENT(259, "G3NetworkManagement", "", Level.SEVERE),
    NOT_ALLOWED_TO_DO_SET_TIME(260, "notAllowed.timeSet", "It is not allowed to do a set time on the device", Level.SEVERE),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

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
        return DeviceProtocolService.COMPONENT_NAME;
    }

}
