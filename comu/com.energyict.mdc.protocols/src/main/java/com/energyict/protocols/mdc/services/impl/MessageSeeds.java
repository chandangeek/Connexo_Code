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

    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(100, Constants.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Unrestricted authentication", Level.INFO),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1(103, Constants.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Restricted authentication", Level.INFO),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2(105, Constants.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Read only authentication", Level.INFO),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0(107, Constants.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0, "No encryption", Level.INFO),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1(109, Constants.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1, "Clear text with authentication", Level.INFO),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2(111, Constants.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2, "Message encryption and authentication", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(112, Constants.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1(113, Constants.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Low level authentication", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2(114, Constants.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Manufacturer specific authentication", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3(115, Constants.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3, "High level authentication using MD5", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4(116, Constants.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4, "High level authentication using SHA1", Level.INFO),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5(117, Constants.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5, "High level authentication using GMAC", Level.INFO),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0(118, Constants.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0, "No message encryption", Level.INFO),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1(119, Constants.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1, "Message authentication", Level.INFO),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2(120, Constants.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2, "Message encryption", Level.INFO),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3(121, Constants.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3, "Message encryption and authentication", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0(122, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0, "No Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1(123, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1, "Low level Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10(124, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10, "SHA - 1Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11(125, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11, "GMAC Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12(126, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12, "No Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13(127, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13, "Low level Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15(128, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15, "Md5 Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16(129, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16, "SHA - 1Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17(130, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17, "GMAC Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18(131, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18, "No Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19(132, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19, "Low level Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21(133, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21, "Md5 Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22(134, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22, "SHA - 1Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23(135, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23, "GMAC Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24(136, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24, "No Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25(137, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25, "Low level Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27(138, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27, "Md5 Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28(139, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28, "SHA - 1Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29(140, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29, "GMAC Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3(141, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3, "Md5 Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30(142, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30, "No Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31(143, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31, "Low level Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33(144, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33, "Md5 Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34(145, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34, "SHA - 1Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35(146, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35, "GMAC Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4(147, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4, "SHA - 1Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5(148, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5, "GMAC Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6(149, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6, "No Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7(150, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7, "Low level Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9(151, Constants.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9, "Md5 Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0(152, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0, "No Encryption Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1(153, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1, "Message Encryption Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10(154, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10, "Message Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11(155, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11, "Message Encryption and Authentication Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12(156, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12, "No Encryption Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13(157, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13, "Message Encryption Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14(158, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14, "Message Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15(159, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15, "Message Encryption and Authentication Management client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16(160, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16, "No Encryption Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17(161, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17, "Message Encryption Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18(162, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18, "Message Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19(163, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19, "Message Encryption and Authentication Firmware client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2(164, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2, "Message Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20(165, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20, "No Encryption Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21(166, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21, "Message Encryption Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22(167, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22, "Message Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23(168, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23, "Message Encryption and Authentication Manufacturer client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3(169, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3, "Message Encryption and Authentication Public client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4(170, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4, "No Encryption DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5(171, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5, "Message Encryption DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6(172, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6, "Message Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7(173, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7, "Message Encryption and Authentication DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8(174, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8, "No Encryption Extended DataCollection client", Level.INFO),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9(175, Constants.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9, "Message Encryption Extended DataCollection client", Level.INFO),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(177, Constants.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No Authentication", Level.INFO),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1(179, Constants.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Level 1authentication", Level.INFO),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2(181, Constants.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Level 2authentication", Level.INFO),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3(183, Constants.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3, "Level three authentication", Level.INFO),
    MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(184, Constants.MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Default authentication", Level.INFO),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0(185, Constants.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0, "KeyT encryption", Level.INFO),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1(186, Constants.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1, "KeyC encryption", Level.INFO),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2(187, Constants.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2, "KeyF encryption", Level.INFO),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(189, Constants.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication", Level.INFO),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1(191, Constants.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Password protection", Level.INFO),
    NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(192, Constants.NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication", Level.INFO),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10(193, Constants.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10, "Standard authentication", Level.INFO),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20(194, Constants.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20, "Standard encryption", Level.INFO),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10(196, Constants.PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10, "Standard authentication", Level.INFO),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20(198, Constants.PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20, "Standard encryption", Level.INFO),
    SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(200, Constants.SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Password authentication", Level.INFO),
    WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(202, Constants.WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Wavenis authentication", Level.INFO),
    WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0(204, Constants.WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0, "Standard Wavenis encryption", Level.INFO),;

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

    public static class Constants {

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
