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
    AUTHENTICATION_KEY("AuthenticationKey"),
    CLIENT_MAC_ADDRESS("ClientMacAddress"),
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
    ANSI_CALLED_AP_TITLE("AnsiCalledAPTitle");

    private final String name;

    private SecurityPropertySpecName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
