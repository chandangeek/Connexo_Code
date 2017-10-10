package com.elster.jupiter.pki.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.pki.impl.wrappers.certificate.ClientCertificateWrapperImpl;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                        } catch (CertificateParsingException e) {
                            throw new PkiLocalizedException(thesaurus, MessageSeeds.COULD_NOT_READ_KEY_USAGES);
                        }
                    }
                    return x;
                })
                .forEach(certificate -> dataModel.update(certificate));

        pkiService.getAllTrustStores().stream()
                .map(x -> x.getCertificates())
                .flatMap(y -> y.stream())
                .forEach(z -> {
                    X509Certificate cert = z.getCertificate().get();
                    System.out.println(z.getCertificate().get().getSubjectDN());
                    System.out.println(z.getCertificate().get().getIssuerDN());
//                    z.getKeyUsages()
//                    z.getExtendedKeyUsages()

                });


        // TODO get the damn trust storese
    }


}
