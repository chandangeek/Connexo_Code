/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.SignatureCheckFailedException;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.util.streams.ReusableInputStream;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Arrays;
import java.util.Map;

class CSRImporter implements FileImporter {
    // TODO: can hardcode?
    public static final int SIGNATURE_LENGTH = 256;
    private final Thesaurus thesaurus;
    private final Map<String, Object> properties;
    private final SecurityManagementService securityManagementService;
    private final CaService caService;
    private final FtpClientService ftpClientService;
    private final Clock clock;

    CSRImporter(Map<String, Object> properties,
                Thesaurus thesaurus,
                SecurityManagementService securityManagementService,
                CaService caService,
                FtpClientService ftpClientService,
                Clock clock) {
        this.thesaurus = thesaurus;
        this.properties = properties;
        this.securityManagementService = securityManagementService;
        this.caService = caService;
        this.ftpClientService = ftpClientService;
        this.clock = clock;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        CSRImporterLogger logger = new CSRImporterLogger(fileImportOccurrence, thesaurus);
        try {
            ReusableInputStream reusableInputStream = ReusableInputStream.from(fileImportOccurrence.getContents());
            verifyInputFileSignature(reusableInputStream);
            logger.log(MessageSeeds.OK_SIGNATURE);

            Map<String, Map<String, PKCS10CertificationRequest>> csrMap = new CSRZipFileParser(thesaurus)
                    .parseInputStream(reusableInputStream.stream());

            Map<String, Map<String, X509Certificate>> certificateMap = new CSRProcessor(securityManagementService, caService, thesaurus)
                    .process(csrMap);

//            processor.addListener(new CSRProcessor.ImportListener() {
//                @Override
//                public void created(String mrid) {
//                    logCreation(fileImportOccurrence);
//                }
//
//                @Override
//                public void updated(String mrid) {
//                    logUpdate(fileImportOccurrence);
//                }
//            });
//
//            processor.process(xmlContents);

            boolean shouldExport = (Boolean) properties.get(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES.getPropertyKey());
            if (csrMap.size() == certificateMap.size()) {
                if (shouldExport) {
                    new CertificateExportProcessor(properties, new CertificateSftpExporterDestination(ftpClientService, clock, properties), logger).processExport(certificateMap);
                }
            } else {
                // TODO error
            }

            logger.markSuccess();
        } catch (LocalizedException e) {
            logger.log( e);
            logger.markFailure();
        } catch (ConstraintViolationException | IOException e) {
            logger.log(e);
            logger.markFailure();
        }
    }

    private void verifyInputFileSignature(ReusableInputStream reusableInputStream) {
        SecurityAccessor<CertificateWrapper> certificateAccessor = (SecurityAccessor<CertificateWrapper>) properties.get(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR.getPropertyKey());
        CertificateWrapper certificateWrapper = certificateAccessor.getActualValue()
                .orElseThrow(() -> new IllegalStateException("There is no active certificate in centrally managed security accessor!"));
        Certificate certificate = certificateWrapper.getCertificate()
                .orElseThrow(() -> new SignatureCheckFailedException(thesaurus, MessageSeeds.NO_CERTIFICATE_IN_WRAPPER, certificateWrapper.getAlias()));
        if (!inputFileHasValidSignature(reusableInputStream.getBytes(), certificate.getPublicKey())) {
            throw new SignatureCheckFailedException(thesaurus);
        }
    }

    private boolean inputFileHasValidSignature(byte[] allBytes, PublicKey publicKey) {
        try {
            final Signature signer = Signature.getInstance("sha" + SIGNATURE_LENGTH + "withrsa");
            signer.initVerify(publicKey);
            signer.update(Arrays.copyOf(allBytes, allBytes.length - SIGNATURE_LENGTH));
            byte[] signature = new byte[SIGNATURE_LENGTH];
            System.arraycopy(allBytes, allBytes.length - SIGNATURE_LENGTH, signature, 0, SIGNATURE_LENGTH);
            return signer.verify(signature);
        } catch (Exception e) {
            throw new SignatureCheckFailedException(thesaurus, e);
        }
    }
}
