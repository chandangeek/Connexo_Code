package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 16/05/14
 * Time: 09:12
 */
public enum MessageSeeds implements MessageSeed, TranslationKey {

    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(100, Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Unrestricted authentication", Level.INFO),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1(103, Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Restricted authentication", Level.INFO),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2(105, Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Read only authentication", Level.INFO),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0(107, Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0, "No encryption", Level.INFO),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1(109, Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1, "Clear text with authentication", Level.INFO),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2(111, Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2, "Message encryption and authentication", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(112, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1(113, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Low level authentication", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2(114, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Manufacturer specific authentication", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3(115, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3, "High level authentication using MD5", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4(116, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4, "High level authentication using SHA1", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5(117, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5, "High level authentication using GMAC", Level.INFO),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0(118, Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0, "No message encryption", Level.INFO),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1(119, Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1, "Message authentication", Level.INFO),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2(120, Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2, "Message encryption", Level.INFO),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3(121, Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3, "Message encryption and authentication", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0(122, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0, "No Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1(123, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1, "Low level Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10(124, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10, "SHA - 1Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11(125, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11, "GMAC Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12(126, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12, "No Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13(127, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13, "Low level Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15(128, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15, "Md5 Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16(129, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16, "SHA - 1Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17(130, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17, "GMAC Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18(131, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18, "No Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19(132, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19, "Low level Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21(133, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21, "Md5 Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22(134, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22, "SHA - 1Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23(135, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23, "GMAC Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24(136, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24, "No Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25(137, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25, "Low level Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27(138, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27, "Md5 Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28(139, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28, "SHA - 1Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29(140, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29, "GMAC Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3(141, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3, "Md5 Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30(142, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30, "No Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31(143, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31, "Low level Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33(144, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33, "Md5 Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34(145, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34, "SHA - 1Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35(146, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35, "GMAC Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4(147, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4, "SHA - 1Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5(148, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5, "GMAC Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6(149, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6, "No Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7(150, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7, "Low level Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9(151, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9, "Md5 Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0(152, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0, "No Encryption Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1(153, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1, "Message Encryption Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10(154, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10, "Message Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11(155, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11, "Message Encryption and Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12(156, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12, "No Encryption Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13(157, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13, "Message Encryption Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14(158, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14, "Message Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15(159, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15, "Message Encryption and Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16(160, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16, "No Encryption Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17(161, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17, "Message Encryption Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18(162, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18, "Message Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19(163, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19, "Message Encryption and Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2(164, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2, "Message Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20(165, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20, "No Encryption Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21(166, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21, "Message Encryption Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22(167, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22, "Message Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23(168, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23, "Message Encryption and Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3(169, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3, "Message Encryption and Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4(170, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4, "No Encryption DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5(171, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5, "Message Encryption DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6(172, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6, "Message Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7(173, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7, "Message Encryption and Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8(174, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8, "No Encryption Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9(175, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9, "Message Encryption Extended DataCollection client", Level.INFO),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(177, Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No Authentication", Level.INFO),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1(179, Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Level 1authentication", Level.INFO),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2(181, Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Level 2authentication", Level.INFO),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3(183, Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3, "Level three authentication", Level.INFO),
    MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(184, Keys.MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Default authentication", Level.INFO),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0(185, Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0, "KeyT encryption", Level.INFO),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1(186, Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1, "KeyC encryption", Level.INFO),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2(187, Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2, "KeyF encryption", Level.INFO),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(189, Keys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication", Level.INFO),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1(191, Keys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Password protection", Level.INFO),
    NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(192, Keys.NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication", Level.INFO),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10(193, Keys.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10, "Standard authentication", Level.INFO),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20(194, Keys.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20, "Standard encryption", Level.INFO),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10(196, Keys.PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10, "Standard authentication", Level.INFO),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20(198, Keys.PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20, "Standard encryption", Level.INFO),
    SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(200, Keys.SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Password authentication", Level.INFO),
    WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(202, Keys.WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Wavenis authentication", Level.INFO),
    WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0(204, Keys.WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0, "Standard Wavenis encryption", Level.INFO),
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
    TIMEOUT(243, "protocol.timeout", "Timeout", Level.INFO),
    RETRIES(243, "protocol.retries", "Retries", Level.INFO),
    DEVICEDIALHOMEID(244, "deviceDialHomeId", "Device call home ID", Level.INFO),
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
    G3_NETWORK_MANAGEMENT(259, "G3NetworkManagement", "", Level.SEVERE);
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

    public static class Keys {

        public static final String ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0 = "AnsiC12SecuritySupport.authenticationlevel.0";
        public static final String ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1 = "AnsiC12SecuritySupport.authenticationlevel.1";
        public static final String ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2 = "AnsiC12SecuritySupport.authenticationlevel.2";
        public static final String ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0 = "AnsiC12SecuritySupport.encryptionlevel.0";
        public static final String ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1 = "AnsiC12SecuritySupport.encryptionlevel.1";
        public static final String ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2 = "AnsiC12SecuritySupport.encryptionlevel.2";
        public static final String DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0 = "DlmsSecuritySupport.authenticationlevel.0";
        public static final String DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1 = "DlmsSecuritySupport.authenticationlevel.1";
        public static final String DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2 = "DlmsSecuritySupport.authenticationlevel.2";
        public static final String DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3 = "DlmsSecuritySupport.authenticationlevel.3";
        public static final String DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4 = "DlmsSecuritySupport.authenticationlevel.4";
        public static final String DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5 = "DlmsSecuritySupport.authenticationlevel.5";
        public static final String DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0 = "DlmsSecuritySupport.encryptionlevel.0";
        public static final String DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1 = "DlmsSecuritySupport.encryptionlevel.1";
        public static final String DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2 = "DlmsSecuritySupport.encryptionlevel.2";
        public static final String DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3 = "DlmsSecuritySupport.encryptionlevel.3";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0 = "DlmsSecuritySupportPerClient.authenticationlevel.0";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1 = "DlmsSecuritySupportPerClient.authenticationlevel.1";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10 = "DlmsSecuritySupportPerClient.authenticationlevel.10";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11 = "DlmsSecuritySupportPerClient.authenticationlevel.11";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12 = "DlmsSecuritySupportPerClient.authenticationlevel.12";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13 = "DlmsSecuritySupportPerClient.authenticationlevel.13";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15 = "DlmsSecuritySupportPerClient.authenticationlevel.15";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16 = "DlmsSecuritySupportPerClient.authenticationlevel.16";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17 = "DlmsSecuritySupportPerClient.authenticationlevel.17";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18 = "DlmsSecuritySupportPerClient.authenticationlevel.18";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19 = "DlmsSecuritySupportPerClient.authenticationlevel.19";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21 = "DlmsSecuritySupportPerClient.authenticationlevel.21";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22 = "DlmsSecuritySupportPerClient.authenticationlevel.22";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23 = "DlmsSecuritySupportPerClient.authenticationlevel.23";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24 = "DlmsSecuritySupportPerClient.authenticationlevel.24";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25 = "DlmsSecuritySupportPerClient.authenticationlevel.25";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27 = "DlmsSecuritySupportPerClient.authenticationlevel.27";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28 = "DlmsSecuritySupportPerClient.authenticationlevel.28";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29 = "DlmsSecuritySupportPerClient.authenticationlevel.29";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3 = "DlmsSecuritySupportPerClient.authenticationlevel.3";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30 = "DlmsSecuritySupportPerClient.authenticationlevel.30";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31 = "DlmsSecuritySupportPerClient.authenticationlevel.31";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33 = "DlmsSecuritySupportPerClient.authenticationlevel.33";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34 = "DlmsSecuritySupportPerClient.authenticationlevel.34";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35 = "DlmsSecuritySupportPerClient.authenticationlevel.35";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4 = "DlmsSecuritySupportPerClient.authenticationlevel.4";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5 = "DlmsSecuritySupportPerClient.authenticationlevel.5";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6 = "DlmsSecuritySupportPerClient.authenticationlevel.6";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7 = "DlmsSecuritySupportPerClient.authenticationlevel.7";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9 = "DlmsSecuritySupportPerClient.authenticationlevel.9";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0 = "DlmsSecuritySupportPerClient.encryptionlevel.0";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1 = "DlmsSecuritySupportPerClient.encryptionlevel.1";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10 = "DlmsSecuritySupportPerClient.encryptionlevel.10";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11 = "DlmsSecuritySupportPerClient.encryptionlevel.11";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12 = "DlmsSecuritySupportPerClient.encryptionlevel.12";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13 = "DlmsSecuritySupportPerClient.encryptionlevel.13";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14 = "DlmsSecuritySupportPerClient.encryptionlevel.14";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15 = "DlmsSecuritySupportPerClient.encryptionlevel.15";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16 = "DlmsSecuritySupportPerClient.encryptionlevel.16";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17 = "DlmsSecuritySupportPerClient.encryptionlevel.17";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18 = "DlmsSecuritySupportPerClient.encryptionlevel.18";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19 = "DlmsSecuritySupportPerClient.encryptionlevel.19";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2 = "DlmsSecuritySupportPerClient.encryptionlevel.2";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20 = "DlmsSecuritySupportPerClient.encryptionlevel.20";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21 = "DlmsSecuritySupportPerClient.encryptionlevel.21";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22 = "DlmsSecuritySupportPerClient.encryptionlevel.22";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23 = "DlmsSecuritySupportPerClient.encryptionlevel.23";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3 = "DlmsSecuritySupportPerClient.encryptionlevel.3";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4 = "DlmsSecuritySupportPerClient.encryptionlevel.4";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5 = "DlmsSecuritySupportPerClient.encryptionlevel.5";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6 = "DlmsSecuritySupportPerClient.encryptionlevel.6";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7 = "DlmsSecuritySupportPerClient.encryptionlevel.7";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8 = "DlmsSecuritySupportPerClient.encryptionlevel.8";
        public static final String DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9 = "DlmsSecuritySupportPerClient.encryptionlevel.9";
        public static final String IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0 = "IEC1107SecuritySupport.authenticationlevel.0";
        public static final String IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1 = "IEC1107SecuritySupport.authenticationlevel.1";
        public static final String IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2 = "IEC1107SecuritySupport.authenticationlevel.2";
        public static final String IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3 = "IEC1107SecuritySupport.authenticationlevel.3";
        public static final String MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0 = "Mtu155SecuritySupport.authenticationlevel.0";
        public static final String MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0 = "Mtu155SecuritySupport.encryptionlevel.0";
        public static final String MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1 = "Mtu155SecuritySupport.encryptionlevel.1";
        public static final String MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2 = "Mtu155SecuritySupport.encryptionlevel.2";
        public static final String NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0 = "NoOrPasswordSecuritySupport.authenticationlevel.0";
        public static final String NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1 = "NoOrPasswordSecuritySupport.authenticationlevel.1";
        public static final String NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0 = "NoSecuritySupport.authenticationlevel.0";
        public static final String PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10 = "PasswordWithLevelSecuritySupport.accesslevel.10";
        public static final String PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20 = "PasswordWithLevelSecuritySupport.accesslevel.20";
        public static final String PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10 = "PasswordWithUserIdentificationSecuritySupport.accesslevel.10";
        public static final String PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20 = "PasswordWithUserIdentificationSecuritySupport.accesslevel.20";
        public static final String SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0 = "SimplePasswordSecuritySupport.authenticationlevel.0";
        public static final String WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0 = "WavenisSecuritySupport.authenticationlevel.0";
        public static final String WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0 = "WavenisSecuritySupport.encryptionlevel.0";

    }
}
