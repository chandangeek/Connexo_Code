/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Path("/certificates")
public class CertificateResource {

    private final PkiService pkiService;
//    private final TrustStoreInfoFactory trustStoreInfoFactory;
//    private final TrustedCertificateInfoFactory trustedCertificateInfoFactory;
    private final ExceptionFactory exceptionFactory;
//    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public CertificateResource(PkiService pkiService, ExceptionFactory exceptionFactory/*, ConcurrentModificationExceptionFactory conflictFactory, TrustStoreInfoFactory trustStoreInfoFactory, TrustedCertificateInfoFactory trustedCertificateInfoFactory*/) {
        this.pkiService = pkiService;
        this.exceptionFactory = exceptionFactory;
//        this.conflictFactory = conflictFactory;
//        this.trustStoreInfoFactory = trustStoreInfoFactory;
//        this.trustedCertificateInfoFactory = trustedCertificateInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public CertificateInfos getCertificates(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {

        // FAKE
        CertificateInfos infos = new CertificateInfos();
        CertificateInfo info = new CertificateInfo();
        info.alias = "Whatever";
        info.expirationDate = Instant.now().plus(5, ChronoUnit.DAYS);
        infos.certificates.add(info);
        infos.total = 1;
        return infos;
    }
}
