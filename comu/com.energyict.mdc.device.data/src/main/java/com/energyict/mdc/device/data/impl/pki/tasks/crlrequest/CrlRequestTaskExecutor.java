/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.crlrequest;

import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;

import sun.security.x509.X509CRLImpl;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CrlRequestTaskExecutor implements TaskExecutor {
    private volatile CaService caService;
    private volatile CrlRequestTaskService crlRequestTaskService;
    private volatile SecurityManagementService securityManagementService;
    private final Logger logger;

    public CrlRequestTaskExecutor(CaService caService, CrlRequestTaskService crlRequestTaskService, SecurityManagementService securityManagementService) {
        this.caService = caService;
        this.crlRequestTaskService = crlRequestTaskService;
        this.securityManagementService = securityManagementService;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        Optional<CrlRequestTaskProperty> crlRequestTaskProperty = crlRequestTaskService.findCrlRequestTaskProperties();
        if (!crlRequestTaskProperty.isPresent()) {
            logger.log(Level.INFO, "no CRL request task properties ");
            return;
        }
        String caName = crlRequestTaskProperty.get().getCaName();
        SecurityAccessor securityAccessor = crlRequestTaskProperty.get().getSecurityAccessor();
        PublicKey publicKey = null;
        if (securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualValue().get();
            Optional<X509Certificate> x509Certificate = certificateWrapper.getCertificate();
            if (x509Certificate.isPresent()) {
                publicKey = x509Certificate.get().getPublicKey();
                logger.log(Level.INFO, publicKey.toString());
            }
        }
        Optional<X509CRL> x509CRL = caService.getLatestCRL(caName);
        if (!x509CRL.isPresent()) {
            logger.log(Level.INFO, "no CRL from " + caName);
            return;
        }
        X509CRL crl = x509CRL.get();
        if (publicKey != null) {
            try {
                crl.verify(publicKey);
            } catch (CRLException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                logger.log(Level.SEVERE, e.getMessage());
                return;
            }
        }

        List<BigInteger> revokedSerialNumbers = new ArrayList<>();
        Set<X509CRLEntry> x509CRLEntries = ((X509CRLImpl) crl).getRevokedCertificates();
        x509CRLEntries.forEach(x509CRLEntry -> {
            revokedSerialNumbers.add(x509CRLEntry.getSerialNumber());
            logger.log(Level.INFO, x509CRLEntry.getSerialNumber().toString());
        });

        processNonTrustedCertificates(revokedSerialNumbers);
        processTrustedCertificates(revokedSerialNumbers);

    }

    private void processNonTrustedCertificates(List<BigInteger> revokedSerialNumbers) {
        List<CertificateWrapper> certificateWrapperList = securityManagementService.findAllCertificates().find()
                .stream()
                .filter(certificateWrapper -> certificateWrapper.getCertificate().isPresent())
                .collect(Collectors.toList());
        certificateWrapperList.forEach(certificateWrapper -> {
            BigInteger sn = certificateWrapper.getCertificate().get().getSerialNumber();
            boolean found = revokedSerialNumbers.stream().anyMatch(revokedSerialNumber -> revokedSerialNumber.compareTo(sn) == 0);
            if (found) {
                logger.log(Level.INFO, "Changing status to REVOKED for " + sn);
                certificateWrapper.setWrapperStatus(CertificateWrapperStatus.REVOKED);
            }
        });
    }

    private void processTrustedCertificates(List<BigInteger> revokedSerialNumbers) {
        List<TrustedCertificate> trustedCertificatesList = new ArrayList<>();
        securityManagementService.getAllTrustStores().forEach(
                trustStore -> trustedCertificatesList.addAll(trustStore.getCertificates())
        );
        List<TrustedCertificate> certificateWrapperList = trustedCertificatesList
                .stream()
                .filter(trustedCertificate -> trustedCertificate.getCertificate().isPresent())
                .collect(Collectors.toList());

        certificateWrapperList.forEach(trustedCertificate -> {
            BigInteger sn = trustedCertificate.getCertificate().get().getSerialNumber();
            boolean found = revokedSerialNumbers.stream().anyMatch(revokedSerialNumber -> revokedSerialNumber.compareTo(sn) == 0);
            if (found) {
                logger.log(Level.INFO, "Changing status to REVOKED for " + sn);
                trustedCertificate.setWrapperStatus(CertificateWrapperStatus.REVOKED);
            }
        });
    }


    @Override
    public void postExecute(TaskOccurrence occurrence) {

    }
}
