/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.crlrequest;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.impl.MessageSeeds;

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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CrlRequestTaskExecutor implements TaskExecutor {
    private final CaService caService;
    private final CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    private final SecurityManagementService securityManagementService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final EventService eventService;
    private Logger logger;
    private final static int MAX_MESSAGE_LENGTH = 3997;

    CrlRequestTaskExecutor(CaService caService,
                           CrlRequestTaskPropertiesService crlRequestTaskPropertiesService,
                           SecurityManagementService securityManagementService,
                           DeviceService deviceService,
                           Clock clock,
                           Thesaurus thesaurus,
                           TransactionService transactionService, EventService eventService) {
        this.caService = caService;
        this.crlRequestTaskPropertiesService = crlRequestTaskPropertiesService;
        this.securityManagementService = securityManagementService;
        this.deviceService = deviceService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.eventService = eventService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        // see post-execute
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        logger = Logger.getAnonymousLogger();
        Handler handler = occurrence.createTaskLogHandler().asHandler();
        logger.addHandler(handler);
        try (TransactionContext context = transactionService.getContext()) {
            run(occurrence);
            context.commit();
        } catch (Throwable e) {
            postFailEvent(eventService, occurrence, e.getLocalizedMessage());
            try (TransactionContext context = transactionService.getContext()) {
                log(e);
                context.commit();
            }
            throw e;
        } finally {
            logger.removeHandler(handler);
        }
    }

    private void run(TaskOccurrence occurrence) {
        if (occurrence.getRecurrentTask() == null) {
            throw new CRLRequestTaskException(MessageSeeds.NO_CRL_REQUEST_TASK);
        }
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskPropertiesService.getCrlRequestTaskPropertiesForCa(occurrence.getRecurrentTask())
                .orElseThrow(() -> new CRLRequestTaskException(MessageSeeds.NO_CRL_REQUEST_TASK_PROPERTIES));
        String caName = crlRequestTaskProperty.getCaName();
        if (!caService.getPkiCaNames().contains(caName)) {
            throw new CRLRequestTaskException(MessageSeeds.CA_WITH_NAME_NOT_CONFIGURED, caName);
        }

        CertificateWrapper certificateWrapper = crlRequestTaskProperty.getCRLSigner();
        X509Certificate x509Certificate = certificateWrapper.getCertificate()
                .orElseThrow(() -> new CRLRequestTaskException(MessageSeeds.NO_CERTIFICATE_IN_WRAPPER, certificateWrapper.getAlias()));
        Optional<X509CRL> crlOptional = caService.getLatestCRL(caName);
        if (!crlOptional.isPresent()) {
            log(MessageSeeds.NO_CRL_FROM_CA, caName);
            return;
        }
        X509CRL crl = crlOptional.get();
        if (x509Certificate.getNotAfter().before(Date.from(clock.instant()))) {
            throw new CRLSignatureVerificationFailedException(new CRLRequestTaskException(MessageSeeds.EXPIRED_CERTIFICATE, certificateWrapper.getAlias()));
        }
        PublicKey publicKey = x509Certificate.getPublicKey();
        try {
            crl.verify(publicKey);
        } catch (SignatureException e) {
            throw new CRLSignatureVerificationFailedException(new CRLRequestTaskException(MessageSeeds.SIGNATURE_DOES_NOT_MATCH, e));
        } catch (CRLException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            throw new CRLSignatureVerificationFailedException(e);
        }

        Set<BigInteger> revokedSerialNumbers = new TreeSet<>();
        Set<X509CRLEntry> x509CRLEntries = ((X509CRLImpl) crl).getRevokedCertificates();
        if (x509CRLEntries == null || x509CRLEntries.isEmpty()) {
            log(MessageSeeds.NO_CRL_FROM_CA, caName);
            return;
        }
        x509CRLEntries.forEach(x509CRLEntry -> {
            revokedSerialNumbers.add(x509CRLEntry.getSerialNumber());
        });
        log(MessageSeeds.RECEIVED_CRL_WITH_SN_FROM_CA, caName, revokedSerialNumbers.size(), revokedSerialNumbers.stream()
                .map(BigInteger::toString)
                .collect(Collectors.joining(", ")));
        processCertificates(revokedSerialNumbers);
    }

    private void processCertificates(Set<BigInteger> revokedSerialNumbers) {
        // TODO: implement search by SN for performance purpose?
        Stream.concat(
                securityManagementService.findAllCertificates().find().stream(),
                securityManagementService.getAllTrustStores().stream()
                        .map(TrustStore::getCertificates)
                        .flatMap(List::stream)
        )
                .filter(certificateWrapper -> certificateWrapper.getCertificate()
                        .map(X509Certificate::getSerialNumber)
                        .filter(revokedSerialNumbers::contains)
                        .isPresent())
                .filter(certificateWrapper -> certificateWrapper.getWrapperStatus() != CertificateWrapperStatus.REVOKED)
                .forEach(this::revoke);
    }

    private void revoke(CertificateWrapper certificateWrapper) {
            if (securityManagementService.isUsedByCertificateAccessors(certificateWrapper)) {
                log(MessageSeeds.CERTIFICATE_USED_BY_SECURITY_ACCESSOR, certificateWrapper.getAlias());
            } else if (isCertificateUsedByUserDirectory(certificateWrapper)) {
                log(MessageSeeds.CERTIFICATE_USED_BY_USER_DIRECTORY, certificateWrapper.getAlias());
            } else if (deviceService.usedByKeyAccessor(certificateWrapper)) {
                log(MessageSeeds.CERTIFICATE_USED_BY_DEVICE, certificateWrapper.getAlias());
            } else {
                certificateWrapper.setWrapperStatus(CertificateWrapperStatus.REVOKED);
                certificateWrapper.save();
                log(MessageSeeds.CERTIFICATE_REVOKED_SUCCESSFULLY, certificateWrapper.getAlias());
            }
    }

    private boolean isCertificateUsedByUserDirectory(CertificateWrapper certificateWrapper){
        try(QueryStream<DirectoryCertificateUsage> queryStream = securityManagementService.streamDirectoryCertificateUsages()){
            return queryStream.anyMatch(Where.where("certificate").isEqualTo(certificateWrapper));
        }
    }

    private void log(MessageSeeds messageSeed, Object... args) {
        String message = thesaurus.getSimpleFormat(messageSeed).format(args);
        StringBuilder messageBuilder = new StringBuilder();
        if (message.length() >= MAX_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_MESSAGE_LENGTH);
            messageBuilder.append(message);
            messageBuilder.append("...");
        }
        logger.log(messageSeed.getLevel(), messageBuilder.toString());
    }

    private void log(LocalizedException e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            logger.log(e.getMessageSeed().getLevel(), e.getLocalizedMessage());
        } else {
            logger.log(e.getMessageSeed().getLevel(), e.getLocalizedMessage(), cause);
        }
    }

    private void log(Throwable e) {
        if (e instanceof LocalizedException) {
            log((LocalizedException) e);
        } else {
            logger.log(MessageSeeds.EXCEPTION_FROM_CRL_REQUEST_TASK.getLevel(), thesaurus.getSimpleFormat(MessageSeeds.EXCEPTION_FROM_CRL_REQUEST_TASK).format(e.getLocalizedMessage()), e);
        }
    }

    private class CRLRequestTaskException extends LocalizedException {
        private CRLRequestTaskException(MessageSeeds messageSeed) {
            super(thesaurus, messageSeed);
        }

        private CRLRequestTaskException(MessageSeeds messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

        private CRLRequestTaskException(MessageSeeds messageSeed, Throwable cause, Object... args) {
            super(thesaurus, messageSeed, cause, args);
        }
    }

    private class CRLSignatureVerificationFailedException extends CRLRequestTaskException {
        private CRLSignatureVerificationFailedException(Throwable cause) {
            super(MessageSeeds.CRL_SIGNATURE_VERIFICATION_FAILED, cause, cause.getLocalizedMessage());
        }
    }
}