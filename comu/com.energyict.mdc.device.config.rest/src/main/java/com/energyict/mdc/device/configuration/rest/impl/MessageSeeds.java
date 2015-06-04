package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.security.Privileges;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    FIELD_IS_REQUIRED(1, "RequiredField", "This field is required"),
    PROTOCOL_INVALID_NAME(2,"deviceType.no.such.protocol", "A protocol with name ''{0}'' does not exist"),
    NO_LOGBOOK_TYPE_ID_FOR_ADDING(3,"NoLogBookTypeIdForAdding", "User should specify ids of LogBook Type for adding"),
    NO_LOGBOOK_TYPE_FOUND(4, "NoLogBookTypeFound", "No LogBook type with id {0}"),
    NO_LOGBOOK_SPEC_FOUND(5, "NoLogBookSpecFound", "No LogBook configuration with id {0}"),
    NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING(6, "NoLoadProfileTypeIdForAdding", "User should specify ids of Load Profile Type for adding"),
    NO_LOAD_PROFILE_TYPE_FOUND(7, "NoLoadProfileTypeFound", "No Load Profile type with id {0}"),
    NO_CHANNEL_SPEC_FOUND(9, "NoChannelSpecFound", "No channel specification with id {0}"),
    INVALID_REFERENCE_TO_REGISTER_TYPE(10, "NoSuchRegisterType", "Register type could not be found"),
    DUPLICATE_OBISCODE(11, "DuplicateObisCode", "A register mapping with obis code ''{0}'', unit ''{1}'' and time of use ''{2}'' already exists"),
    AS_SOON_AS_POSSIBLE(12, "asSoonAsPossible", "As soon a possible"),
    MINIMIZE_CONNECTIONS(13, "minimizeConnections", "Minimize connections"),
    NO_SUCH_DEVICE(14, "noSuchDevice", "No device with id {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_CONNECTION_TASK(16, "NoSuchConnectionTask", "No such connection task"),
    NO_DEVICECONFIG_ID_FOR_ADDING(17, "NoDeviceConfigurationIdForAdding", "User should specify ids of Device Configuration for adding"),
    CONNECTION_TYPE_UNKNOWN(18, "NoSuchConnectionType", "No connection type pluggable class could be found for ''{0}''"),
    NO_VALIDATIONRULESET_ID_FOR_ADDING(20,"NoValidationRuleSetIdForAdding", "User should specify ids of Validation Ruleset for adding"),
    EXECUTE_COM_TASK_LEVEL1(21, Privileges.EXECUTE_COM_TASK_1, "Execute com task (level 1)"),
    EXECUTE_COM_TASK_LEVEL2(22, Privileges.EXECUTE_COM_TASK_2, "Execute com task (level 2)"),
    EXECUTE_COM_TASK_LEVEL3(23, Privileges.EXECUTE_COM_TASK_3, "Execute com task (level 3)"),
    EXECUTE_COM_TASK_LEVEL4(24, Privileges.EXECUTE_COM_TASK_4, "Execute com task (level 4)"),
    EDIT_DEVICE_SECURITY_PROPERTIES_1(25, Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_1, "Edit device security settings (level 1)"),
    EDIT_DEVICE_SECURITY_PROPERTIES_2(26, Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_2, "Edit device security settings (level 2)"),
    EDIT_DEVICE_SECURITY_PROPERTIES_3(27, Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_3, "Edit device security settings (level 3)"),
    EDIT_DEVICE_SECURITY_PROPERTIES_4(28, Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_4, "Edit device security settings (level 4)"),
    VIEW_DEVICE_SECURITY_PROPERTIES_1(29, Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_1, "View device security settings (level 1)"),
    VIEW_DEVICE_SECURITY_PROPERTIES_2(30, Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_2, "View device security settings (level 2)"),
    VIEW_DEVICE_SECURITY_PROPERTIES_3(31, Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_3, "View device security settings (level 3)"),
    VIEW_DEVICE_SECURITY_PROPERTIES_4(32, Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_4, "View device security settings (level 4)"),
    UNKNOWN_PRIVILEGE_ID(33, "NoSuchExecutionLevels", "No such execution levels: {0}"),
    NO_SUCH_DEVICE_MESSAGE_SPEC(34, "NoSuchDeviceMessageSpec", "No such device message spec: {0}"),
    EXECUTE_DEVICE_MESSAGE_LEVEL1(35, Privileges.EXECUTE_DEVICE_MESSAGE_1, "Level 1"),
    EXECUTE_DEVICE_MESSAGE_LEVEL2(36, Privileges.EXECUTE_DEVICE_MESSAGE_2, "Level 2"),
    EXECUTE_DEVICE_MESSAGE_LEVEL3(37, Privileges.EXECUTE_DEVICE_MESSAGE_3, "Level 3"),
    EXECUTE_DEVICE_MESSAGE_LEVEL4(38, Privileges.EXECUTE_DEVICE_MESSAGE_4, "Level 4"),
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
    DEFAULT(205, "Default", "Default"),
    NO_SUCH_DEVICE_LIFE_CYCLE(206, "NoSuchDeviceLifeCycle", "There is no device life cycle with id = {0}"),
    UNABLE_TO_CHANGE_DEVICE_LIFE_CYCLE(207, "UnableToChangeDeviceLifeCycle", "Unable to change device life cycle to \"{0}\""),

    ;

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
        return DeviceConfigurationApplication.COMPONENT_NAME;
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
