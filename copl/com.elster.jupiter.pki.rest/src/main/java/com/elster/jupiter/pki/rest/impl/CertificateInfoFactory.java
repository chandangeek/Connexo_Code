/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateFormatter;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.rest.util.ExceptionFactory;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import javax.naming.InvalidNameException;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

public class CertificateInfoFactory implements CertificateFormatter {

    private final ExceptionFactory exceptionFactory;

    @Inject
    public CertificateInfoFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

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
        info.isObsolete = certificateWrapper.isObsolete();

        info.alias = certificateWrapper.getAlias();
        info.status = certificateWrapper.getStatus();
        info.expirationDate = certificateWrapper.getExpirationTime().orElse(null);

        if (ClientCertificateWrapper.class.isAssignableFrom(certificateWrapper.getClass())) {
            ClientCertificateWrapper clientCertificateWrapper = (ClientCertificateWrapper) certificateWrapper;
            info.keyEncryptionMethod = clientCertificateWrapper.getPrivateKeyWrapper().getKeyEncryptionMethod();
        }

        try {
            certificateWrapper.getAllKeyUsages().ifPresent(keyUsages -> info.type = keyUsages);
            if (certificateWrapper.getCertificate().isPresent()) {
                X509Certificate x509Certificate = certificateWrapper.getCertificate().get();
                info.issuer = x500FormattedName(x509Certificate.getIssuerX500Principal().getName(X500Principal.RFC1779));
                info.subject = x500FormattedName(x509Certificate.getSubjectX500Principal().getName(X500Principal.RFC1779));
                info.certificateVersion = x509Certificate.getVersion();
                info.serialNumber = x509Certificate.getSerialNumber();
                info.notBefore = x509Certificate.getNotBefore().toInstant();
                info.notAfter = x509Certificate.getNotAfter().toInstant();
                info.signatureAlgorithm = x509Certificate.getSigAlgName();
            } else if (certificateWrapper.hasCSR()) {
                PKCS10CertificationRequest csr = ((ClientCertificateWrapper) certificateWrapper).getCSR().get();
                info.subject = x500FormattedName(csr.getSubject().toString());
            }
        } catch (InvalidNameException e) {
            throw exceptionFactory.newException(MessageSeeds.INVALID_DN);
        }

        return info;
    }

    public CertificateUsagesInfo asCertificateUsagesInfo(List<SecurityAccessor> accessors, List<String> devices, List<String> directories, List<String> importers) {
        CertificateUsagesInfo info = new CertificateUsagesInfo();
        info.securityAccessors = accessors.stream()
                .limit(3)
                .map(accessor -> accessor.getKeyAccessorType().getName())
                .sorted()
                .collect(Collectors.toList());
        info.devices = devices.stream()
                .limit(3)
                .sorted()
                .collect(Collectors.toList());
        info.userDirectories = directories.stream()
                .limit(3)
                .sorted()
                .collect(Collectors.toList());
        info.importers = importers.stream()
                .limit(3)
                .sorted()
                .collect(Collectors.toList());
        return info;
    }
}
