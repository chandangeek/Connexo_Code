package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum DefaultTranslationKey implements TranslationKey {
    PRE_TRANSITION_CHECKS_FAILED("PreTransitionChecksFailed" , "Pretransition checks failed"),
    CIM_DATE_RECEIVE("DeviceCimReceivedDate" , "Shipment date"),
    CIM_DATE_INSTALLED("DeviceCimInstalledDate" , "Installation date"),
    CIM_DATE_REMOVE("DeviceCimRemovedDate" , "Deactivation date"),
    CIM_DATE_RETRIED("DeviceCimRetriedDate" , "Decommissioning date"),
    LAST_CHECKED_PROPERTY_NAME(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key() , "Start validation date"),
    CONNECTION_TASK_STATUS_INCOMPLETE(Keys.CONNECTION_TASK_STATUS_INCOMPLETE, "Incomplete"),
    CONNECTION_TASK_STATUS_ACTIVE(Keys.CONNECTION_TASK_STATUS_ACTIVE, "Active"),
    CONNECTION_TASK_STATUS_INACTIVE(Keys.CONNECTION_TASK_STATUS_INACTIVE, "Inactive"),
    POWERDOWN(ProfileStatus.Flag.POWERDOWN.name(), "Power down"),
    POWERUP(ProfileStatus.Flag.POWERUP.name(), "Power up"),
    SHORTLONG(ProfileStatus.Flag.SHORTLONG.name(), "Short long"),
    WATCHDOGRESET(ProfileStatus.Flag.WATCHDOGRESET.name(), "Watchdog reset"),
    CONFIGURATIONCHANGE(ProfileStatus.Flag.CONFIGURATIONCHANGE.name(), "Configuration change"),
    CORRUPTED(ProfileStatus.Flag.CORRUPTED, "Corrupted"),
    OVERFLOW(ProfileStatus.Flag.OVERFLOW, "Overflow"),
    RESERVED1(ProfileStatus.Flag.RESERVED1, "Reserved 1"),
    RESERVED4(ProfileStatus.Flag.RESERVED4, "Reserved 4"),
    RESERVED5(ProfileStatus.Flag.RESERVED5, "Reserved 5"),
    MISSING(ProfileStatus.Flag.MISSING, "Missing"),
    SHORT(ProfileStatus.Flag.SHORT, "Short"),
    LONG(ProfileStatus.Flag.LONG, "Long"),
    OTHER(ProfileStatus.Flag.OTHER, "Other"),
    REVERSERUN(ProfileStatus.Flag.REVERSERUN, "Reverse run"),
    PHASEFAILURE(ProfileStatus.Flag.PHASEFAILURE, "Phase failure"),
    BADTIME(ProfileStatus.Flag.BADTIME, "Bad time"),
    DEVICE_ERROR(ProfileStatus.Flag.DEVICE_ERROR, "Device error"),
    BATTERY_LOW(ProfileStatus.Flag.BATTERY_LOW, "Battery low"),
    TEST(ProfileStatus.Flag.TEST, "Test"),
    DEFAULT("Default", "Default"),
    DEFAULT_NOT_DEFINED("DefaultNotDefined", "Default (not defined yet)"),
    INDIVIDUAL("Individual", "Individual"),
    FAILURE("Failure", "Failure"),
    INBOUND(ConnectionType.Direction.INBOUND.name(), "Inbound"),
    OUTBOUND(ConnectionType.Direction.OUTBOUND.name(), "Outbound"),
    COMPLETE("Complete", "Complete"),
    INCOMPLETE("Incomplete", "Incomplete"),
    ON_REQUEST("onRequest", "On request"),
    SHARED_SCHEDULE("masterSchedule", "Shared schedule"),
    INDIVIDUAL_SCHEDULE("individualSchedule", "Individual schedule"),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Unrestricted authentication"),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1(Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Restricted authentication"),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2(Keys.ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Read only authentication"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0(Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0, "No encryption"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1(Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1, "Clear text with authentication"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2(Keys.ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2, "Message encryption and authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1(Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Low level authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2(Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Manufacturer specific authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3(Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3, "High level authentication using MD5"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4(Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4, "High level authentication using SHA1"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5(Keys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5, "High level authentication using GMAC"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0(Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0, "No message encryption"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1(Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1, "Message authentication"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2(Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2, "Message encryption"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3(Keys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3, "Message encryption and authentication"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0, "No Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1, "Low level Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10, "SHA - 1Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11, "GMAC Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12, "No Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13, "Low level Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15, "Md5 Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16, "SHA - 1Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17, "GMAC Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18, "No Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19, "Low level Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21, "Md5 Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22, "SHA - 1Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23, "GMAC Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24, "No Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25, "Low level Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27, "Md5 Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28, "SHA - 1Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29, "GMAC Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3, "Md5 Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30, "No Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31, "Low level Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33, "Md5 Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34, "SHA - 1Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35, "GMAC Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4, "SHA - 1Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5, "GMAC Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6, "No Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7, "Low level Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9(Keys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9, "Md5 Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0, "No Encryption Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1, "Message Encryption Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10, "Message Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11, "Message Encryption and Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12, "No Encryption Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13, "Message Encryption Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14, "Message Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15, "Message Encryption and Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16, "No Encryption Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17, "Message Encryption Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18, "Message Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19, "Message Encryption and Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2, "Message Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20, "No Encryption Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21, "Message Encryption Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22, "Message Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23, "Message Encryption and Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3, "Message Encryption and Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4, "No Encryption DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5, "Message Encryption DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6, "Message Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7, "Message Encryption and Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8, "No Encryption Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9(Keys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9, "Message Encryption Extended DataCollection client"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No Authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1(Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Level 1authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2(Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2, "Level 2authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3(Keys.IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3, "Level three authentication"),
    MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0(Keys.MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Default authentication"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0(Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0, "KeyT encryption"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1(Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1, "KeyC encryption"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2(Keys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2, "KeyF encryption"),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(Keys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication"),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1(Keys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1, "Password protection"),
    NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(Keys.NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "No authentication"),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10(Keys.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10, "Standard authentication"),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20(Keys.PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20, "Standard encryption"),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10(Keys.PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10, "Standard authentication"),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20(Keys.PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20, "Standard encryption"),
    SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(Keys.SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Password authentication"),
    WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0(Keys.WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0, "Wavenis authentication"),
    WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0(Keys.WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0, "Standard Wavenis encryption"),
    NOT_DEFINED_YET("NotDefinedYet", "(not defined yet)"),
    AS_SOON_AS_POSSIBLE("asSoonAsPossible", "As soon as possible"),
    MINIMIZE_CONNECTIONS("minimizeConnections", "Minimize connections"),
    MDC_LABEL_CATEGORY_FAVORITES("mdc.label.category.favorites", "Favorites"),
    NO_RESTRICTIONS("NoRestrictions", "No restrictions"),
    ;

    private String key;
    private String defaultFormat;

    DefaultTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    DefaultTranslationKey(ProfileStatus.Flag profileStatusFlag, String defaultFormat) {
        this("ProfileStatusFlag." + profileStatusFlag.name(), defaultFormat);
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public String translateWith(Thesaurus thesaurus){
        return thesaurus.getFormat(this).format();
    }

    public static class Keys {
        public static final String CONNECTION_TASK_STATUS_INCOMPLETE = "connectionTaskStatusIncomplete";
        public static final String CONNECTION_TASK_STATUS_ACTIVE = "connectionTaskStatusActive";
        public static final String CONNECTION_TASK_STATUS_INACTIVE = "connectionTaskStatusInActive";
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