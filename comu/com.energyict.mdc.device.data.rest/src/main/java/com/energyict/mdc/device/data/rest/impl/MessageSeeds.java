package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import com.energyict.mdc.protocol.api.ConnectionType;
import java.text.MessageFormat;
import java.util.logging.Level;
public enum MessageSeeds implements MessageSeed {


    NO_SUCH_DEVICE(14, "noSuchDevice", "No device with mrId {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_PARTIAL_CONNECTION_TASK(16, "NoSuchPartialConnectionTask", "No such connection method on device config"),
    NO_SUCH_CONNECTION_METHOD(17, "NoSuchConnectionTask" , "Device {0} has no connection method {1}"),
    NO_SUCH_REGISTER(18, "NoSuchRegister" , "No register with id {0}"),
    NO_SUCH_COM_SCHEDULE(19, "NoSuchComSchedule" , "No communication schedule with id {0}"),
    DEVICE_VALIDATION_BULK_MSG(20, "DeviceValidationBulkMessage" , "This bulk operation for {0} schedule on {1} device is invalid"),
    NO_SUCH_READING(21, "NoSuchReading" , "Register {0} has no reading with id {1}"),
    INVALID_DATE(22, "InvalidDate", "Date should be less or equal to {0}"),
    NO_SUCH_LOAD_PROFILE_ON_DEVICE(23, "NoSuchLoadProfile", "Device {0} has no load profile {1}"),
    NO_SUCH_CHANNEL_ON_LOAD_PROFILE(30, "NoSuchChannel", "Load profile {0} has no channel {1}"),
    NO_CHANNELS_ON_REGISTER(72, "NoChannelsOnRegister", "Register {0} has no channels"),
    NO_SUCH_READING_ON_REGISTER(73, "NoSuchReadingOnRegister", "Register {0} has no reading with timestamp {1}"),
    NO_SUCH_LOG_BOOK_ON_DEVICE(24, "NoSuchLogBook", "Device {0} has no log book {1}"),
    CONNECTION_TYPE_STRATEGY_NOT_APPLICABLE(25, "connectionTypeStrategy.notApplicable", "Not applicable"),
    UPDATE_URGENCY_NOT_ALLOWED(26,"urgencyUpdateNotAllowed" ,"Urgency update not allowed"),
    UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED(27,"updateDialectPropertiesNotAllowed" ,"Protocol dialect update not allowed"),
    UPDATE_CONNECTION_METHOD_NOT_ALLOWED(28,"updateConnectionMethodNotAllowed" ,"Connection method update not allowed"),
    RUN_COMTASK__NOT_ALLOWED(29,"runComTaskNotAllowed" ,"Running of this communication task is not allowed"),
    POWERDOWN(31, ProfileStatus.Flag.POWERDOWN.name(), "Power down"),
    POWERUP(32, ProfileStatus.Flag.POWERUP.name(), "Power up"),
    SHORTLONG(33, ProfileStatus.Flag.SHORTLONG.name(), "Short long"),
    WATCHDOGRESET(34, ProfileStatus.Flag.WATCHDOGRESET.name(), "Watchdog reset"),
    CONFIGURATIONCHANGE(45, ProfileStatus.Flag.CONFIGURATIONCHANGE.name(), "Configuration change"),
    CORRUPTED(46, ProfileStatus.Flag.CORRUPTED.name(), "Corrupted"),
    OVERFLOW(47, ProfileStatus.Flag.OVERFLOW.name(), "Overflow"),
    RESERVED1(48, ProfileStatus.Flag.RESERVED1.name(), "Reserved 1"),
    RESERVED4(49, ProfileStatus.Flag.RESERVED4.name(), "Reserved 4"),
    RESERVED5(50, ProfileStatus.Flag.RESERVED5.name(), "Reserved 5"),
    MISSING(51, ProfileStatus.Flag.MISSING.name(), "Missing"),
    SHORT(52, ProfileStatus.Flag.SHORT.name(), "Short"),
    LONG(53, ProfileStatus.Flag.LONG.name(), "Long"),
    OTHER(54, ProfileStatus.Flag.OTHER.name(), "Other"),
    REVERSERUN(55, ProfileStatus.Flag.REVERSERUN.name(), "Reverse run"),
    PHASEFAILURE(56, ProfileStatus.Flag.PHASEFAILURE.name(), "Phase failure"),
    BADTIME(57, ProfileStatus.Flag.BADTIME.name(), "Bad time"),
    DEVICE_ERROR(58, ProfileStatus.Flag.DEVICE_ERROR.name(), "Device error"),
    BATTERY_LOW(59, ProfileStatus.Flag.BATTERY_LOW.name(), "Battery low"),
    TEST(60, ProfileStatus.Flag.TEST.name(), "Test"),
    NULL_DATE(61, "NullDate", "Date must be filled in"),
    DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE(62, "DeactivateValidationRuleSetNotPossible", "Deactivate of validation rule set {0} is currently not possible."),
    PENDING(63, "Pending", "Pending"),
    FAILED(64, "Failed", "Failed"),
    BUSY(65, "Busy", "Busy"),
    ON_HOLD(66, "OnHold", "Inactive"),
    RETRYING(67, "Retrying", "Retrying"),
    NEVER_COMPLETED(68, "NeverCompleted", "Never completed"),
    WAITING(69, "Waiting", "Waiting"),
    DEFAULT(70, "Default", "Default"),
    DEFAULT_NOT_DEFINED(71, "DefaultNotDefined", "Default (not defined yet)"),
    SUCCESS(74, "Success", "Success"),
    BROKEN(75, "Broken", "Broken"),
    SETUP_ERROR(76, "SetupError", "Setup error"),
    FAILURE(77, "Failure", "Failure"),
    CONNECTION_ERROR(78, "ConnectionError", "Connection error"),
    CONFIGURATION_ERROR(79, "ConfigurationError", "Configuration error"),
    CONFIGURATION_WARNING(80, "ConfigurationWarning", "Configuration warning"),
    IO_ERROR(81, "IoError", "I/O error"),
    PROTOCOL_ERROR(82, "ProtocolError", "Protocol error"),
    OK(83, "OK", "Ok"),
    RESCHEDULED(84, "Rescheduled", "Rescheduled"),
    TIME_ERROR(85, "TimeError", "Time error"),
    UNEXPECTED_ERROR(86, "UnexpectedError", "Unexpected error"),
    INDIVIDUAL(87, "Individual", "Individual"),
    NO_SUCH_COM_SESSION_ON_CONNECTION_METHOD(88,"noSuchComSession" ,"No such communication session exists for this connection method"),
    INBOUND(89, ConnectionType.Direction.INBOUND.name(), "Inbound"),
    OUTBOUND(90, ConnectionType.Direction.OUTBOUND.name(), "Outbound"),
    NO_SUCH_COM_TASK(91, "NoSucComTaskOnDevice", "No such communication task exists for device ''{0}''"),
    COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE(92, "NoEnablementForDevice", "Communication task ''{0}'' is not enabled for device ''{1}''"),
    NO_SUCH_COM_TASK_EXEC_SESSION(93, "NoSuchComTaskExecSession", "The communication task logging could not be found"),
    DEVICEGROUPNAME_ALREADY_EXISTS(94, "deviceGroupNameAlreadyExists", "A devicegroup with name {0} already exists"),    
    COMPLETE(95, "Complete", "Complete"),
    INCOMPLETE(96, "Incomplete", "Incomplete"),
    NO_SUCH_SECURITY_PROPERTY_SET_ON_DEVICE(97, "NoSuchSecurityPropertySetOnDevice", "No security settings with id {0} exist for device ''{1}''"),
    NO_SUCH_SECURITY_PROPERTY_SET(98, "NoSuchSecurityPropertySet", "No security settings with id {0} exist"),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(100, Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Unrestricted authentication"),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1(103, Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Restricted authentication"),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2(105, Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Read only authentication"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0(107, Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0, "No encryption"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1(109, Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1, "Clear text with authentication"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2(111, Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2, "Message encryption and authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(112, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1(113, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Low level authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2(114, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Manufacturer specific authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3(115, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3, "High level authentication using MD5"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4(116, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4, "High level authentication using SHA1"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5(117, Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5, "High level authentication using GMAC"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0(118, Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0, "No message encryption"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1(119, Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1, "Message authentication"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2(120, Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2, "Message encryption"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3(121, Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3, "Message encryption and authentication"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0(122, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0, "No Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1(123, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1, "Low level Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10(124, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10, "SHA - 1Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11(125, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11, "GMAC Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12(126, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12, "No Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13(127, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13, "Low level Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15(128, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15, "Md5 Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16(129, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16, "SHA - 1Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17(130, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17, "GMAC Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18(131, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18, "No Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19(132, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19, "Low level Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21(133, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21, "Md5 Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22(134, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22, "SHA - 1Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23(135, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23, "GMAC Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24(136, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24, "No Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25(137, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25, "Low level Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27(138, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27, "Md5 Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28(139, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28, "SHA - 1Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29(140, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29, "GMAC Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3(141, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3, "Md5 Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30(142, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30, "No Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31(143, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31, "Low level Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33(144, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33, "Md5 Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34(145, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34, "SHA - 1Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35(146, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35, "GMAC Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4(147, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4, "SHA - 1Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5(148, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5, "GMAC Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6(149, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6, "No Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7(150, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7, "Low level Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9(151, Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9, "Md5 Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0(152, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0, "No Encryption Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1(153, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1, "Message Encryption Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10(154, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10, "Message Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11(155, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11, "Message Encryption and Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12(156, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12, "No Encryption Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13(157, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13, "Message Encryption Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14(158, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14, "Message Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15(159, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15, "Message Encryption and Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16(160, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16, "No Encryption Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17(161, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17, "Message Encryption Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18(162, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18, "Message Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19(163, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19, "Message Encryption and Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2(164, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2, "Message Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20(165, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20, "No Encryption Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21(166, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21, "Message Encryption Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22(167, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22, "Message Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23(168, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23, "Message Encryption and Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3(169, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3, "Message Encryption and Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4(170, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4, "No Encryption DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5(171, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5, "Message Encryption DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6(172, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6, "Message Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7(173, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7, "Message Encryption and Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8(174, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8, "No Encryption Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9(175, Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9, "Message Encryption Extended DataCollection client"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(177, Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No Authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1(179, Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Level 1authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2(181, Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Level 2authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3(183, Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3, "Level three authentication"),
    MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(184, Keys.MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Default authentication"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0(185, Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0, "KeyT encryption"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1(186, Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1, "KeyC encryption"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2(187, Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2, "KeyF encryption"),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(189, Keys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication"),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1(191, Keys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Password protection"),
    NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(192, Keys.NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication"),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10(193, Keys.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10, "Standard authentication"),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20(194, Keys.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20, "Standard encryption"),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10(196, Keys.PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10, "Standard authentication"),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20(198, Keys.PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20, "Standard encryption"),
    SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(200, Keys.SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Password authentication"),
    WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(202, Keys.WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Wavenis authentication"),
    WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0(204, Keys.WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0, "Standard Wavenis encryption"),
    NOT_DEFINED_YET(205, "NotDefinedYet", "(not defined yet)"),;


    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return DeviceApplication.COMPONENT_NAME;
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
        return format;
    }

    public String format(Thesaurus thesaurus, Object... args){
        if (thesaurus == null){
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }
    @Override
    public Level getLevel() {
        return Level.SEVERE;
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
