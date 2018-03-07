package com.elster.jupiter.pki;

import java.util.EnumSet;

public enum CertificateType {
    DIGITAL_SIGNATURE("DigitalSignature", EnumSet.of(KeyUsage.digitalSignature), EnumSet.noneOf(ExtendedKeyUsage.class)),
    KEY_AGREEMENT("KeyAgreement", EnumSet.of(KeyUsage.keyAgreement), EnumSet.noneOf(ExtendedKeyUsage.class)),
    TLS("TLS", EnumSet.of(KeyUsage.keyAgreement, KeyUsage.digitalSignature), EnumSet.of(ExtendedKeyUsage.tlsWebClientAuthentication, ExtendedKeyUsage.tlsWebServerAuthentication)),
    OTHER("Other",EnumSet.noneOf(KeyUsage.class),EnumSet.noneOf(ExtendedKeyUsage.class));

    private String name;
    private EnumSet<KeyUsage> keyUsages;
    private EnumSet<ExtendedKeyUsage> extendedKeyUsages;

    CertificateType(String name, EnumSet<KeyUsage> keyUsages, EnumSet<ExtendedKeyUsage> extendedKeyUsages) {
        this.name = name;
        this.keyUsages = keyUsages;
        this.extendedKeyUsages = extendedKeyUsages;
    }

    public String getName() {
        return name;
    }

    public boolean isApplicableTo(KeyType keyType){
        return keyType.getKeyUsages().containsAll(keyUsages)
                && keyUsages.containsAll(keyType.getKeyUsages())
                && keyType.getExtendedKeyUsages().containsAll(extendedKeyUsages)
                && extendedKeyUsages.containsAll(keyType.getExtendedKeyUsages());
    }
}
