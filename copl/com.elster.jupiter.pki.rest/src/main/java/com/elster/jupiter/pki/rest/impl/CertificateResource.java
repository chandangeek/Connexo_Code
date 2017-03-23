/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.ExceptionFactory;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response importCertificate(
            @PathParam("id") long trustStoreId,
            @FormDataParam("file") InputStream certificateInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("alias") String alias) {
        try {
            CertificateWrapper certificateWrapper = pkiService.newCertificateWrapper(alias);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);
            certificateWrapper.setCertificate(certificate);
            certificateWrapper.save();
            return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
        } catch (CertificateException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE, e);
        } catch (NoSuchProviderException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE_FACTORY, e);
        }
    }

}
