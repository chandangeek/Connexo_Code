package com.elster.jupiter.pki;

/**
 * This enum allows identification of the actual element stored in a KeyAccessor.
 *
 */
public enum CryptographicType {
    Certificate(false, MetaType.CERTIFICATE, false),
    ClientCertificate(false, MetaType.CERTIFICATE, true), // will contain PrivateKey as well
    TrustedCertificate(false, MetaType.CERTIFICATE, false),
    SymmetricKey(true, MetaType.KEY, true),
    Passphrase(true, MetaType.KEY, true),
    AsymmetricKey(false, MetaType.KEY, true),
    ;


    CryptographicType(boolean durationMandatory, MetaType metaType, boolean requiresKeyEncryptionMethod) {
        this.durationMandatory = durationMandatory;
        this.metaType = metaType;
        this.requiresKeyEncryptionMethod = requiresKeyEncryptionMethod;
    }

    private final boolean requiresKeyEncryptionMethod;
    private boolean durationMandatory;
    private MetaType metaType;

    public boolean requiresDuration() {
        return durationMandatory;
    };

    public boolean requiresKeyEncryptionMethod() {
        return requiresKeyEncryptionMethod;
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public boolean isKey() {
        return MetaType.KEY.equals(metaType);
    }

    public enum MetaType {
        KEY, CERTIFICATE
    }
}
