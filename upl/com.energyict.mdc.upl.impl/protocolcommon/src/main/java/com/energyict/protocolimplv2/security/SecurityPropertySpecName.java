package com.energyict.protocolimplv2.security;

/**
 * Summarizes all the used SecurityProperty names for our protocol packages.
 * This allows proper visible reuse of security property names
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:43
 */
public enum SecurityPropertySpecName {

    PASSWORD("Password"),
    ENCRYPTION_KEY("EncryptionKey"),
    ENCRYPTION_KEY_1("EncryptionKey1"),
    ENCRYPTION_KEY_2("EncryptionKey2"),
    ENCRYPTION_KEY_3("EncryptionKey3"),
    AUTHENTICATION_KEY("AuthenticationKey"),
    MASTER_KEY("MasterKey"),
    CLIENT_MAC_ADDRESS("ClientMacAddress"),
    CRYPTOSERVER_PHASE("CryptoServerPhase"),
    SECURITY_LEVEL("SecurityLevel"),
    /**
     * A UserName or a UserIdentification
     */
    DEVICE_ACCESS_IDENTIFIER("DeviceAccessIdentifier"),
    ANSI_C12_USER("C12User"),
    ANSI_C12_USER_ID("C12UserId"),
    /**
     * Indicates whether the password should be represented as ASCII or as Binary
     */
    BINARY_PASSWORD("PasswordBinary"),
    ANSI_CALLED_AP_TITLE("CalledAPTitle"),
    ANSI_SECURITY_KEY("SecurityKey"),

    /* Below are incremental properties for the different clients of DLMSSecuritySuportPerClient */
    PASSWORD_PUBLIC("PasswordPublic"),
    PASSWORD_DATA("PasswordData"),
    PASSWORD_EXT_DATA("PasswordExtData"),
    PASSWORD_MANAGEMENT("PasswordManagement"),
    PASSWORD_FIRMWARE("PasswordFirmware"),
    PASSWORD_MANUFACTURER("PasswordManufacturer"),

    ENCRYPTION_KEY_PUBLIC("EncryptionKeyPublic"),
    ENCRYPTION_KEY_DATA("EncryptionKeyData"),
    ENCRYPTION_KEY_EXT_DATA("EncryptionKeyExtData"),
    ENCRYPTION_KEY_MANAGEMENT("EncryptionKeyManagement"),
    ENCRYPTION_KEY_FIRMWARE("EncryptionKeyFirmware"),
    ENCRYPTION_KEY_CUSTOMER("EncryptionKeyCustomer"),
    ENCRYPTION_KEY_MANUFACTURER("EncryptionKeyManufacturer"),

    AUTHENTICATION_KEY_PUBLIC("AuthenticationKeyPublic"),
    AUTHENTICATION_KEY_DATA("AuthenticationKeyData"),
    AUTHENTICATION_KEY_EXT_DATA("AuthenticationKeyExtData"),
    AUTHENTICATION_KEY_MANAGEMENT("AuthenticationKeyManagement"),
    AUTHENTICATION_KEY_FIRMWARE("AuthenticationKeyFirmware"),
    AUTHENTICATION_KEY_MANUFACTURER("AuthenticationKeyManufacturer");

    private final String name;

    private SecurityPropertySpecName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
