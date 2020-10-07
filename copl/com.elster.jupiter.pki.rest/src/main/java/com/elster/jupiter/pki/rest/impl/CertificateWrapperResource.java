/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.hsm.utils.krypto.CSRGenerator;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.AliasParameterFilter;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateRequestData;
import com.elster.jupiter.pki.CertificateStatus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.IssuerParameterFilter;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsagesParameterFilter;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SubjectParameterFilter;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.DataVaultPrivateKeyFactory;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.HsmPrivateKeyFactory;
import com.elster.jupiter.pki.rest.AliasInfo;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Where;


import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Path("/certificates")
public class CertificateWrapperResource {
    private static final Logger LOGGER = Logger.getLogger(CertificateWrapperResource.class.getName());
    private static final long DEFAULT_TIMEOUT = 30000;

    private final SecurityManagementService securityManagementService;
    private final CaService caService;
    private final CertificateInfoFactory certificateInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DataSearchFilterFactory dataSearchFilterFactory;
    private final CertificateRevocationUtils revocationUtils;
    private final Thesaurus thesaurus;

    @Inject
    public CertificateWrapperResource(SecurityManagementService securityManagementService,
                                      CaService caService,
                                      CertificateInfoFactory certificateInfoFactory,
                                      ExceptionFactory exceptionFactory,
                                      DataSearchFilterFactory dataSearchFilterFactory,
                                      CertificateRevocationUtils certificateRevocationUtils,
                                      Thesaurus thesaurus) {
        this.securityManagementService = securityManagementService;
        this.caService = caService;
        this.certificateInfoFactory = certificateInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.dataSearchFilterFactory = dataSearchFilterFactory;
        this.revocationUtils = certificateRevocationUtils;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList getCertificates(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<CertificateWrapperInfo> infoList = (Checks.is(queryParameters.getLike()).empty() ?
                findCertificates(jsonQueryFilter) :
                securityManagementService.findCertificatesByFilter(dataSearchFilterFactory.asLikeFilter(queryParameters.getLike())))
                .from(queryParameters)
                .stream()
                .filter(wr -> statusFilter(wr, jsonQueryFilter))
                .map(certificateInfoFactory::asInfo)
                .collect(toList());
        return PagedInfoList.fromPagedList("certificates", infoList, queryParameters);
    }

    /**
     * Specific custom filter for certificate statuses
     * Status is not a DB stored property. Certificate status depends on several sources (e.g. actual X509Certificate state or extra obsolete flag)
     */
    static boolean statusFilter(CertificateWrapper wr, JsonQueryFilter jsonQueryFilter) {
        List<String> statuses = jsonQueryFilter.getStringList("status");
        return CollectionUtils.isEmpty(statuses) || wr.getCertificateStatus().map(CertificateStatus::getName).filter(statuses::contains).isPresent();
    }

    private Finder<CertificateWrapper> findCertificates(JsonQueryFilter jsonQueryFilter) {
        if (jsonQueryFilter.hasFilters()) {
            SecurityManagementService.DataSearchFilter dataSearchFilter = getDataSearchFilter(jsonQueryFilter);
            return securityManagementService.findCertificatesByFilter(dataSearchFilter);
        } else {
            return securityManagementService.findAllCertificates();
        }
    }

    private SecurityManagementService.DataSearchFilter getDataSearchFilter(JsonQueryFilter jsonQueryFilter) {
        return dataSearchFilterFactory.asFilter(jsonQueryFilter, Optional.empty());
    }

    @GET
    @Path("/aliases")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList aliasSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<AliasInfo> collect = securityManagementService.getAliasesByFilter(new AliasParameterFilter(securityManagementService, jsonQueryFilter))
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getAlias)
                .map(AliasInfo::new)
                .collect(toList());
        return PagedInfoList.fromPagedList("aliases", collect, queryParameters);
    }

    @GET
    @Path("/subjects")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList subjectSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<SubjectInfo> collect = securityManagementService.getSubjectsByFilter(new SubjectParameterFilter(securityManagementService, jsonQueryFilter))
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getSubject)
                .distinct()
                .map(SubjectInfo::new)
                .collect(toList());
        return PagedInfoList.fromPagedList("subjects", collect, queryParameters);
    }

    @GET
    @Path("/issuers")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList issuerSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<IssuerInfo> collect = securityManagementService.getIssuersByFilter(new IssuerParameterFilter(securityManagementService, jsonQueryFilter))
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getIssuer)
                .distinct()
                .map(IssuerInfo::new)
                .collect(toList());
        return PagedInfoList.fromPagedList("issuers", collect, queryParameters);
    }

    @GET
    @Path("/keyusages")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList keyUsagesSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        KeyUsagesParameterFilter filter = new KeyUsagesParameterFilter(securityManagementService, jsonQueryFilter);
        List<KeyUsageInfo> infos = securityManagementService.getKeyUsagesByFilter(filter)
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getStringifiedKeyUsages)
                .map(x -> filterKeyUsagesbySearchParam().apply(x, filter.searchValue))
                .flatMap(Collection::stream)
                .distinct()
                .map(KeyUsageInfo::new)
                .collect(toList());

        return PagedInfoList.fromPagedList("keyUsages", infos, queryParameters);
    }

    @GET
    @Path("/statuses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList statusSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> statuses = Stream.of(CertificateStatus.values())
                .map(st -> new IdWithNameInfo(st.getName(), st.getDisplayName(thesaurus)))
                .collect(toList());
        return PagedInfoList.fromPagedList("statuses", statuses, queryParameters);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    @Transactional
    public Response importNewCertificate(
            @FormDataParam("file") InputStream certificateInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("alias") String alias) {
        CertificateWrapper certificateWrapper = securityManagementService.newCertificateWrapper(alias);
        doImportCertificateForCertificateWrapper(certificateInputStream, certificateWrapper);
        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public CertificateWrapperInfo getCertificate(@PathParam("id") long certificateId) {
        CertificateWrapper certificateWrapper = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));

        return certificateInfoFactory.asInfo(certificateWrapper);
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response removeCertificate(@PathParam("id") long certificateId) {
        CertificateWrapper certificateWrapper = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        certificateWrapper.delete();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Transactional
    @Path("/{id}/requestCertificate")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response requestCertificate(@PathParam("id") long certificateId, @QueryParam("timeout") long timeout) {
        RequestableCertificateWrapper certificateWrapper = securityManagementService.findCertificateWrapper(certificateId)
                .filter(RequestableCertificateWrapper.class::isInstance)
                .map(RequestableCertificateWrapper.class::cast)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        PKCS10CertificationRequest pkcs10CertificationRequest = certificateWrapper.getCSR().get();
        timeout = timeout == 0 ? 30 : timeout;
        try {
            X509Certificate certificate = signCertificateAsync(pkcs10CertificationRequest, certificateWrapper.getCertificateRequestData()).get(timeout, TimeUnit.SECONDS);
            try {
                certificateWrapper.setCertificate(certificate, Optional.empty());
                certificateWrapper.save();
            } catch (Exception e) {
                throw exceptionFactory.newException(MessageSeeds.COULD_NOT_SAVE_CERTIFICATE_FROM_CA, e.getLocalizedMessage());
            }
        } catch (CompletionException | InterruptedException | TimeoutException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_RECIEVE_CERTIFICATE_TIMEOUT);
        } catch (ExecutionException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_RECIEVE_CERTIFICATE_FROM_CA, e.getCause().getLocalizedMessage());
        }
        return Response.status(Response.Status.OK).build();
    }

    private CompletableFuture<X509Certificate> signCertificateAsync(PKCS10CertificationRequest pkcs10CertificationRequest, Optional<CertificateRequestData> certificateRequestData) {
        return CompletableFuture.supplyAsync(() -> caService.signCsr(pkcs10CertificationRequest, certificateRequestData), Executors.newSingleThreadExecutor());
    }

    @POST
    @Transactional
    @Path("/{id}/markObsolete")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response markCertificateObsolete(@PathParam("id") long certificateId) {
        CertificateWrapper cert = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));

        CertificateUsagesInfo certificateUsages = findCertificateUsages(cert);
        if (certificateUsages.isUsed) {
            return Response.status(Response.Status.ACCEPTED).entity(certificateUsages).build();
        }
        cert.setWrapperStatus(CertificateWrapperStatus.OBSOLETE);
        cert.save();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Transactional
    @Path("/{id}/forceMarkObsolete")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response forceMarkCertificateObsolete(@PathParam("id") long certificateId) {
        CertificateWrapper cert = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        cert.setWrapperStatus(CertificateWrapperStatus.OBSOLETE);
        cert.save();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Transactional
    @Path("/{id}/unmarkObsolete")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response unMarkCertificateObsolete(@PathParam("id") long certificateId) {
        CertificateWrapper cert = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        cert.setWrapperStatus(CertificateWrapperStatus.NATIVE);
        cert.save();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/{id}/checkRevoke")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response checkRevokeCertificate(@PathParam("id") long certificateId) {
        CertificateWrapper cert = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));

        CertificateUsagesInfo certificateUsages = findCertificateUsages(cert);
        if (certificateUsages.isUsed) {
            return Response.status(Response.Status.ACCEPTED).entity(certificateUsages).build();
        }

        return Response.status(Response.Status.OK)
                .entity(new CertificateRevocationInfo(revocationUtils.isCAConfigured()))
                .build();
    }

    @POST
    @Path("/{id}/revoke")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response revokeCertificate(@PathParam("id") long certificateId, @QueryParam("timeout") long timeout) {
        CertificateWrapper cert = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));

        //should never happen, but lets leave it here since force revocation (e.g. manual via rest client) can surely break something
        if (findCertificateUsages(cert).isUsed) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Revocation called with certificate usages").build();
        }
        revocationUtils.revokeCertificate(cert, timeout == 0 ? DEFAULT_TIMEOUT : timeout);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/checkBulkRevoke")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response checkBulkRevokeCertificate(CertificateRevocationInfo revocationInfo) {
        List<CertificateWrapper> certificates = revocationUtils.findAllCertificateWrappers(revocationInfo.bulk.certificatesIds);
        revocationInfo.isOnline = revocationUtils.isCAConfigured();
        certificates.forEach(cert -> {
            if (isRequestedOrRevoked(cert)) {
                revocationInfo.addWithWrongStatus(cert);
            } else if (findCertificateUsages(cert).isUsed) {
                revocationInfo.addWithUsages(cert);
            }
        });
        //do math on backend
        revocationInfo.updateCounters();
        return Response.status(Response.Status.OK).entity(revocationInfo).build();
    }

    @POST
    @Path("/bulkRevoke")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response bulkRevokeCertificate(CertificateRevocationInfo revocationInfo) {
        List<CertificateWrapper> certificates = revocationUtils.findAllCertificateWrappers(revocationInfo.bulk.certificatesIds);

        List<CertificateWrapper> toRevoke = new ArrayList<>();
        List<CertificateWrapper> withUsages = new ArrayList<>();
        List<CertificateWrapper> withWrongStatus = new ArrayList<>();

        certificates.forEach(certificate -> {
            if (isRequestedOrRevoked(certificate)) {
                withWrongStatus.add(certificate);
            } else if (findCertificateUsages(certificate).isUsed) {
                withUsages.add(certificate);
            } else {
                toRevoke.add(certificate);
            }
        });
        long timeout = revocationInfo.timeout == null || revocationInfo.timeout == 0 ? DEFAULT_TIMEOUT : revocationInfo.timeout;
        CertificateRevocationResultInfo resultInfo = revocationUtils.bulkRevokeCertificates(toRevoke, timeout);
        withUsages.forEach(resultInfo::addWithUsages);
        withWrongStatus.forEach(resultInfo::addWithWrongStatus);
        resultInfo.updateCounters(certificates.size());

        return Response.status(Response.Status.OK).entity(resultInfo).build();
    }

    private static boolean isRequestedOrRevoked(CertificateWrapper cert) {
        return cert.getCertificateStatus()
                .filter(status -> status == CertificateStatus.REQUESTED || status == CertificateStatus.REVOKED)
                .isPresent();
    }

    @POST // This should be PUT but has to be POST due to some 3th party issue
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    @Path("/{id}")
    @Transactional
    public Response importCertificateIntoExistingWrapper(
            @PathParam("id") long certificateWrapperId,
            @FormDataParam("file") InputStream certificateInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("version") long version) {
        if (contentDispositionHeader == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "file");
        }
        // TODO: why is conflict check implemented only here? A lot of methods above can change certificate wrapper.
        CertificateWrapper certificateWrapper = securityManagementService.findAndLockCertificateWrapper(certificateWrapperId, version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        doImportCertificateForCertificateWrapper(certificateInputStream, certificateWrapper);
        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    @POST // This should be PUT but has to be POST due to some 3th party issue
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    @Path("/csr")
    @Transactional
    public Response createCertificateWrapperWithKeysAndCSR(CsrInfo csrInfo) {
        checkMandatoryFields(csrInfo);
        KeyType keyType = securityManagementService.getKeyType(csrInfo.keyTypeId)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_KEY_TYPE, "keyType"));
        switch (csrInfo.keyEncryptionMethod) {
            case HsmPrivateKeyFactory.KEY_ENCRYPTION_METHOD:
                return createRequestableCertificateWrapper(csrInfo, keyType);
            case DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD:
                return createClientCertificateWrapper(csrInfo, keyType);
            default:
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CRYPTOGRAPHIC_TYPE);
        }
    }

    private void checkMandatoryFields(CsrInfo csrInfo) {
        if (csrInfo.keyTypeId == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyTypeId");
        }
        if (csrInfo.alias == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "alias");
        }
        if (csrInfo.keyEncryptionMethod == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyEncryptionMethod");
        }
        if (csrInfo.CN == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "CN");
        }
    }

    private Response createClientCertificateWrapper(CsrInfo csrInfo, KeyType keyType) {
        ClientCertificateWrapper clientCertificateWrapper = securityManagementService.newClientCertificateWrapper(keyType, csrInfo.keyEncryptionMethod).alias(csrInfo.alias).add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();
        clientCertificateWrapper.setCertificateRequestData(csrInfo.getCertificateRequestData());
        X500Name x500Name = getX500Name(csrInfo);
        clientCertificateWrapper.generateCSR(x500Name);
        return Response.status(Response.Status.CREATED).entity(certificateInfoFactory.asInfo(clientCertificateWrapper)).build();
    }

    private Response createRequestableCertificateWrapper(CsrInfo csrInfo, KeyType keyType) {
        X500Name x500Name = getX500Name(csrInfo);
        try {
            LOGGER.info("Requesting CSR signature for HSM label " + csrInfo.alias);
            PKCS10CertificationRequest csr = new PKCS10CertificationRequest(new CSRGenerator().generate(csrInfo.alias, x500Name.toString()));
            LOGGER.info("CSR obtained for subject: " + csr.getSubject().toString());
            RequestableCertificateWrapper requestableCertificateWrapper =
                    securityManagementService.newCertificateWrapper(csrInfo.alias);
            requestableCertificateWrapper.setCSR(csr, keyType.getKeyUsages(), keyType.getExtendedKeyUsages());
            requestableCertificateWrapper.setCertificateRequestData(csrInfo.getCertificateRequestData());
            requestableCertificateWrapper.save();
            LOGGER.info("CSR saved");
            return Response.status(Response.Status.CREATED).entity(certificateInfoFactory.asInfo(requestableCertificateWrapper)).build();
        } catch (Exception e) {
            LOGGER.info(MessageSeeds.NOT_POSSIBLE_TO_CREATE_CSR.getKey() + " " + e);
            throw exceptionFactory.newException(MessageSeeds.NOT_POSSIBLE_TO_CREATE_CSR, e);
        }
    }

    @GET
    @Path("{id}/download/csr")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response downloadCsr(@PathParam("id") long certificateId) {
        CertificateWrapper certificateWrapper = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        if (!certificateWrapper.hasCSR()) {
            throw exceptionFactory.newException(MessageSeeds.NO_CSR_PRESENT);
        }
        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = ((RequestableCertificateWrapper) certificateWrapper).getCSR()
                    .get();
            byte[] encoded = pkcs10CertificationRequest.getEncoded();
            StreamingOutput streamingOutput = output -> {
                output.write(encoded);
                output.flush();
            };
            return Response
                    .ok(streamingOutput, MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + certificateWrapper.getAlias().replaceAll("[^a-zA-Z0-9-_]", "") + ".csr")
                    .build();
        } catch (IOException e) {
            throw exceptionFactory.newException(MessageSeeds.FAILED_TO_READ_CSR, e);
        }
    }

    @GET
    @Path("{id}/download/certificate")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response downloadCertificate(@PathParam("id") long certificateId) {
        CertificateWrapper certificateWrapper = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
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

    @GET
    @Path("ejbca/endentities")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response getEJBCAEndEntity() {
        return Response.ok(new Options(caService.getEndEntities())).build();
    }

    @GET
    @Path("ejbca/caname/{endentityId}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response getEJBCACaName(@PathParam("endentityId") int endentityId) {
        return Response.ok(new Options(caService.getCaName(endentityId))).build();
    }

    @GET
    @Path("ejbca/certificateprofile/{endentityId}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response getEJBCACertProfile(@PathParam("endentityId") int endentityId) {
        return Response.ok(new Options(caService.getCertificateProfile(endentityId))).build();
    }


    private X500Name getX500Name(CsrInfo csrInfo) {
        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        getX500FieldFromInfo(x500NameBuilder, BCStyle.CN, csrInfo.CN);
        getX500FieldFromInfo(x500NameBuilder, BCStyle.C, csrInfo.C);
        getX500FieldFromInfo(x500NameBuilder, BCStyle.L, csrInfo.L);
        getX500FieldFromInfo(x500NameBuilder, BCStyle.OU, csrInfo.OU);
        getX500FieldFromInfo(x500NameBuilder, BCStyle.ST, csrInfo.ST);
        getX500FieldFromInfo(x500NameBuilder, BCStyle.O, csrInfo.O);
        return x500NameBuilder.build();
    }

    private void getX500FieldFromInfo(X500NameBuilder x500NameBuilder, ASN1ObjectIdentifier asn1ObjectIdentifier, String field) {
        if (!Checks.is(field).emptyOrOnlyWhiteSpace()) {
            x500NameBuilder.addRDN(asn1ObjectIdentifier, field);
        }
    }

    private void doImportCertificateForCertificateWrapper(InputStream certificateInputStream, CertificateWrapper certificateWrapper) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);
            if (certificate == null) {
                throw new LocalizedFieldValidationException(MessageSeeds.NOT_A_VALID_CERTIFICATE, "file");
            }
            certificateWrapper.setCertificate(certificate, Optional.empty());
            certificateWrapper.save();
        } catch (CertificateException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE, e);
        } catch (NoSuchProviderException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE_FACTORY, e);
        }
    }

    private BiFunction<Optional<String>, String, List<String>> filterKeyUsagesbySearchParam() {
        return (Optional<String> usages, String searchParam) -> {
            if (usages.isPresent()) {
                return Stream.of(usages.get().split(","))
                        .filter(x -> x.toLowerCase().trim().contains(searchParam.
                                replace("*", "")
                                .replace("?", "")))
                        .collect(toList());

            } else {
                return Collections.emptyList();
            }
        };
    }

    private List<DirectoryCertificateUsage> findDirectoryCertificateUsages(CertificateWrapper cert) {
        return securityManagementService.streamDirectoryCertificateUsages()
                .filter(Where.where("certificate").isEqualTo(cert))
                .select();
    }

    private CertificateUsagesInfo findCertificateUsages(CertificateWrapper certificateWrapper) {
        List<String> devicesNames = securityManagementService.getCertificateAssociatedDevicesNames(certificateWrapper);
        List<DirectoryCertificateUsage> directoryUsages = findDirectoryCertificateUsages(certificateWrapper);
        List<SecurityAccessor> accessors = securityManagementService.getAssociatedCertificateAccessors(certificateWrapper);
        return certificateInfoFactory.asCertificateUsagesInfo(accessors, devicesNames, directoryUsages);
    }
}
