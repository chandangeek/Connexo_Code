/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.cert.X509Certificate;
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
        info.id = certificateWrapper.getId();
        info.hasCSR = certificateWrapper.hasCSR();
        info.hasCertificate = certificateWrapper.getCertificate().isPresent();
        info.hasPrivateKey = certificateWrapper.hasPrivateKey();

        info.alias = certificateWrapper.getAlias();
        info.status = certificateWrapper.getStatus();
        info.expirationDate = certificateWrapper.getExpirationTime().orElse(null);

        certificateWrapper.getAllKeyUsages().ifPresent(keyUsages -> info.type = keyUsages);
        if (certificateWrapper.getCertificate().isPresent()) {
            X509Certificate x509Certificate = certificateWrapper.getCertificate().get();
            info.issuer = x509Certificate.getIssuerDN().getName();
            info.subject = x509Certificate.getSubjectDN().getName();
            info.version = x509Certificate.getVersion();
            info.serialNumber = x509Certificate.getSerialNumber();
            info.notBefore = x509Certificate.getNotBefore().toInstant();
            info.notAfter = x509Certificate.getNotAfter().toInstant();
            info.signatureAlgorithm = x509Certificate.getSigAlgName();
        } else if (certificateWrapper.hasCSR()) {
            PKCS10CertificationRequest csr = ((ClientCertificateWrapper) certificateWrapper).getCSR().get();
            info.subject = csr.getSubject().toString();
            info.signatureAlgorithm = csr.getSignatureAlgorithm().getAlgorithm().toString();
        }

        return info;
    }

}
