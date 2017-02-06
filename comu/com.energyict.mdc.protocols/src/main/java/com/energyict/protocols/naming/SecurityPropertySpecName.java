/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.naming;

import com.elster.jupiter.nls.TranslationKey;

public enum SecurityPropertySpecName implements TranslationKey {

    PASSWORD("Password", "Password"),
    ENCRYPTION_KEY("EncryptionKey", "Encryption key"),
    ENCRYPTION_KEY_WITH_KEY_ACCESSOR("EncryptionKeyWithKeyAccessor", "Encryption key"),
    MASTER_KEY("MasterKey", "Master key"),
    ENCRYPTION_KEY_1("EncryptionKey1", "Encryption key"),
    ENCRYPTION_KEY_2("EncryptionKey2", "Encryption key"),
    ENCRYPTION_KEY_3("EncryptionKey3", "Encryption key"),
    AUTHENTICATION_KEY("AuthenticationKey", "Authentication key"),
    AUTHENTICATION_KEY_WITH_KEY_ACCESSOR("AuthenticationKeyWithKeyAccessor", "Authentication key"),
    CLIENT_MAC_ADDRESS("ClientMacAddress", "Client mac address"),
    /**
     * A UserName or a UserIdentification
     */
    DEVICE_ACCESS_IDENTIFIER("DeviceAccessIdentifier", "Device access identifier"),
    ANSI_C12_USER("C12User", "C12 User"),
    ANSI_C12_USER_ID("C12UserId", "C12 User id"),
    /**
     * Indicates whether the password should be represented as ASCII or as Binary
     */
    BINARY_PASSWORD("PasswordBinary", "Binary password"),
    ANSI_CALLED_AP_TITLE("AnsiCalledAPTitle"),

    /* Below are incremental properties for the different clients of DLMSSecuritySuportPerClient */
    PASSWORD_PUBLIC("PasswordPublic", "Public password"),
    PASSWORD_DATA("PasswordData", "Data password"),
    PASSWORD_EXT_DATA("PasswordExtData", "Extra data password"),
    PASSWORD_MANAGEMENT("PasswordManagement", "Management client password"),
    PASSWORD_FIRMWARE("PasswordFirmware", "Firmware client password"),
    PASSWORD_MANUFACTURER("PasswordManufacturer", "Password client password"),

    ENCRYPTION_KEY_PUBLIC("EncryptionKeyPublic", "Public encryption key"),
    ENCRYPTION_KEY_DATA("EncryptionKeyData", "Data encryption key"),
    ENCRYPTION_KEY_EXT_DATA("EncryptionKeyExtData", "Extra data encryption key"),
    ENCRYPTION_KEY_MANAGEMENT("EncryptionKeyManagement", "Management client encryption key"),
    ENCRYPTION_KEY_FIRMWARE("EncryptionKeyFirmware", "Firmware client encryption key"),
    ENCRYPTION_KEY_CUSTOMER("EncryptionKeyCustomer", "Customer encryption key"),
    ENCRYPTION_KEY_MANUFACTURER("EncryptionKeyManufacturer", "Manufacturer client encryption key"),

    AUTHENTICATION_KEY_PUBLIC("AuthenticationKeyPublic", "public authentication key"),
    AUTHENTICATION_KEY_DATA("AuthenticationKeyData", "Data authentication key"),
    AUTHENTICATION_KEY_EXT_DATA("AuthenticationKeyExtData", "Extra dta authentication key"),
    AUTHENTICATION_KEY_MANAGEMENT("AuthenticationKeyManagement", "Management client authentication key"),
    AUTHENTICATION_KEY_FIRMWARE("AuthenticationKeyFirmware", "Firmware client authentication key"),
    AUTHENTICATION_KEY_MANUFACTURER("AuthenticationKeyManufacturer", "Manufacturer authentication key"),

    SERVER_SIGNING_CERTIFICATE("ServerSigningCertificate", "Server signing request"),
    SERVER_KEY_AGREEMENT_CERTIFICATE("ServerKeyAgreementCertificate", "Server key agreement certificate"),
    ;

    private final String key;
    private final String defaultFormat;

    SecurityPropertySpecName(String key) {
        this(key, key);
    }

    SecurityPropertySpecName(String key, String defaultFormat) {
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