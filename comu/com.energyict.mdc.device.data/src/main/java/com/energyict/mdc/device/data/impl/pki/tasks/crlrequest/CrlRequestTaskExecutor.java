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
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;

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
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CrlRequestTaskExecutor implements TaskExecutor {
    private volatile CaService caService;
    private volatile CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceService deviceService;
    private volatile Clock clock;
    private final Logger logger;

    public CrlRequestTaskExecutor(CaService caService, CrlRequestTaskPropertiesService crlRequestTaskPropertiesService, SecurityManagementService securityManagementService, DeviceService deviceService, Clock clock) {
        this.caService = caService;
        this.crlRequestTaskPropertiesService = crlRequestTaskPropertiesService;
        this.securityManagementService = securityManagementService;
        this.deviceService = deviceService;
        this.clock = clock;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        Optional<CrlRequestTaskProperty> crlRequestTaskProperty = crlRequestTaskPropertiesService.findCrlRequestTaskProperties();
        if (!crlRequestTaskProperty.isPresent()) {
            logger.log(Level.WARNING, "No CRL request task properties ");
            return;
        }
        if (!caService.isConfigured()) {
            logger.log(Level.WARNING, "CA service is not configured");
            return;
        }
        String caName = crlRequestTaskProperty.get().getCaName();
        if (!caService.getPkiCaNames().contains(caName)) {
            logger.log(Level.WARNING, "CA " + caName + " is not configured");
            return;
        }
        SecurityAccessor securityAccessor = crlRequestTaskProperty.get().getSecurityAccessor();
        if (securityAccessor == null) {
            logger.log(Level.WARNING, "No security accessor configured");
            return;
        }
        PublicKey publicKey;
        if (securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualValue().get();
            Optional<X509Certificate> x509Certificate = certificateWrapper.getCertificate();
            if (x509Certificate.isPresent()) {
                boolean expired = x509Certificate.get().getNotAfter().before(Date.from(clock.instant()));
                if (expired) {
                    logger.log(Level.WARNING, "Security accessor certificate is expired");
                    return;
                }
                publicKey = x509Certificate.get().getPublicKey();
                logger.log(Level.INFO, "Public key " + publicKey.getAlgorithm());
            } else {
                logger.log(Level.WARNING, "No certificate with public key configured");
                return;
            }
        } else {
            logger.log(Level.WARNING, "Wrong security accessor configured");
            return;
        }
        Optional<X509CRL> x509CRL = caService.getLatestCRL(caName);
        if (!x509CRL.isPresent()) {
            logger.log(Level.WARNING, "No CRL from " + caName);
            return;
        }
        X509CRL crl = x509CRL.get();
        try {
            crl.verify(publicKey);
        } catch (CRLException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return;
        }

        List<BigInteger> revokedSerialNumbers = new ArrayList<>();
        Set<X509CRLEntry> x509CRLEntries = ((X509CRLImpl) crl).getRevokedCertificates();
        x509CRLEntries.forEach(x509CRLEntry -> {
            revokedSerialNumbers.add(x509CRLEntry.getSerialNumber());
        });
        printCrl(revokedSerialNumbers);
        processNonTrustedCertificates(revokedSerialNumbers);
        processTrustedCertificates(revokedSerialNumbers);
    }

    private void processNonTrustedCertificates(List<BigInteger> revokedSerialNumbers) {
        List<CertificateWrapper> certificateWrapperList = securityManagementService.findAllCertificates().find()
                .stream()
                .filter(certificateWrapper -> certificateWrapper.getCertificate().isPresent())
                .filter(certificateWrapper -> certificateWrapper.getWrapperStatus() != CertificateWrapperStatus.REVOKED)
                .collect(Collectors.toList());
        certificateWrapperList.forEach(certificateWrapper -> {
            BigInteger sn = certificateWrapper.getCertificate().get().getSerialNumber();
            boolean toBeRevoked = revokedSerialNumbers.stream().anyMatch(revokedSerialNumber -> revokedSerialNumber.compareTo(sn) == 0);
            if (toBeRevoked) {
                revoke(certificateWrapper);
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
                .filter(trustedCertificate -> trustedCertificate.getWrapperStatus() != CertificateWrapperStatus.REVOKED)
                .collect(Collectors.toList());

        certificateWrapperList.forEach(trustedCertificate -> {
            BigInteger sn = trustedCertificate.getCertificate().get().getSerialNumber();
            boolean toBeRevoked = revokedSerialNumbers.stream().anyMatch(revokedSerialNumber -> revokedSerialNumber.compareTo(sn) == 0);
            if (toBeRevoked) {
                revoke(trustedCertificate);
            }
        });
    }

    private void revoke(CertificateWrapper certificateWrapper) {
        boolean isUsedByCertificateAccessors = securityManagementService.isUsedByCertificateAccessors(certificateWrapper);
        boolean isUsedByDirectory = !securityManagementService.getDirectoryCertificateUsagesQuery().select(Where.where("certificate").isEqualTo(certificateWrapper)).isEmpty();
        boolean isUsedByDevices = usedByDevices(certificateWrapper);
        if (isUsedByCertificateAccessors) {
            logger.log(Level.WARNING, certificateWrapper.getAlias() + " is still used by certificate accessors");
        } else if (isUsedByDirectory) {
            logger.log(Level.WARNING, certificateWrapper.getAlias() + " is still used by user directory");
        } else if (isUsedByDevices) {
            logger.log(Level.WARNING, certificateWrapper.getAlias() + " is still used by devices");
        } else {
            logger.log(Level.INFO, "Changing status to REVOKED for " + certificateWrapper.getAlias());
            certificateWrapper.setWrapperStatus(CertificateWrapperStatus.REVOKED);
        }
    }

    private boolean usedByDevices(CertificateWrapper wrapper) {
        List<SecurityAccessor> securityAccessorList = new ArrayList<>();
        deviceService.findAllDevices(Condition.TRUE).stream().map(device -> securityAccessorList.addAll(device.getSecurityAccessors()));
        List<CertificateWrapper> certificateWrapperList = securityAccessorList
                .stream()
                .filter(securityAccessor -> securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper)
                .filter(securityAccessor -> ((CertificateWrapper) securityAccessor.getActualValue().get()).getCertificate().isPresent())
                .map(securityAccessor -> (CertificateWrapper) securityAccessor.getActualValue().get())
                .collect(Collectors.toList());
        return certificateWrapperList.stream().anyMatch(certificateWrapper -> certificateWrapper.getId() == wrapper.getId());
    }

    private void printCrl(List<BigInteger> revokedSerialNumbers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Received CRL\n");
        revokedSerialNumbers.forEach(sn -> sb.append("sn=").append(sn).append('\n'));
        logger.log(Level.INFO, sb.toString());
    }


    @Override
    public void postExecute(TaskOccurrence occurrence) {
        logger.log(Level.INFO, "CRL request task has been triggered");
    }
}
