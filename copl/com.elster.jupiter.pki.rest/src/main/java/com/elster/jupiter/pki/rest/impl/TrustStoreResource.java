/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

@Path("/truststores")
public class TrustStoreResource {

    public static final int MAX_FILE_SIZE = 250 * 1024;
    private final PkiService pkiService;
    private final TrustStoreInfoFactory trustStoreInfoFactory;
    private final CertificateInfoFactory certificateInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public TrustStoreResource(PkiService pkiService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, TrustStoreInfoFactory trustStoreInfoFactory, CertificateInfoFactory certificateInfoFactory) {
        this.pkiService = pkiService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.trustStoreInfoFactory = trustStoreInfoFactory;
        this.certificateInfoFactory = certificateInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getTrustStores(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("trustStores", trustStoreInfoFactory.asInfoList(this.pkiService.getAllTrustStores()), queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public TrustStoreInfo getTrustStore(@PathParam("id") long id) {
        TrustStore trustStore = findTrustStoreOrThrowException(id);
        return trustStoreInfoFactory.asInfo(trustStore);
    }

    @GET
    @Path("/{id}/certificates")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCertificates(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        TrustStore trustStore = findTrustStoreOrThrowException(id);
        return asPagedInfoList(certificateInfoFactory.asInfo(trustStore.getCertificates()), "certificates", queryParameters);
    }

    @POST
    @Path("/{id}/certificates/single")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Response importSingleCertificate(
            @PathParam("id") long trustStoreId,
            @FormDataParam("file") InputStream certificateInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("alias") String alias) {
        try {
            if (contentDispositionHeader.getSize() > MAX_FILE_SIZE) {
                throw new LocalizedFieldValidationException(MessageSeeds.FILE_TOO_BIG, "file");
            }
            TrustStore trustStore = findTrustStoreOrThrowException(trustStoreId);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);
            if (certificate==null) {
                throw new LocalizedFieldValidationException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE, "file");
            }
            trustStore.addCertificate(alias, certificate);
            return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
        } catch (CertificateException e) {
            throw new LocalizedFieldValidationException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE, "file", e);
        } catch (NoSuchProviderException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE_FACTORY, e);
        }
    }

    @POST
    @Path("/{id}/certificates/keystore")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Response importKeyStore(
            @PathParam("id") long trustStoreId,
            @FormDataParam("file") InputStream keyStoreInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("password") String password) {
        try {
            if (contentDispositionHeader.getSize() > MAX_FILE_SIZE) {
                throw new LocalizedFieldValidationException(MessageSeeds.FILE_TOO_BIG, "file");
            }
            TrustStore trustStore = findTrustStoreOrThrowException(trustStoreId);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreInputStream, password.toCharArray());
            trustStore.loadKeyStore(keyStore);
            return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(MessageSeeds.COULD_NOT_READ_KEYSTORE, "file", e);
        }
    }

    @POST
    @Transactional
    @Path("/{id}/validateKeyStoreFile")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public Response validateKeyStoreFile(TrustStoreInfo info) {
        if (info.keyStoreFileSize != null && info.keyStoreFileSize.intValue() > MAX_FILE_SIZE) {
            throw new LocalizedFieldValidationException(MessageSeeds.FILE_TOO_BIG, "keyStoreFile");
        }
        return Response.ok().build();
    }


    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public TrustStoreInfo addTrustStore(TrustStoreInfo info) {
        PkiService.TrustStoreBuilder builder = pkiService.newTrustStore(info.name);
        if (info.description != null) {
            builder = builder.description(info.description);
        }
        TrustStore trustStore = builder.add();
        return trustStoreInfoFactory.asInfo(trustStore);
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response editTrustStore(@PathParam("id") long id, TrustStoreInfo info) {
        TrustStore trustStore = pkiService.findAndLockTrustStoreByIdAndVersion(id, info.version)
            .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                    .withActualVersion(() -> getCurrentTrustStoreVersion(info.id))
                    .supplier());
        trustStore.setName(info.name);
        trustStore.setDescription(info.description);
        trustStore.save();
        return Response.ok(trustStoreInfoFactory.asInfo(trustStore)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response deleteTrustStore(@PathParam("id") long id) {
        findTrustStoreOrThrowException(id).delete();
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}/certificates/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response removeTrustedCertificate(@PathParam("id") long trustStoreId, @PathParam("certificateId") long certificateId) {
        CertificateWrapper certificateWrapper = pkiService.findCertificateWrapper(certificateId)
            .orElseThrow( exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE, certificateId) );
        if ( ((TrustedCertificate)certificateWrapper).getTrustStore().getId() != trustStoreId ) {
            throw exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE_IN_STORE, certificateId, trustStoreId).get();
        }
        certificateWrapper.delete();
        return Response.status(Response.Status.OK).build();
    }

    private Long getCurrentTrustStoreVersion(long id) {
        return pkiService.findTrustStore(id).map(TrustStore::getVersion).orElse(null);
    }

    private PagedInfoList asPagedInfoList(List<CertificateWrapperInfo> certificateWrapperInfos, String rootKeyName, JsonQueryParameters queryParameters) {
        List<CertificateWrapperInfo> pagedInfos = ListPager.of(certificateWrapperInfos).from(queryParameters).find();
        return PagedInfoList.fromPagedList(rootKeyName, pagedInfos, queryParameters);
    }

    private TrustStore findTrustStoreOrThrowException(@PathParam("id") long id) {
        return this.pkiService.findTrustStore(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_TRUSTSTORE));
    }


}
