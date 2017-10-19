/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.*;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.rest.util.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchProviderException;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/truststores")
public class TrustStoreResource {

    public static final int MAX_FILE_SIZE = 250 * 1024;
    private final PkiService pkiService;
    private final TrustStoreInfoFactory trustStoreInfoFactory;
    private final CertificateInfoFactory certificateInfoFactory;
    private final DataSearchFilterFactory dataSearchFilterFactory;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public TrustStoreResource(PkiService pkiService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, TrustStoreInfoFactory trustStoreInfoFactory, CertificateInfoFactory certificateInfoFactory, DataSearchFilterFactory dataSearchFilterFactory) {
        this.pkiService = pkiService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.trustStoreInfoFactory = trustStoreInfoFactory;
        this.certificateInfoFactory = certificateInfoFactory;
        this.dataSearchFilterFactory = dataSearchFilterFactory;
    }

    private static List<KeyUsage> applyKeyUsages(TrustedCertificate x) {
        return x.getKeyUsages().stream().collect(toList());
    }

    private static List<ExtendedKeyUsage> applyExtendedKeyUsages(TrustedCertificate x) {
        try {
            return x.getExtendedKeyUsages().stream().collect(toList());
        } catch (CertificateParsingException e) {
            throw new LocalizedFieldValidationException(MessageSeeds.CERTIFICATE_ENCODING_EXCEPTION, "file");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public PagedInfoList getTrustStores(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("trustStores", trustStoreInfoFactory.asInfoList(this.pkiService.getAllTrustStores()), queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public TrustStoreInfo getTrustStore(@PathParam("id") long id) {
        TrustStore trustStore = findTrustStoreOrThrowException(id);
        return trustStoreInfoFactory.asInfo(trustStore);
    }

    @GET
    @Path("/{id}/certificates")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public PagedInfoList getCertificates(@PathParam("id") long id, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        TrustStore trustStore = findTrustStoreOrThrowException(id);
        List<? extends CertificateWrapper> certificates;
        if (jsonQueryFilter.hasFilters()) {
            PkiService.DataSearchFilter dataSearchFilter = getDataSearchFilter(jsonQueryFilter,id);
            certificates = pkiService.findTrustedCertificatesByFilter(dataSearchFilter);
        } else {
            certificates = trustStore.getCertificates();
        }
        return asPagedInfoList(certificateInfoFactory.asInfo(certificates), "certificates", queryParameters);
    }

    private PkiService.DataSearchFilter getDataSearchFilter(JsonQueryFilter jsonQueryFilter,long trustStoreId) {
        return dataSearchFilterFactory.asFilter(jsonQueryFilter,pkiService.findTrustStore(trustStoreId));
    }

    @GET
    @Path("/{id}/certificates/aliases")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public PagedInfoList getCertificateAliases(@PathParam("id") long id, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        TrustStore trustStore = findTrustStoreOrThrowException(id);
        List<AliasInfo> collect = trustStore.getCertificates().stream()
                .map(cert -> cert.getAlias())
                .map(AliasInfo::new)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("aliases", collect, queryParameters);
    }

    @GET
    @Path("/{id}/certificates/subjects")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public PagedInfoList getCertificateSubjects(@PathParam("id") long id, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        TrustStore trustStore = findTrustStoreOrThrowException(id);
        List<SubjectInfo> collect = trustStore.getCertificates().stream()
                .map(cert -> cert.getSubject())
                .distinct()
                .map(SubjectInfo::new)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("subjects", collect, queryParameters);
    }

    @GET
    @Path("/{id}/certificates/issuers")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public PagedInfoList getCertificateIssuers(@PathParam("id") long id, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        TrustStore trustStore = findTrustStoreOrThrowException(id);
        List<IssuerInfo> collect = trustStore.getCertificates().stream()
                .map(cert -> cert.getIssuer())
                .distinct()
                .map(IssuerInfo::new)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("issuers", collect, queryParameters);
    }

    @GET
    @Path("/{id}/certificates/keyusages")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public PagedInfoList getCertificateKeyUsages(@PathParam("id") long id, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        TrustStore trustStore = findTrustStoreOrThrowException(id);
        List<KeyUsageInfo> collect = trustStore.getCertificates().stream()
                .map(TrustStoreResource::applyKeyUsages)
                .flatMap(Collection::stream)
                .distinct()
                .map((KeyUsage keyUsage) -> new KeyUsageInfo(keyUsage.name()))
                .collect(toList());

        return PagedInfoList.fromPagedList("keyUsages", collect, queryParameters);
    }


    @GET
    @Path("{id}/certificates/{certificateId}/download/certificate")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    public Response downloadCertificate(@PathParam("id") long trustStoreId, @PathParam("certificateId") long certificateId) {
        CertificateWrapper certificateWrapper = pkiService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        if (!TrustedCertificate.class.isAssignableFrom(certificateWrapper.getClass()) ||
                ((TrustedCertificate) certificateWrapper).getTrustStore().getId() != trustStoreId) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CERTIFICATE);
        }
        if (!certificateWrapper.getCertificate().isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_CERTIFICATE_PRESENT);
        }
        try {
            byte[] encoded = certificateWrapper.getCertificate().get().getEncoded();
            StreamingOutput streamingOutput = output -> {
                output.write(encoded);
                output.flush();
            };
            return Response
                    .ok(streamingOutput, MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + certificateWrapper.getAlias().replaceAll("[^a-zA-Z0-9-_]", "") + ".cert")
                    .build();
        } catch (CertificateEncodingException e) {
            throw exceptionFactory.newException(MessageSeeds.FAILED_TO_READ_CERTIFICATE, e);
        }
    }


    @POST
    @Path("/{id}/certificates/single")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_TRUST_STORES})
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
            if (certificate == null) {
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_TRUST_STORES})
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
            throw new LocalizedFieldValidationException(MessageSeeds.COULD_NOT_IMPORT_KEYSTORE, "file", e);
        }
    }

    @POST
    @Transactional
    @Path("/{id}/validateKeyStoreFile")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public Response validateKeyStoreFile(TrustStoreInfo info) {
        if (info.keyStoreFileSize != null && info.keyStoreFileSize.intValue() > MAX_FILE_SIZE) {
            throw new LocalizedFieldValidationException(MessageSeeds.FILE_TOO_BIG, "keyStoreFile");
        }
        return Response.ok().build();
    }


    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_TRUST_STORES})
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_TRUST_STORES})
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response deleteTrustStore(@PathParam("id") long id) {
        findTrustStoreOrThrowException(id).delete();
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}/certificates/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_TRUST_STORES})
    public Response removeTrustedCertificate(@PathParam("id") long trustStoreId, @PathParam("certificateId") long certificateId) {
        CertificateWrapper certificateWrapper = pkiService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE, certificateId));
        if (((TrustedCertificate) certificateWrapper).getTrustStore().getId() != trustStoreId) {
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
