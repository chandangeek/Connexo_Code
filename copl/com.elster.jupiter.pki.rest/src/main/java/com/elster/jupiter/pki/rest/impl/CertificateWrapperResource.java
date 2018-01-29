/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.AliasParameterFilter;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.IssuerParameterFilter;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsagesParameterFilter;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SubjectParameterFilter;
import com.elster.jupiter.pki.rest.AliasInfo;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Where;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Path("/certificates")
public class CertificateWrapperResource {

    private static final long MAX_FILE_SIZE = 2048;
    private final SecurityManagementService securityManagementService;
    private final CertificateInfoFactory certificateInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DataSearchFilterFactory dataSearchFilterFactory;

    @Inject
    public CertificateWrapperResource(SecurityManagementService securityManagementService, CertificateInfoFactory certificateInfoFactory, ExceptionFactory exceptionFactory, DataSearchFilterFactory dataSearchFilterFactory/*, ConcurrentModificationExceptionFactory conflictFactory, TrustStoreInfoFactory trustStoreInfoFactory, TrustedCertificateInfoFactory trustedCertificateInfoFactory*/) {
        this.securityManagementService = securityManagementService;
        this.certificateInfoFactory = certificateInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.dataSearchFilterFactory = dataSearchFilterFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList getCertificates(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<CertificateWrapperInfo> infoList = findCertficates(jsonQueryFilter)
                .from(queryParameters)
                .stream()
                .map(certificateInfoFactory::asInfo)
                .collect(toList());

        return PagedInfoList.fromPagedList("certificates", infoList, queryParameters);
    }

    private Finder<CertificateWrapper> findCertficates(JsonQueryFilter jsonQueryFilter) {
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

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    @Transactional
    public Response importNewCertificate(
            @FormDataParam("file") InputStream certificateInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("alias") String alias) {
        if (contentDispositionHeader.getSize() > MAX_FILE_SIZE) {
            throw new LocalizedFieldValidationException(MessageSeeds.IMPORTFILE_TOO_BIG, "file");
        }
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
        if (!securityManagementService.getDirectoryCertificateUsagesQuery().select(Where.where("certificate").isEqualTo(certificateWrapper)).isEmpty()) {
            throw exceptionFactory.newException(MessageSeeds.CERTIFICATE_USED_BY_DIRECTORY, certificateId);
        }
        certificateWrapper.delete();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Transactional
    @Path("/{id}/markObsolete")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response markCertificateObsolete(@PathParam("id") long certificateId) {
        CertificateWrapper cert = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));

        //fixme
        List<String> directories = Collections.emptyList();
        List<String> importers = Collections.emptyList();
        List<String> devices = securityManagementService.getCertificateAssociatedDevicesNames(cert);
        List<SecurityAccessor> accessors = securityManagementService.getAssociatedCertificateAccessors(cert);

        if (accessors.isEmpty() && devices.isEmpty() && importers.isEmpty() && directories.isEmpty()) {
            cert.setObsolete(true);
            cert.save();
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.ACCEPTED)
                .entity(certificateInfoFactory.asCertificateUsagesInfo(accessors, devices, directories, importers))
                .build();
    }

    @POST
    @Transactional
    @Path("/{id}/forceMarkObsolete")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response forceMarkCertificateObsolete(@PathParam("id") long certificateId) {
        CertificateWrapper wrapper = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        wrapper.setObsolete(true);
        wrapper.save();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Transactional
    @Path("/{id}/unmarkObsolete")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response unMarkCertificateObsolete(@PathParam("id") long certificateId) {
        CertificateWrapper wrapper = securityManagementService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        wrapper.setObsolete(false);
        wrapper.save();
        return Response.status(Response.Status.OK).build();
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
        if (contentDispositionHeader.getSize() > MAX_FILE_SIZE) {
            throw new LocalizedFieldValidationException(MessageSeeds.IMPORTFILE_TOO_BIG, "file");
        }
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
        KeyType keyType = securityManagementService.getKeyType(csrInfo.keyTypeId)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_KEY_TYPE, "keyType"));
        ClientCertificateWrapper clientCertificateWrapper = securityManagementService.newClientCertificateWrapper(keyType, csrInfo.keyEncryptionMethod).alias(csrInfo.alias).add();
        X500Name x500Name = getX500Name(csrInfo);
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();
        clientCertificateWrapper.generateCSR(x500Name);
        return Response.status(Response.Status.CREATED).entity(certificateInfoFactory.asInfo(clientCertificateWrapper)).build();
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
            certificateWrapper.setCertificate(certificate);
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
}
