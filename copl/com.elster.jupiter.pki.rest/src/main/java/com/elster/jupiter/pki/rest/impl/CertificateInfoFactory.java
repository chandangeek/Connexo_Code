/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.security.auth.x500.X500Principal;
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
        info.version = certificateWrapper.getVersion();
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
            certificateWrapper.getAllKeyUsages().ifPresent(keyUsages -> info.type = keyUsages);
            X509Certificate x509Certificate = certificateWrapper.getCertificate().get();
            info.issuer = x509Certificate.getIssuerX500Principal().getName(X500Principal.RFC1779);
            info.subject = x509Certificate.getSubjectX500Principal().getName(X500Principal.RFC1779);
            info.certificateVersion = x509Certificate.getVersion();
            info.serialNumber = x509Certificate.getSerialNumber();
            info.notBefore = x509Certificate.getNotBefore().toInstant();
            info.notAfter = x509Certificate.getNotAfter().toInstant();
            info.signatureAlgorithm = x509Certificate.getSigAlgName();
        } else if (certificateWrapper.hasCSR()) {
            PKCS10CertificationRequest csr = ((ClientCertificateWrapper) certificateWrapper).getCSR().get();
            info.subject = csr.getSubject().toString();
        }

        return info;
    }

}
