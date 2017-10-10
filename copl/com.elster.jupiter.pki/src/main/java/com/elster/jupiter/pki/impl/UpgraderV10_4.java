package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.security.cert.CertificateParsingException;
import java.util.stream.Collectors;

public class UpgraderV10_4 implements Upgrader {
    private final DataModel dataModel;
    private final PkiService pkiService;
    private final Thesaurus thesaurus;

    @Inject
    UpgraderV10_4(DataModel dataModel, PkiService pkiService, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.pkiService = pkiService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4));

        updateCertificates();
        updateTrustedCertificates();
    }

    private void updateCertificates() {
        pkiService.findAllCertificates().find().stream()
                .map(x -> {
                    RequestableCertificateWrapper csr = (RequestableCertificateWrapper) x;
                    if (csr.hasCSR()) {
                        csr.getCSR().ifPresent(cert -> csr.setSubject(cert.getSubject().toString()));
                    } else {
                        try {
                            csr.getCertificate().ifPresent(cert -> csr.setSubject(cert.getSubjectDN().toString()));
                            csr.getCertificate().ifPresent(cert -> csr.setIssuer(cert.getIssuerDN().toString()));
                            csr.setKeyUsagesCsv(csr.stringifyKeyUsages(csr.getKeyUsages()));
                            csr.setExtendedKeyUsagesCsv(csr.stringifyExtendedKeyUsages(csr.getExtendedKeyUsages()));
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
        pkiService.getAllTrustStores().stream()
                .map(x -> {
                    for (TrustedCertificate trustedCertificate : x.getCertificates()) {
                        try {
                            trustedCertificate.getCertificate().ifPresent(cert -> trustedCertificate.setSubject(cert.getSubjectDN().toString()));
                            trustedCertificate.getCertificate().ifPresent(cert -> trustedCertificate.setIssuer(cert.getIssuerDN().toString()));
                            trustedCertificate.setKeyUsagesCsv(trustedCertificate.stringifyKeyUsages(trustedCertificate.getKeyUsages()));
                            trustedCertificate.setExtendedKeyUsagesCsv(trustedCertificate.stringifyExtendedKeyUsages(trustedCertificate.getExtendedKeyUsages()));
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
