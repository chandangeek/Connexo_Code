/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.rest.util.ExceptionFactory;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CertificateInfoFactory {

    private final Map<String, Integer> rdsOrder = new HashMap<>();
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CertificateInfoFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
        rdsOrder.put("CN", 1);
        rdsOrder.put("OU", 2);
        rdsOrder.put("O", 3);
        rdsOrder.put("L", 4);
        rdsOrder.put("ST", 5);
        rdsOrder.put("C", 6);
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

        info.alias = certificateWrapper.getAlias();
        info.status = certificateWrapper.getStatus();
        info.expirationDate = certificateWrapper.getExpirationTime().orElse(null);

        if (ClientCertificateWrapper.class.isAssignableFrom(certificateWrapper.getClass())) {
            ClientCertificateWrapper clientCertificateWrapper = (ClientCertificateWrapper) certificateWrapper;
            info.keyEncryptionMethod = clientCertificateWrapper.getPrivateKeyWrapper().getKeyEncryptionMethod();
        }

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

        return info;
    }

    String x500FormattedName(String x500Name) {
        try {
            return new LdapName(x500Name)
                    .getRdns()
                    .stream()
                    .sorted(Comparator.comparing(rdn -> rdsOrder.getOrDefault(rdn.getType(), 7)))
                    .map(Rdn::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .map(X500Principal::new)
                    .map(p -> p.getName(X500Principal.RFC1779))
                    .get();
        } catch (InvalidNameException e) {
            throw exceptionFactory.newException(MessageSeeds.INVALID_DN);
        }
    }

}
