/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import java.security.cert.CertificateParsingException;
import java.util.EnumSet;
import java.util.Set;

public enum CertificateType {
    DIGITAL_SIGNATURE("DigitalSignature", "dlms-signature-", EnumSet.of(KeyUsage.digitalSignature), EnumSet.noneOf(ExtendedKeyUsage.class)),
    KEY_AGREEMENT("KeyAgreement", "dlms-agreement-", EnumSet.of(KeyUsage.keyAgreement), EnumSet.noneOf(ExtendedKeyUsage.class)),
    TLS("TLS", "dlms-tls-", EnumSet.of(KeyUsage.keyAgreement, KeyUsage.digitalSignature), EnumSet.of(ExtendedKeyUsage.tlsWebClientAuthentication, ExtendedKeyUsage.tlsWebServerAuthentication)),
    WEBTLS("WEBTLS", "tls-", EnumSet.of(KeyUsage.keyAgreement, KeyUsage.digitalSignature), EnumSet.of(ExtendedKeyUsage.tlsWebClientAuthentication, ExtendedKeyUsage.tlsWebServerAuthentication)),
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
        EnumSet<KeyUsage> keyUsages = keyType.getKeyUsages();
        EnumSet<ExtendedKeyUsage> extendedKeyUsages = keyType.getExtendedKeyUsages();
        return checkIfApplicable(keyUsages, extendedKeyUsages);
    }

    public boolean isApplicableTo(CertificateWrapper certificateWrapper) throws CertificateParsingException {
        // actually behind the scene it is enumset as well
        Set<KeyUsage> keyUsages = certificateWrapper.getKeyUsages();
        Set<ExtendedKeyUsage> extendedKeyUsages = certificateWrapper.getExtendedKeyUsages();
        return checkIfApplicable(keyUsages, extendedKeyUsages);
    }

    private boolean checkIfApplicable(Set<KeyUsage> keyUsages, Set<ExtendedKeyUsage> extendedKeyUsages) {
        return keyUsages.containsAll(this.keyUsages)
                && this.keyUsages.containsAll(keyUsages)
                && extendedKeyUsages.containsAll(this.extendedKeyUsages)
                && this.extendedKeyUsages.containsAll(extendedKeyUsages);
    }
}
