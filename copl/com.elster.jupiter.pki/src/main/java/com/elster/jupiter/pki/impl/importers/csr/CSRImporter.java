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

    CSRImporter(Map<String, Object> properties,
                Thesaurus thesaurus,
                SecurityManagementService securityManagementService,
                CaService caService,
                FtpClientService ftpClientService) {
        this.thesaurus = thesaurus;
        this.properties = properties;
        this.securityManagementService = securityManagementService;
        this.caService = caService;
        this.ftpClientService = ftpClientService;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        try {
            ReusableInputStream reusableInputStream = ReusableInputStream.from(fileImportOccurrence.getContents());
            verifyInputFileSignature(reusableInputStream);
            log(fileImportOccurrence, MessageSeeds.OK_SIGNATURE);

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
                    new CertificateExportProcessor(ftpClientService, properties).processExport(certificateMap);
                }
            } else {
                // TODO error
            }

            markSuccess(fileImportOccurrence);
        } catch (LocalizedException e) {
            log(fileImportOccurrence, e);
            markFailure(fileImportOccurrence);
        } catch (ConstraintViolationException | IOException e) {
            log(fileImportOccurrence, e);
            markFailure(fileImportOccurrence);
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

//    private void logCreation(FileImportOccurrence fileImportOccurrence) {
//        log(fileImportOccurrence, MessageSeeds.CALENDAR_CREATED);
//    }
//
//    private void logUpdate(FileImportOccurrence fileImportOccurrence) {
//        log(fileImportOccurrence, MessageSeeds.CALENDAR_UPDATED);
//    }
//
    private void log(FileImportOccurrence fileImportOccurrence, LocalizedException exception) {
        Throwable cause = exception.getCause();
        if (cause == null) {
            fileImportOccurrence.getLogger().log(exception.getMessageSeed().getLevel(), exception.getLocalizedMessage());
        } else {
            fileImportOccurrence.getLogger().log(exception.getMessageSeed().getLevel(), exception.getLocalizedMessage(), cause);
        }
    }

    private void log(FileImportOccurrence fileImportOccurrence, Throwable exception) {
        log(fileImportOccurrence, MessageSeeds.CSR_IMPORT_EXCEPTION, exception.getLocalizedMessage());
    }

    private void log(FileImportOccurrence fileImportOccurrence, MessageSeeds messageSeeds, Object... args) {
        fileImportOccurrence.getLogger().log(messageSeeds.getLevel(), thesaurus.getFormat(messageSeeds).format(args));
    }

    private void markFailure(FileImportOccurrence fileImportOccurrence) {
        fileImportOccurrence.markFailure(thesaurus.getFormat(TranslationKeys.CSR_IMPORT_FAILED).format());
    }

    private void markSuccess(FileImportOccurrence fileImportOccurrence) {
        fileImportOccurrence.markSuccess(thesaurus.getFormat(TranslationKeys.CSR_IMPORT_SUCCESS).format());
    }
}
