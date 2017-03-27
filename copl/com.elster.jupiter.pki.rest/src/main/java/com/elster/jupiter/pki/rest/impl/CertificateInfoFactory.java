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

    public List<CertificateWrapperInfo> asInfo(List<? extends CertificateWrapper> certificates) {
        return certificates.stream()
                .map(this::asInfo)
                .sorted((c1, c2) -> c1.alias.compareToIgnoreCase(c2.alias))
                .collect(Collectors.toList());
    }

    public CertificateWrapperInfo asInfo(CertificateWrapper certificateWrapper) {
        CertificateWrapperInfo info = new CertificateWrapperInfo();
        info.id = certificateWrapper.getId();
        info.hasCSR = certificateWrapper.hasCSR();
        info.hasCertificate = certificateWrapper.getCertificate().isPresent();
        info.hasPrivateKey = certificateWrapper.hasPrivateKey();

        info.alias = certificateWrapper.getAlias();
        info.status = certificateWrapper.getStatus();
        info.expirationDate = certificateWrapper.getExpirationTime().orElse(null);

        if (ClientCertificateWrapper.class.isAssignableFrom(certificateWrapper.getClass())) {
            ClientCertificateWrapper clientCertificateWrapper = (ClientCertificateWrapper) certificateWrapper;
            info.keyEncryptionMethod = clientCertificateWrapper.getPrivateKeyWrapper().getKeyEncryptionMethod();
        }

        if (certificateWrapper.getCertificate().isPresent()) {
            info.certificate = info.new CertificateInfo();
            certificateWrapper.getAllKeyUsages().ifPresent(keyUsages -> info.certificate.type = keyUsages);
            X509Certificate x509Certificate = certificateWrapper.getCertificate().get();
            info.certificate.issuer = x509Certificate.getIssuerDN().getName();
            info.certificate.subject = x509Certificate.getSubjectDN().getName();
            info.certificate.version = x509Certificate.getVersion();
            info.certificate.serialNumber = x509Certificate.getSerialNumber();
            info.certificate.notBefore = x509Certificate.getNotBefore().toInstant();
            info.certificate.notAfter = x509Certificate.getNotAfter().toInstant();
            info.certificate.signatureAlgorithm = x509Certificate.getSigAlgName();
        } else if (certificateWrapper.hasCSR()) {
            info.csr = info.new CertificateInfo();
            PKCS10CertificationRequest csr = ((ClientCertificateWrapper) certificateWrapper).getCSR().get();
            info.csr.subject = csr.getSubject().toString();
//            info.csr.signatureAlgorithm = csr.getSignatureAlgorithm().getAlgorithm().toString();
        }

        return info;
    }

}
