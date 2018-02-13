package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateAuthoritySearchFilter;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.RevokeStatus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;

import org.apache.commons.collections.CollectionUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

@Component(name = "com.elster.jupiter.pki.rest.impl.CertificateRevocationUtils",
        service = {CertificateRevocationUtils.class},
        immediate = true)
public class CertificateRevocationUtils {
    private static final Logger LOGGER = Logger.getLogger(CertificateWrapperResource.class.getName());
    static final String TIMEOUT_MESSAGE = "Interrupted by timeout";

    private SecurityManagementService securityManagementService;
    private ExceptionFactory exceptionFactory;
    private CaService caService;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    //OSGI
    public CertificateRevocationUtils() {
    }

    @Inject
    public CertificateRevocationUtils(SecurityManagementService securityManagementService,
                                      CaService caService,
                                      NlsService nlsService) {
        this();
        setSecurityManagementService(securityManagementService);
        setCaService(caService);
        setNlsService(nlsService);
        activate();
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.exceptionFactory = new ExceptionFactory(nlsService.getThesaurus(PkiApplication.COMPONENT_NAME, Layer.REST));
    }

    @Reference
    public void setCaService(CaService caService) {
        this.caService = caService;
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
    }

    public boolean isCAConfigured() {
        return caService.isConfigured();
    }

    public List<CertificateWrapper> findAllCertificateWrappers(List<Long> certificatesIds) {
        List<Long> missedIds = new ArrayList<>();
        List<CertificateWrapper> certs = certificatesIds.stream()
                .map((id) -> {
                    Optional<CertificateWrapper> wr = securityManagementService.findCertificateWrapper(id);
                    if (!wr.isPresent()) {
                        missedIds.add(id);
                    }
                    return wr;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        if (CollectionUtils.isNotEmpty(missedIds)) {
            throw exceptionFactory.newException(MessageSeeds.NO_CERTIFICATES_WITH_IDS, missedIds);
        }
        return certs;
    }

    public void revokeCertificate(CertificateWrapper certificateWrapper, Long timeout) {
        CertificateWrapper cert;
        try {
            cert = executorService.submit(() -> caSendRevoke(certificateWrapper)).get(timeout, TimeUnit.SECONDS);
            LOGGER.fine("Certificate " + cert.getAlias() + " was revoked by Certification Authority");
        } catch (InterruptedException | ExecutionException e) {
            throw exceptionFactory.newException(MessageSeeds.REVOCATION_FAILED, e.getLocalizedMessage());
        } catch (TimeoutException e) {
            throw exceptionFactory.newException(MessageSeeds.REVOCATION_FAILED, TIMEOUT_MESSAGE);
        }

        try {
            updateCertificateWrapperStatus(cert, CertificateWrapperStatus.REVOKED);
        } catch (Exception e) {
            LocalizedException ex;
            if (caService.isConfigured()) {
                ex = exceptionFactory.newException(MessageSeeds.REVOCATION_DESYNC);
            } else {
                ex = exceptionFactory.newException(MessageSeeds.STATUS_CHANGE_FAILED);
            }
            ex.addSuppressed(e);
            throw ex;
        }
    }

    /**
     * Revokes collection of {@link CertificateWrapper} and changes it status to {@link CertificateWrapperStatus}#REVOKED
     *
     * @param certificateWrappers certificates to revoke
     * @param timeout timeout in seconds
     */
    public CertificateRevocationResultInfo bulkRevokeCertificates(List<CertificateWrapper> certificateWrappers, long timeout) {
        CertificateRevocationResultInfo info = new CertificateRevocationResultInfo();
        Map<CertificateWrapper, Future<CertificateWrapper>> jobs = new HashMap<>();

        certificateWrappers.forEach(cw -> jobs.put(cw, executorService.submit(() -> caSendRevoke(cw))));

        LOGGER.fine("Started " + jobs.size() + " revocation async tasks");

        List<Future> asyncResults = new ArrayList<>();
        List<CertificateWrapper> revokedByCA = new ArrayList<>();

        //wait for results async, interrupt all jobs if/when first timeout is happened
        jobs.entrySet().forEach(entry -> asyncResults.add(executorService.submit(() -> {
            try {
                revokedByCA.add(entry.getValue().get(timeout, TimeUnit.SECONDS));
                info.addResult(entry.getKey().getAlias(), true);
                LOGGER.fine("Certificate " + entry.getKey().getAlias() + " was revoked by Certification Authority");
            } catch (TimeoutException e) {
                LOGGER.warning("Exception occurred during async revocation task: " + e.getLocalizedMessage());
                info.addResult(entry.getKey().getAlias(), false, TIMEOUT_MESSAGE);
                cancelAllFutures(jobs.values());
            } catch (CancellationException e) {
                LOGGER.warning("Exception occurred during async revocation task: " + e.getLocalizedMessage());
                info.addResult(entry.getKey().getAlias(), false, TIMEOUT_MESSAGE);
            } catch (Exception e) {
                LOGGER.warning("Exception occurred during async revocation task: " + e.getLocalizedMessage());
                info.addResult(entry.getKey().getAlias(), false, e.getLocalizedMessage());
            }
        })));

        //we can use some lock here, but plain 'wait until done' is easier
        waitAllAsyncResults(asyncResults);

        //sync updating for certificate wrapper statuses since ORM layer can't handle async tasks properly
        revokedByCA.forEach(cw -> {
            try {
                updateCertificateWrapperStatus(cw, CertificateWrapperStatus.REVOKED);
            } catch (Exception e) {
                LOGGER.warning("Exception occurred while changing certificate status: " + e.getLocalizedMessage());
                if (isCAConfigured()) {
                    info.replaceResult(cw.getAlias(), false, "Certificate was revoked by the Certification Authority, but failed to change it status");
                } else {
                    info.replaceResult(cw.getAlias(), false, "Failed to change certificate wrapper status");
                }
            }
        });
        return info;
    }

    private CertificateWrapper caSendRevoke(CertificateWrapper certificateWrapper) {
        if (!caService.isConfigured()) {
            return certificateWrapper;
        }

        CertificateAuthoritySearchFilter revokeFilter = new CertificateAuthoritySearchFilter(
                certificateWrapper.getCertificate()
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_CERTIFICATE_PRESENT))
                        .getSerialNumber(),
                certificateWrapper.getIssuer(),
                certificateWrapper.getSubject());

        try {
            // front end doesn't specify reason, so hardcoded
            caService.revokeCertificate(revokeFilter, RevokeStatus.REVOCATION_REASON_UNSPECIFIED.getVal());
            RevokeStatus revokeStatus = caService.checkRevocationStatus(revokeFilter);
            if (revokeStatus == null || revokeStatus == RevokeStatus.NOT_REVOKED) {
                throw exceptionFactory.newException(MessageSeeds.REVOCATION_FAILED, "Unexpected revocation status: " + revokeStatus);
            }
            return certificateWrapper;
        } catch (Exception e) {
            LocalizedException ex = exceptionFactory.newException(MessageSeeds.REVOCATION_FAILED, e.getLocalizedMessage());
            ex.addSuppressed(e);
            throw ex;
        }
    }

    private void waitAllAsyncResults(List<Future> asyncResults) {
        asyncResults.forEach(future -> {
            try {
                if (!future.isDone() && !future.isCancelled()) {
                    future.get();
                }
            } catch (Exception e) {
                LOGGER.warning("Exception occurred while awaiting for async results. Ignoring... -> " + e.getLocalizedMessage());
            }
        });
    }

    private void cancelAllFutures(Collection<Future<CertificateWrapper>> futures) {
        futures.forEach(future -> future.cancel(true));
    }

    private void updateCertificateWrapperStatus(CertificateWrapper certificateWrapper, CertificateWrapperStatus status) {
        certificateWrapper.setWrapperStatus(status);
        certificateWrapper.save();
    }
}
