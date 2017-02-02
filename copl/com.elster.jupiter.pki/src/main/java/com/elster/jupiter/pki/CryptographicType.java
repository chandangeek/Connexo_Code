package com.elster.jupiter.pki;

/**
 * Created by bvn on 1/18/17.
 */
public enum CryptographicType {
    Certificate(false),
    CertificateWithPrivateKey(false),
    SymmetricKey(true),
    Passphrase(true),
    AsymmetricKey(true);

    CryptographicType(boolean durationMandatory) {
        this.durationMandatory = durationMandatory;
    }

    private boolean durationMandatory;

    public boolean requiresDuration() {
        return durationMandatory;
    };
}
