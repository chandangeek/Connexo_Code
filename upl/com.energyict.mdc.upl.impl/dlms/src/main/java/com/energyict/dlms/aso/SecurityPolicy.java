package com.energyict.dlms.aso;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/02/2016 - 14:38
 */
public class SecurityPolicy {

    private final int dataTransportSecurityLevel;
    private final int suite;

    public SecurityPolicy(int suite, int dataTransportSecurityLevel) {
        this.suite = suite;
        this.dataTransportSecurityLevel = dataTransportSecurityLevel;
    }

    public int getDataTransportSecurityLevel() {
        return dataTransportSecurityLevel;
    }

    //TODO this is suite0, add support for suite 1 and 2

    public boolean isRequestPlain() {
        return dataTransportSecurityLevel == 0;
    }

    public boolean isRequestAuthenticatedOnly() {
        return dataTransportSecurityLevel == 1;
    }

    public boolean isRequestEncryptedOnly() {
        return dataTransportSecurityLevel == 2;
    }

    public boolean isRequestAuthenticatedAndEncrypted() {
        return dataTransportSecurityLevel == 3;
    }

    public boolean isResponsePlain() {
        return dataTransportSecurityLevel == 0;
    }

    public boolean isResponseAuthenticatedOnly() {
        return dataTransportSecurityLevel == 1;
    }

    public boolean isResponseEncryptedOnly() {
        return dataTransportSecurityLevel == 2;
    }

    public boolean isResponseAuthenticatedAndEncrypted() {
        return dataTransportSecurityLevel == 3;
    }
}