/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateRequestData;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Where;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import java.security.cert.X509Certificate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class CSRProcessor {
    private static final Pattern FULL_ALIAS_PATTERN = Pattern.compile("^(.+)-[^-]+$");
    private final CSRImporterLogger logger;
    private final SecurityManagementService securityManagementService;
    private final CaService caService;
    private final Map<String, Object> properties;
    private Optional<CertificateRequestData> certificateRequestData;

    @Inject
    CSRProcessor(SecurityManagementService securityManagementService, CaService caService, Map<String, Object> properties, CSRImporterLogger logger, Optional<CertificateRequestData> certificateRequestData) {
        this.securityManagementService = securityManagementService;
        this.caService = caService;
        this.properties = properties;
        this.logger = logger;
        this.certificateRequestData = certificateRequestData;
    }

    public Map<String, Map<String, X509Certificate>> process(Map<String, Map<String, PKCS10CertificationRequest>> csrMap) {
        return csrMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, serialAndCertificateMap -> serialAndCertificateMap.getValue().entrySet().stream()
                        .map(fullAliasAndCSR -> Pair.of(fullAliasAndCSR.getKey(), processCSR(serialAndCertificateMap.getKey(), fullAliasAndCSR.getKey(), fullAliasAndCSR.getValue()).orElse(null)))
                        .filter(Pair::hasLast)
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getLast))));
    }

    private Optional<X509Certificate> processCSR(String serial, String fullAlias, PKCS10CertificationRequest csr) {
        logger.log(MessageSeeds.PROCESSING_CSR, serial, fullAlias);

        Matcher matcher = FULL_ALIAS_PATTERN.matcher(fullAlias);
        if (!matcher.matches()) {
            throw new CSRImporterException(logger.getThesaurus(), MessageSeeds.WRONG_FILE_NAME_FORMAT);
        }
        String prefix = matcher.group(1);
        String alias = serial + '-' + prefix;
        if (certificateRequestData.isPresent()) {
            certificateRequestData.get().setPrefix(prefix);
        }
        return Optional.of(processCSR(alias, csr));
    }

    /**
     * Call the EJBCA web-service to do the actual signing. Method implements 2 variants:
     * - the CSR and the resulting certificate are saved in Connexo
     * - just sign and return
     *
     * The second option is preferred in production, since the CSRs are signed before the devices are defined in Connexo
     * (while devices are still in production), so if we already create the certificates, we'll need to manually link them
     * to devices after those are are created via shipment file.
     *
     * The normal flow is to sign the CSRs (without saving anything in Connexo), then import the devices via shipment file,
     * then import the certificates via Certificate Importer - which will automatically create the certificate and link them
     * to the appropiate security accessor.
     *
     * @param alias
     * @param csr
     * @return
     */
    private X509Certificate processCSR(String alias, PKCS10CertificationRequest csr) {
        if (saveCertificateInConnexo()) {
            Optional<CertificateWrapper> certificateWrapperOptional = securityManagementService.findCertificateWrapper(alias);
            certificateWrapperOptional
                    .filter(this::isInUse)
                    .ifPresent(certificateWrapper -> {
                        throw new CSRImporterException(logger.getThesaurus(), MessageSeeds.CSR_IS_IN_USE, alias);
                    });
            CertificateWrapper wrapper = certificateWrapperOptional
                    .orElseGet(() -> securityManagementService.newCertificateWrapper(alias));
            if (!(wrapper instanceof RequestableCertificateWrapper)) {
                throw new IllegalStateException("For some reason trusted certificate is found instead of a requestable one.");
            }
            RequestableCertificateWrapper csrWrapper = (RequestableCertificateWrapper) wrapper;
            // TODO: what the heck is with this method? should key usages be taken from CSR? or does indication of key usages here override them?
            csrWrapper.setCSR(csr, EnumSet.noneOf(KeyUsage.class), EnumSet.noneOf(ExtendedKeyUsage.class));
            csrWrapper.save();
            logger.log(MessageSeeds.CSR_IMPORTED_SUCCESSFULLY, alias);

            X509Certificate certificate = signCsr(csr, alias);
            logger.log(MessageSeeds.CSR_SIGNED_SUCCESSFULLY, alias);

            csrWrapper.setCertificate(certificate, certificateRequestData);
            csrWrapper.setWrapperStatus(CertificateWrapperStatus.NATIVE);
            csrWrapper.save();
            logger.log(MessageSeeds.CERTIFICATE_IMPORTED_SUCCESSFULLY, alias);
            return certificate;
        } else {
            X509Certificate certificate = signCsr(csr, alias);
            logger.log(MessageSeeds.CSR_SIGNED_SUCCESSFULLY, alias);
            return certificate;
        }
    }

    private boolean saveCertificateInConnexo() {
        Object propertyValue = properties.get(CSRImporterTranslatedProperty.SAVE_CERTIFICATE.getPropertyKey());
        if (Objects.nonNull(propertyValue)){
            return (boolean) propertyValue;
        }
        return false;
    }

    private boolean isInUse(CertificateWrapper certificateWrapper) {
        return securityManagementService.isUsedByCertificateAccessors(certificateWrapper)
                || securityManagementService.streamDirectoryCertificateUsages()
                .anyMatch(Where.where("certificate").isEqualTo(certificateWrapper))
                || !securityManagementService.getCertificateAssociatedDevicesNames(certificateWrapper).isEmpty();
    }

    private X509Certificate signCsr(PKCS10CertificationRequest csr, String alias) {
        TimeDuration timeout = (TimeDuration) properties.get(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey());
        // TODO: move to RequestableCertificateWrapper?
        try {
            return CompletableFuture.supplyAsync(() -> {
                return caService.signCsr(csr, certificateRequestData);
            }, Executors.newSingleThreadExecutor())
                    .get(timeout.getMilliSeconds(), TimeUnit.MILLISECONDS);
        } catch (CompletionException | InterruptedException | TimeoutException e) {
            throw new CSRImporterException(logger.getThesaurus(), MessageSeeds.SIGN_CSR_BY_CA_TIMED_OUT, alias);
        } catch (ExecutionException e) {
            throw new CSRImporterException(logger.getThesaurus(), MessageSeeds.SIGN_CSR_BY_CA_FAILED, e, alias, e.getCause().getLocalizedMessage());
        }
    }
}
