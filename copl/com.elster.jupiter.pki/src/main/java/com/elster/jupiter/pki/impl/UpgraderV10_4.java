package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.security.cert.CertificateParsingException;
import java.util.stream.Collectors;

public class UpgraderV10_4 implements Upgrader {
    private final SecurityManagementService securityManagementService;

    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    @Inject
    UpgraderV10_4(DataModel dataModel, SecurityManagementService securityManagementService, Thesaurus thesaurus) {
        super();
        this.securityManagementService = securityManagementService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4));

        updateCertificates();
        updateTrustedCertificates();
    }

    private void updateCertificates() {
        securityManagementService.findAllCertificates().find().stream()
                .map(x -> {
                    RequestableCertificateWrapper csr = (RequestableCertificateWrapper) x;
                    if (csr.hasCSR()) {
                        csr.getCSR().ifPresent(cert -> csr.setSubject(cert.getSubject().toString()));
                    } else {
                        try {
                            csr.getCertificate().ifPresent(cert -> csr.setSubject(cert.getSubjectDN().toString()));
                            csr.getCertificate().ifPresent(cert -> csr.setIssuer(cert.getIssuerDN().toString()));
                            csr.setKeyUsagesCsv(csr.stringifyKeyUsages(csr.getKeyUsages(), csr.getExtendedKeyUsages()));
                            csr.save();
                        } catch (CertificateParsingException e) {
                            throw new PkiLocalizedException(thesaurus, MessageSeeds.COULD_NOT_READ_KEY_USAGES);
                        }
                    }
                    return x;
                })
                .collect(Collectors.toList());
    }


    private void updateTrustedCertificates() {
        securityManagementService.getAllTrustStores().stream()
                .map(x -> {
                    for (TrustedCertificate trustedCertificate : x.getCertificates()) {
                        try {
                            trustedCertificate.getCertificate().ifPresent(cert -> trustedCertificate.setSubject(cert.getSubjectDN().toString()));
                            trustedCertificate.getCertificate().ifPresent(cert -> trustedCertificate.setIssuer(cert.getIssuerDN().toString()));
                            trustedCertificate.setKeyUsagesCsv(trustedCertificate.stringifyKeyUsages(trustedCertificate.getKeyUsages(), trustedCertificate.getExtendedKeyUsages()));
                            trustedCertificate.save();
                        } catch (CertificateParsingException e) {
                            throw new PkiLocalizedException(thesaurus, MessageSeeds.COULD_NOT_READ_KEY_USAGES);
                        }
                    }
                    return x;
                })
                .collect(Collectors.toList());
    }
}
