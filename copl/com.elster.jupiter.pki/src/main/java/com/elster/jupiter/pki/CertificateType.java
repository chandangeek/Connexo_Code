/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import java.util.EnumSet;

public enum CertificateType {
    DIGITAL_SIGNATURE("DigitalSignature", "dlms-signature-", EnumSet.of(KeyUsage.digitalSignature), EnumSet.noneOf(ExtendedKeyUsage.class)),
    KEY_AGREEMENT("KeyAgreement", "dlms-agreement-", EnumSet.of(KeyUsage.keyAgreement), EnumSet.noneOf(ExtendedKeyUsage.class)),
    TLS("TLS", "dlms-tls-", EnumSet.of(KeyUsage.keyAgreement, KeyUsage.digitalSignature), EnumSet.of(ExtendedKeyUsage.tlsWebClientAuthentication, ExtendedKeyUsage.tlsWebServerAuthentication)),
    OTHER("Other", "", EnumSet.noneOf(KeyUsage.class), EnumSet.noneOf(ExtendedKeyUsage.class));

    private String name;
    private String prefix;
    private EnumSet<KeyUsage> keyUsages;
    private EnumSet<ExtendedKeyUsage> extendedKeyUsages;

    CertificateType(String name, String prefix, EnumSet<KeyUsage> keyUsages, EnumSet<ExtendedKeyUsage> extendedKeyUsages) {
        this.name = name;
        this.prefix = prefix;
        this.keyUsages = keyUsages;
        this.extendedKeyUsages = extendedKeyUsages;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isApplicableTo(KeyType keyType){
        return keyType.getKeyUsages().containsAll(keyUsages)
                && keyUsages.containsAll(keyType.getKeyUsages())
                && keyType.getExtendedKeyUsages().containsAll(extendedKeyUsages)
                && extendedKeyUsages.containsAll(keyType.getExtendedKeyUsages());
    }
}
