/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.crlrequest;

import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;

import sun.security.x509.X509CRLImpl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrlRequestTaskExecutor implements TaskExecutor {
    private volatile CaService caService;
    private volatile CrlRequestTaskService crlRequestTaskService;
    private final Logger logger;

    public CrlRequestTaskExecutor(CaService caService, CrlRequestTaskService crlRequestTaskService) {
        this.caService = caService;
        this.crlRequestTaskService = crlRequestTaskService;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskService.findCrlRequestTaskProperties();
        String caName = crlRequestTaskProperty.getCaName();
        SecurityAccessor securityAccessor = crlRequestTaskProperty.getSecurityAccessor();
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
        if (x509CRL.isPresent()) {
            X509CRL crl = x509CRL.get();
            if (publicKey != null) {
                try {
                    crl.verify(publicKey);
                } catch (CRLException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                    logger.log(Level.SEVERE, e.getMessage());
                }
            }
            Set<X509CRLEntry> x509CRLEntries = ((X509CRLImpl) crl).getRevokedCertificates();
            for (X509CRLEntry x509CRLEntry : x509CRLEntries) {
                logger.log(Level.INFO, x509CRLEntry.getSerialNumber().toString());
            }

        }


    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {

    }
}
