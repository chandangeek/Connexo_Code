/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.device.BaseDevice;

public enum TranslationKeys implements TranslationKey {

    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_0("AnsiC12SecuritySupport.authenticationlevel.0", "Unrestricted authentication"),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_1("AnsiC12SecuritySupport.authenticationlevel.1", "Restricted authentication"),
    ANSIC12SECURITYSUPPORT_AUTHENTICATIONLEVEL_2("AnsiC12SecuritySupport.authenticationlevel.2", "Read only authentication"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_0("AnsiC12SecuritySupport.encryptionlevel.0", "No encryption"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_1("AnsiC12SecuritySupport.encryptionlevel.1", "Clear text with authentication"),
    ANSIC12SECURITYSUPPORT_ENCRYPTIONLEVEL_2("AnsiC12SecuritySupport.encryptionlevel.2", "Message encryption and authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0("DlmsSecuritySupport.authenticationlevel.0", "No authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1("DlmsSecuritySupport.authenticationlevel.1", "Low level authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2("DlmsSecuritySupport.authenticationlevel.2", "Manufacturer specific authentication"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3("DlmsSecuritySupport.authenticationlevel.3", "High level authentication using MD5"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4("DlmsSecuritySupport.authenticationlevel.4", "High level authentication using SHA1"),
    DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5("DlmsSecuritySupport.authenticationlevel.5", "High level authentication using GMAC"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0("DlmsSecuritySupport.encryptionlevel.0", "No message encryption"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1("DlmsSecuritySupport.encryptionlevel.1", "Message authentication"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2("DlmsSecuritySupport.encryptionlevel.2", "Message encryption"),
    DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3("DlmsSecuritySupport.encryptionlevel.3", "Message encryption and authentication"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0("DlmsSecuritySupportPerClient.authenticationlevel.0", "No Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1("DlmsSecuritySupportPerClient.authenticationlevel.1", "Low level Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10("DlmsSecuritySupportPerClient.authenticationlevel.10", "SHA - 1Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11("DlmsSecuritySupportPerClient.authenticationlevel.11", "GMAC Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12("DlmsSecuritySupportPerClient.authenticationlevel.12", "No Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13("DlmsSecuritySupportPerClient.authenticationlevel.13", "Low level Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15("DlmsSecuritySupportPerClient.authenticationlevel.15", "Md5 Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16("DlmsSecuritySupportPerClient.authenticationlevel.16", "SHA - 1Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17("DlmsSecuritySupportPerClient.authenticationlevel.17", "GMAC Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18("DlmsSecuritySupportPerClient.authenticationlevel.18", "No Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19("DlmsSecuritySupportPerClient.authenticationlevel.19", "Low level Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21("DlmsSecuritySupportPerClient.authenticationlevel.21", "Md5 Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22("DlmsSecuritySupportPerClient.authenticationlevel.22", "SHA - 1Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23("DlmsSecuritySupportPerClient.authenticationlevel.23", "GMAC Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24("DlmsSecuritySupportPerClient.authenticationlevel.24", "No Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25("DlmsSecuritySupportPerClient.authenticationlevel.25", "Low level Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27("DlmsSecuritySupportPerClient.authenticationlevel.27", "Md5 Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28("DlmsSecuritySupportPerClient.authenticationlevel.28", "SHA - 1Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29("DlmsSecuritySupportPerClient.authenticationlevel.29", "GMAC Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3("DlmsSecuritySupportPerClient.authenticationlevel.3", "Md5 Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30("DlmsSecuritySupportPerClient.authenticationlevel.30", "No Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31("DlmsSecuritySupportPerClient.authenticationlevel.31", "Low level Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33("DlmsSecuritySupportPerClient.authenticationlevel.33", "Md5 Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34("DlmsSecuritySupportPerClient.authenticationlevel.34", "SHA - 1Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35("DlmsSecuritySupportPerClient.authenticationlevel.35", "GMAC Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4("DlmsSecuritySupportPerClient.authenticationlevel.4", "SHA - 1Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5("DlmsSecuritySupportPerClient.authenticationlevel.5", "GMAC Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6("DlmsSecuritySupportPerClient.authenticationlevel.6", "No Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7("DlmsSecuritySupportPerClient.authenticationlevel.7", "Low level Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9("DlmsSecuritySupportPerClient.authenticationlevel.9", "Md5 Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0("DlmsSecuritySupportPerClient.encryptionlevel.0", "No Encryption Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1("DlmsSecuritySupportPerClient.encryptionlevel.1", "Message Encryption Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10("DlmsSecuritySupportPerClient.encryptionlevel.10", "Message Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11("DlmsSecuritySupportPerClient.encryptionlevel.11", "Message Encryption and Authentication Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12("DlmsSecuritySupportPerClient.encryptionlevel.12", "No Encryption Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13("DlmsSecuritySupportPerClient.encryptionlevel.13", "Message Encryption Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14("DlmsSecuritySupportPerClient.encryptionlevel.14", "Message Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15("DlmsSecuritySupportPerClient.encryptionlevel.15", "Message Encryption and Authentication Management client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16("DlmsSecuritySupportPerClient.encryptionlevel.16", "No Encryption Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17("DlmsSecuritySupportPerClient.encryptionlevel.17", "Message Encryption Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18("DlmsSecuritySupportPerClient.encryptionlevel.18", "Message Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19("DlmsSecuritySupportPerClient.encryptionlevel.19", "Message Encryption and Authentication Firmware client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2("DlmsSecuritySupportPerClient.encryptionlevel.2", "Message Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20("DlmsSecuritySupportPerClient.encryptionlevel.20", "No Encryption Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21("DlmsSecuritySupportPerClient.encryptionlevel.21", "Message Encryption Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22("DlmsSecuritySupportPerClient.encryptionlevel.22", "Message Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23("DlmsSecuritySupportPerClient.encryptionlevel.23", "Message Encryption and Authentication Manufacturer client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3("DlmsSecuritySupportPerClient.encryptionlevel.3", "Message Encryption and Authentication Public client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4("DlmsSecuritySupportPerClient.encryptionlevel.4", "No Encryption DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5("DlmsSecuritySupportPerClient.encryptionlevel.5", "Message Encryption DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6("DlmsSecuritySupportPerClient.encryptionlevel.6", "Message Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7("DlmsSecuritySupportPerClient.encryptionlevel.7", "Message Encryption and Authentication DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8("DlmsSecuritySupportPerClient.encryptionlevel.8", "No Encryption Extended DataCollection client"),
    DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9("DlmsSecuritySupportPerClient.encryptionlevel.9", "Message Encryption Extended DataCollection client"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_0("IEC1107SecuritySupport.authenticationlevel.0", "No Authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_1("IEC1107SecuritySupport.authenticationlevel.1", "Level 1 authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_2("IEC1107SecuritySupport.authenticationlevel.2", "Level 2 authentication"),
    IEC1107SECURITYSUPPORT_AUTHENTICATIONLEVEL_3("IEC1107SecuritySupport.authenticationlevel.3", "Level 3 authentication"),
    MTU155SECURITYSUPPORT_AUTHENTICATIONLEVEL_0("Mtu155SecuritySupport.authenticationlevel.0", "Default authentication"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0("Mtu155SecuritySupport.encryptionlevel.0", "KeyT encryption"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1("Mtu155SecuritySupport.encryptionlevel.1", "KeyC encryption"),
    MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2("Mtu155SecuritySupport.encryptionlevel.2", "KeyF encryption"),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0("NoOrPasswordSecuritySupport.authenticationlevel.0", "No authentication"),
    NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_1("NoOrPasswordSecuritySupport.authenticationlevel.1", "Password protection"),
    NOSECURITYSUPPORT_AUTHENTICATIONLEVEL_0("NoSecuritySupport.authenticationlevel.0", "No authentication"),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_10("PasswordWithLevelSecuritySupport.accesslevel.10", "Standard authentication"),
    PASSWORDWITHLEVELSECURITYSUPPORT_ACCESSLEVEL_20("PasswordWithLevelSecuritySupport.accesslevel.20", "Standard encryption"),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_10("PasswordWithUserIdentificationSecuritySupport.accesslevel.10", "Standard authentication"),
    PASSWORDWITHUSERIDENTIFICATIONSECURITYSUPPORT_ACCESSLEVEL_20("PasswordWithUserIdentificationSecuritySupport.accesslevel.20", "Standard encryption"),
    SIMPLEPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0("SimplePasswordSecuritySupport.authenticationlevel.0", "Password authentication"),
    WAVENISSECURITYSUPPORT_AUTHENTICATIONLEVEL_0("WavenisSecuritySupport.authenticationlevel.0", "Wavenis authentication"),
    WAVENISSECURITYSUPPORT_ENCRYPTIONLEVEL_0("WavenisSecuritySupport.encryptionlevel.0", "Standard Wavenis encryption"),
    INHERITED_ACCESSLEVEL("inheritedDeviceAccessLevel", "Inherited access level"),
    GARNET_AUTHENTICATION_LEVEL_0("GarnetSecuritySupport.authenticationlevel.0", "Garnet message authentication"),
    GARNET_ENCRYPTION_LEVEL_1("GarnetSecuritySupport.encryptionlevel.1", "Garnet message encryption"),
    DEVICEDIALHOMEID("deviceDialHomeId", "Device call home ID"),
    TIMEOUT("protocol.timeout", "Timeout"),
    RETRIES("protocol.retries", "Retries"),
    DIALECT_CPS_DOMAIN_NAME(DeviceProtocolDialectPropertyProvider.class.getName(), "Device protocol dialect"),
    SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME(BaseDevice.class.getName(), "Security property set"),
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}
