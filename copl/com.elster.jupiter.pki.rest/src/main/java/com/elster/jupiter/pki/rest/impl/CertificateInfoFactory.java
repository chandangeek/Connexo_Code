/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class CertificateInfoFactory {

    public List<CertificateInfo> asInfo(List<? extends CertificateWrapper> certificates) {
        return certificates.stream()
                .map(this::asInfo)
                .sorted((c1, c2) -> c1.alias.compareToIgnoreCase(c2.alias))
                .collect(Collectors.toList());
    }

    public CertificateInfo asInfo(CertificateWrapper certificateWrapper) {
        CertificateInfo info = new CertificateInfo();
        info.alias = certificateWrapper.getAlias();
        info.status = certificateWrapper.getStatus();
        info.expirationDate = certificateWrapper.getExpirationTime().orElse(null);
        certificateWrapper.getAllKeyUsages().ifPresent(keyUsages -> info.type = keyUsages);
        certificateWrapper.getCertificate().ifPresent(x509Certificate -> info.issuer = x509Certificate.getIssuerDN().getName());
        certificateWrapper.getCertificate().ifPresent(x509Certificate -> info.subject = x509Certificate.getSubjectDN().getName());
        certificateWrapper.getCertificate().ifPresent(x509Certificate -> info.version = x509Certificate.getVersion());
        certificateWrapper.getCertificate().ifPresent(x509Certificate -> info.serialNumber = x509Certificate.getSerialNumber());
        certificateWrapper.getCertificate().ifPresent(x509Certificate -> info.notBefore = x509Certificate.getNotBefore().toInstant());
        certificateWrapper.getCertificate().ifPresent(x509Certificate -> info.notAfter = x509Certificate.getNotAfter().toInstant());
        certificateWrapper.getCertificate().ifPresent(x509Certificate -> info.signatureAlgorithm = x509Certificate.getSigAlgName());

        return info;
    }

}
