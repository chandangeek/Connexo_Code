package com.elster.jupiter.pki;

/**
 * This enum allows identification of the actual element stored in a KeyAccessor.
 *
 */
public enum CryptographicType {
    Certificate(false),
    ClientCertificate(false),
    TrustedCertificate(false),
    SymmetricKey(true),
    Passphrase(true),
    AsymmetricKey(true),
    ;

    CryptographicType(boolean durationMandatory) {
        this.durationMandatory = durationMandatory;
    }

    private boolean durationMandatory;

    public boolean requiresDuration() {
        return durationMandatory;
    };
}
