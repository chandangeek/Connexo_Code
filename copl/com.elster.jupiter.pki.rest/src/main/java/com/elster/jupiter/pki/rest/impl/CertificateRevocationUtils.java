package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateAuthoritySearchFilter;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.RevokeStatus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;

import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

public class CertificateRevocationUtils {
    private static final Logger LOGGER = Logger.getLogger(CertificateWrapperResource.class.getName());

    private final SecurityManagementService securityManagementService;
    private final ExceptionFactory exceptionFactory;
    private final CaService caService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Inject
    public CertificateRevocationUtils(SecurityManagementService securityManagementService,
                                      ExceptionFactory exceptionFactory,
                                      CaService caService) {
        this.securityManagementService = securityManagementService;
        this.exceptionFactory = exceptionFactory;
        this.caService = caService;
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

    public void revokeCertificate(CertificateWrapper certificateWrapper) {
        CertificateWrapper cert = caSendRevoke(certificateWrapper);
        try {
            updateCertificateWrapperStatus(cert, CertificateWrapperStatus.REVOKED);
        } catch (Exception e) {
            LocalizedException ex = exceptionFactory.newException(MessageSeeds.REVOCATION_DESYNC);
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

        List<Future> asyncResults = new ArrayList<>();
        List<CertificateWrapper> revokedByCA = new ArrayList<>();

        //wait for results async, interrupt all jobs if/when first timeout is happened
        jobs.entrySet().forEach(entry -> asyncResults.add(executorService.submit(() -> {
            try {
                revokedByCA.add(entry.getValue().get(timeout, TimeUnit.SECONDS));
                info.addResult(entry.getKey().getAlias(), true);
            } catch (TimeoutException e) {
                info.addResult(entry.getKey().getAlias(), false, "Interrupted by timeout");
                cancelAllFutures(jobs.values());
            } catch (CancellationException e) {
                info.addResult(entry.getKey().getAlias(), false, "Interrupted by timeout");
            } catch (Exception e) {
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
                info.replaceResult(cw.getAlias(), false, "Certificate was revoked by the Certification Authority, but failed to change status within Connexo");
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
