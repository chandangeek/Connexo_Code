package com.elster.jupiter.pki;

/**
 * This enum allows identification of the actual element stored in a KeyAccessor.
 *
 */
public enum CryptographicType {
    Certificate(false, MetaType.CERTIFICATE),
    ClientCertificate(false, MetaType.CERTIFICATE),
    TrustedCertificate(false, MetaType.CERTIFICATE),
    SymmetricKey(true, MetaType.KEY),
    Passphrase(true, MetaType.KEY),
    AsymmetricKey(false, MetaType.KEY),
    ;

    CryptographicType(boolean durationMandatory, MetaType metaType) {
        this.durationMandatory = durationMandatory;
        this.metaType = metaType;
    }

    private boolean durationMandatory;
    private MetaType metaType;

    public boolean requiresDuration() {
        return durationMandatory;
    };

    public boolean isKey() {
        return MetaType.KEY.equals(metaType);
    }

    enum MetaType {
        KEY, CERTIFICATE
    }
}
