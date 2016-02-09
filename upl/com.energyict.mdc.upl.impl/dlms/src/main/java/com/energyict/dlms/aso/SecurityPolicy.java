package com.energyict.dlms.aso;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/02/2016 - 14:38
 */
public class SecurityPolicy {

    /**
     * For suite 0, the dataTransportSecurityLevel is either 0, 1, 2 or 3.
     * <p/>
     * For suite 1 and 2, the dataTransportSecurityLevel has 6 bits:
     * 0 unused, shall be set to 0,
     * 1 unused, shall be set to 0,
     * 2 authenticated request,
     * 3 encrypted request,
     * 4 digitally signed request,
     * 5 authenticated response,
     * 6 encrypted response,
     * 7 digitally signed response
     */
    private final int dataTransportSecurityLevel;
    private final int suite;

    public SecurityPolicy(int suite, int dataTransportSecurityLevel) {
        this.suite = suite;
        this.dataTransportSecurityLevel = dataTransportSecurityLevel;
    }

    public int getDataTransportSecurityLevel() {
        return dataTransportSecurityLevel;
    }

    public boolean isRequestPlain() {
        return suite == 0 ? (dataTransportSecurityLevel == 0) :
                (!isRequestAuthenticated() && !isRequestEncrypted());
    }

    public boolean isRequestAuthenticatedOnly() {
        return suite == 0 ? (dataTransportSecurityLevel == 1) :
                (isRequestAuthenticated() && !isRequestEncrypted());
    }

    public boolean isRequestEncryptedOnly() {
        return suite == 0 ? (dataTransportSecurityLevel == 2) :
                (!isRequestAuthenticated() && isRequestEncrypted());
    }

    public boolean isRequestAuthenticatedAndEncrypted() {
        return suite == 0 ? (dataTransportSecurityLevel == 3) :
                (isRequestAuthenticated() && isRequestEncrypted());
    }

    public boolean isRequestSigned() {
        return suite != 0 && (isBitSet(SecurityPolicyMapper.REQUESTS_SIGNED_FLAG));
    }

    public boolean isResponsePlain() {
        return suite == 0 ? (dataTransportSecurityLevel == 0) :
                (!isResponseAuthenticated() && !isResponseEncrypted());
    }

    public boolean isResponseAuthenticatedOnly() {
        return suite == 0 ? (dataTransportSecurityLevel == 1) :
                (isResponseAuthenticated() && !isResponseEncrypted());
    }

    public boolean isResponseEncryptedOnly() {
        return suite == 0 ? (dataTransportSecurityLevel == 2) :
                (!isResponseAuthenticated() && isResponseEncrypted());
    }

    public boolean isResponseAuthenticatedAndEncrypted() {
        return suite == 0 ? (dataTransportSecurityLevel == 3) :
                (isResponseAuthenticated() && isResponseEncrypted());
    }

    public boolean isResponseSigned() {
        return suite != 0 && (isBitSet(SecurityPolicyMapper.RESPONSES_SIGNED_FLAG));
    }

    private boolean isRequestAuthenticated() {
        return isBitSet(SecurityPolicyMapper.REQUESTS_AUTHENTICATED_FLAG);
    }

    private boolean isRequestEncrypted() {
        return isBitSet(SecurityPolicyMapper.REQUESTS_ENCRYPTED_FLAG);
    }

    private boolean isResponseAuthenticated() {
        return isBitSet(SecurityPolicyMapper.RESPONSES_AUTHENTICATED_FLAG);
    }

    private boolean isResponseEncrypted() {
        return isBitSet(SecurityPolicyMapper.RESPONSES_ENCRYPTED_FLAG);
    }

    private boolean isBitSet(int bitPosition) {
        return (dataTransportSecurityLevel & (1 << bitPosition)) != 0;
    }
}