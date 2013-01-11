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
    CLIENT_ID("ClientId");

    private final String name;

    private SecurityPropertySpecName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
