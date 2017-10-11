/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.*;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.util.Checks;

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
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Path("/certificates")
public class CertificateWrapperResource {

    private static final long MAX_FILE_SIZE = 2048;
    private final PkiService pkiService;
    private final CertificateInfoFactory certificateInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CertificateWrapperResource(PkiService pkiService, CertificateInfoFactory certificateInfoFactory, ExceptionFactory exceptionFactory/*, ConcurrentModificationExceptionFactory conflictFactory, TrustStoreInfoFactory trustStoreInfoFactory, TrustedCertificateInfoFactory trustedCertificateInfoFactory*/) {
        this.pkiService = pkiService;
        this.certificateInfoFactory = certificateInfoFactory;
        this.exceptionFactory = exceptionFactory;
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
            PkiService.DataSearchFilter dataSearchFilter = getDataSearchFilter(jsonQueryFilter);
            return pkiService.findCertificatesByFilter(dataSearchFilter);
        } else {
            return pkiService.findAllCertificates();
        }
    }

    private PkiService.DataSearchFilter getDataSearchFilter(JsonQueryFilter jsonQueryFilter) {
        PkiService.DataSearchFilter dataSearchFilter = new PkiService.DataSearchFilter();

        JsonFilterParametersBean params = new JsonFilterParametersBean(jsonQueryFilter);

        dataSearchFilter.alias = params.getStringList("alias");
        dataSearchFilter.subject = params.getStringList("subject");
        dataSearchFilter.issuer = params.getStringList("issuer");
        dataSearchFilter.keyUsages = params.getStringList("keyUsages");
        dataSearchFilter.extendedKeyUsages = params.getStringList("extendedKeyUsages");
        dataSearchFilter.intervalFrom = params.getInstant("intervalFrom");
        dataSearchFilter.intervalTo = params.getInstant("intervalTo");

        return dataSearchFilter;
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
            throw new LocalizedFieldValidationException(MessageSeeds.CERTIFICATE_TOO_BIG, "file");
        }
        CertificateWrapper certificateWrapper = pkiService.newCertificateWrapper(alias);
        return doImportCertificateForCertificateWrapper(certificateInputStream, certificateWrapper);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public CertificateWrapperInfo getCertificate(@PathParam("id") long certificateId) {
        CertificateWrapper certificateWrapper = pkiService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));

        return certificateInfoFactory.asInfo(certificateWrapper);
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response removeCertificate(@PathParam("id") long certificateId) {
        CertificateWrapper certificateWrapper = pkiService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        certificateWrapper.delete();
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
            throw new LocalizedFieldValidationException(MessageSeeds.CERTIFICATE_TOO_BIG, "file");
        }
        CertificateWrapper certificateWrapper = pkiService.findAndLockCertificateWrapper(certificateWrapperId, version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));
        return doImportCertificateForCertificateWrapper(certificateInputStream, certificateWrapper);
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
        KeyType keyType = pkiService.getKeyType(csrInfo.keyTypeId)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_KEY_TYPE, "keyType"));
        ClientCertificateWrapper clientCertificateWrapper = pkiService.newClientCertificateWrapper(keyType, csrInfo.keyEncryptionMethod).alias(csrInfo.alias).add();
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
        CertificateWrapper certificateWrapper = pkiService.findCertificateWrapper(certificateId)
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
        CertificateWrapper certificateWrapper = pkiService.findCertificateWrapper(certificateId)
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

    private Response doImportCertificateForCertificateWrapper(InputStream certificateInputStream, CertificateWrapper certificateWrapper) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);
            if (certificate == null) {
                throw new LocalizedFieldValidationException(MessageSeeds.NOT_A_VALID_CERTIFICATE, "file");
            }
            certificateWrapper.setCertificate(certificate);
            certificateWrapper.save();
            return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
        } catch (CertificateException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE, e);
        } catch (NoSuchProviderException e) {
            throw exceptionFactory.newException(MessageSeeds.COULD_NOT_CREATE_CERTIFICATE_FACTORY, e);
        }
    }

    @GET
    @Path("/aliases")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList aliasSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params, @Context UriInfo uriInfo) {
        PkiService.AliasSearchFilter aliasSearchFilter = getAliasSearchFilter(params, uriInfo.getQueryParameters(), jsonQueryFilter);
        List<AliasInfo> collect = pkiService.getAliasesByFilter(aliasSearchFilter)
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getAlias)
                .map(AliasInfo::new)
                .collect(toList());
        return PagedInfoList.fromPagedList("aliases", collect, queryParameters);
    }

    private PkiService.AliasSearchFilter getAliasSearchFilter(StandardParametersBean params, MultivaluedMap<String, String> uriParams, JsonQueryFilter jsonQueryFilter) {
        PkiService.AliasSearchFilter aliasSearchFilter = new PkiService.AliasSearchFilter();
        String alias = null;
        Long trustStoreId = null;

        if (uriParams.containsKey("alias")) {
            alias = params.getFirst("alias");
        }
        if (alias == null && jsonQueryFilter.hasFilters()) {
            alias = jsonQueryFilter.getString("alias");
        }
        if (uriParams.containsKey("trustStore")) {
            trustStoreId = Long.valueOf(params.getFirst("trustStore"));
        }
        if (trustStoreId == null && jsonQueryFilter.hasProperty("trustStore")) {
            trustStoreId = jsonQueryFilter.getLong("trustStore");
        }
        if (trustStoreId != null) {
            aliasSearchFilter.trustStore = pkiService.findTrustStore(trustStoreId)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUSTSTORE, "trustStore"));
        }
        if (alias == null || alias.isEmpty()) {
            aliasSearchFilter.alias = "*";
        }
        if (alias != null && !alias.isEmpty()) {
            if (!alias.contains("*") && !alias.contains("?")) {
                aliasSearchFilter.alias = "*" + alias + "*";
            } else {
                aliasSearchFilter.alias = alias;
            }
        }
        return aliasSearchFilter;
    }

    @GET
    @Path("/subjects")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList subjectSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params, @Context UriInfo uriInfo) {
        PkiService.SubjectSearchFilter subjectSearchFilter = getSubjectSearchFilter(params, uriInfo.getQueryParameters(), jsonQueryFilter);
        List<SubjectInfo> collect = pkiService.getSubjectsByFilter(subjectSearchFilter)
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getSubject)
                .map(SubjectInfo::new)
                .collect(toList());
        return PagedInfoList.fromPagedList("subjects", collect, queryParameters);
    }

    private PkiService.SubjectSearchFilter getSubjectSearchFilter(StandardParametersBean params, MultivaluedMap<String, String> uriParams, JsonQueryFilter jsonQueryFilter) {
        PkiService.SubjectSearchFilter subjectSearchFilter = new PkiService.SubjectSearchFilter();
        String subject = null;
        Long trustStoreId = null;

        if (uriParams.containsKey("subject")) {
            subject = params.getFirst("subject");
        }
        if (subject == null && jsonQueryFilter.hasProperty("subject")) {
            subject = jsonQueryFilter.getString("subject");
        }
        if (uriParams.containsKey("trustStore")) {
            trustStoreId = Long.valueOf(params.getFirst("trustStore"));
        }
        if (trustStoreId == null && jsonQueryFilter.hasProperty("trustStore")) {
            trustStoreId = jsonQueryFilter.getLong("trustStore");
        }
        if (trustStoreId != null) {
            subjectSearchFilter.trustStore = pkiService.findTrustStore(trustStoreId)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUSTSTORE, "trustStore"));
        }
        if (subject == null || subject.isEmpty()) {
            subjectSearchFilter.subject = "*";
        }
        if (subject != null && !subject.isEmpty()) {
            if (!subject.contains("*") && !subject.contains("?")) {
                subjectSearchFilter.subject = "*" + subject + "*";
            } else {
                subjectSearchFilter.subject = subject;
            }
        }
        return subjectSearchFilter;
    }

    @GET
    @Path("/issuers")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList issuerSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params, @Context UriInfo uriInfo) {
        PkiService.IssuerSearchFilter issueSearchFilter = getIssuerSearchFilter(params, uriInfo.getQueryParameters(), jsonQueryFilter);
        List<IssuerInfo> collect = pkiService.getIssuersByFilter(issueSearchFilter)
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getIssuer)
                .map(IssuerInfo::new)
                .collect(toList());
        return PagedInfoList.fromPagedList("issuers", collect, queryParameters);
    }

    private PkiService.IssuerSearchFilter getIssuerSearchFilter(StandardParametersBean params, MultivaluedMap<String, String> uriParams, JsonQueryFilter jsonQueryFilter) {
        PkiService.IssuerSearchFilter issuerSearchFilter = new PkiService.IssuerSearchFilter();
        String issuer = null;
        Long trustStoreId = null;

        if (uriParams.containsKey("issuer")) {
            issuer = params.getFirst("issuer");
        }
        if (issuer == null && jsonQueryFilter.hasProperty("issuer")) {
            issuer = jsonQueryFilter.getString("issuer");
        }
        if (uriParams.containsKey("trustStore")) {
            trustStoreId = Long.valueOf(params.getFirst("trustStore"));
        }
        if (trustStoreId == null && jsonQueryFilter.hasProperty("trustStore")) {
            trustStoreId = jsonQueryFilter.getLong("trustStore");
        }
        if (trustStoreId != null) {
            issuerSearchFilter.trustStore = pkiService.findTrustStore(trustStoreId)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUSTSTORE, "trustStore"));
        }
        if (issuer == null || issuer.isEmpty()) {
            issuerSearchFilter.issuer = "*";
        }
        if (issuer != null && !issuer.isEmpty()) {
            if (!issuer.contains("*") && !issuer.contains("?")) {
                issuerSearchFilter.issuer = "*" + issuer + "*";
            } else {
                issuerSearchFilter.issuer = issuer;
            }
        }
        return issuerSearchFilter;
    }

    @GET
    @Path("/keyusages")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList keyUsagesSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params, @Context UriInfo uriInfo) {
        PkiService.KeyUsagesSearchFilter keyUsagesSearchFilter = getKeyUsagesSearchFilter(params, uriInfo.getQueryParameters(), jsonQueryFilter);

        List<KeyUsageInfo> infos = pkiService.getKeyUsagesByFilter(keyUsagesSearchFilter)
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getStringifiedKeyUsages)
                .map(x -> filterKeyUsagesbySearchParam().apply(x, keyUsagesSearchFilter.keyUsages))
                .flatMap(Collection::stream)
                .map(KeyUsageInfo::new)
                .distinct()
                .collect(toList());

        return PagedInfoList.fromPagedList("keyusages", infos, queryParameters);
    }

    private PkiService.KeyUsagesSearchFilter getKeyUsagesSearchFilter(StandardParametersBean params, MultivaluedMap<String, String> uriParams, JsonQueryFilter jsonQueryFilter) {
        PkiService.KeyUsagesSearchFilter keyUsagesSearchFilter = new PkiService.KeyUsagesSearchFilter();
        String keyUsages = null;
        Long trustStoreId = null;

        if (uriParams.containsKey("keyUsages")) {
            keyUsages = params.getFirst("keyUsages");
        }
        if (keyUsages == null && jsonQueryFilter.hasProperty("keyUsages")) {
            keyUsages = jsonQueryFilter.getString("keyUsages");
        }
        if (uriParams.containsKey("trustStore")) {
            trustStoreId = Long.valueOf(params.getFirst("trustStore"));
        }
        if (trustStoreId == null && jsonQueryFilter.hasProperty("trustStore")) {
            trustStoreId = jsonQueryFilter.getLong("trustStore");
        }
        if (trustStoreId != null) {
            keyUsagesSearchFilter.trustStore = pkiService.findTrustStore(trustStoreId)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUSTSTORE, "trustStore"));
        }
        if (keyUsages == null || keyUsages.isEmpty()) {
            keyUsagesSearchFilter.keyUsages = "*";
        }
        if (keyUsages != null && !keyUsages.isEmpty()) {
            if (!keyUsages.contains("*") && !keyUsages.contains("?")) {
                keyUsagesSearchFilter.keyUsages = "*" + keyUsages + "*";
            } else {
                keyUsagesSearchFilter.keyUsages = keyUsages;
            }
        }
        return keyUsagesSearchFilter;
    }

    @GET
    @Path("/extendedkeyusages")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList extendedKeyUsagesSource(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params, @Context UriInfo uriInfo) {
        PkiService.ExtendedKeyUsagesSearchFilter extendedKeyUsagesSearchFilter = getExtendedKeyUsagesSearchFilter(params, uriInfo.getQueryParameters(), jsonQueryFilter);

        List<ExtendedKeyUsageInfo> infos = pkiService.getExtendedKeyUsagesByFilter(extendedKeyUsagesSearchFilter)
                .from(queryParameters)
                .stream()
                .map(CertificateWrapper::getStringifiedExtendedKeyUsages)
                .map(x -> filterKeyUsagesbySearchParam().apply(x, extendedKeyUsagesSearchFilter.extendedKeyUsages))
                .flatMap(Collection::stream)
                .map(ExtendedKeyUsageInfo::new)
                .distinct()
                .collect(toList());

        return PagedInfoList.fromPagedList("extendedkeyusages", infos, queryParameters);
    }

    private PkiService.ExtendedKeyUsagesSearchFilter getExtendedKeyUsagesSearchFilter(StandardParametersBean params, MultivaluedMap<String, String> uriParams, JsonQueryFilter jsonQueryFilter) {
        PkiService.ExtendedKeyUsagesSearchFilter extendedKeyUsagesSearchFilter = new PkiService.ExtendedKeyUsagesSearchFilter();
        String extendedKeyUsages = null;
        Long trustStoreId = null;

        if (uriParams.containsKey("extendedKeyUsages")) {
            extendedKeyUsages = params.getFirst("extendedKeyUsages");
        }
        if (extendedKeyUsages == null && jsonQueryFilter.hasProperty("extendedKeyUsages")) {
            extendedKeyUsages = jsonQueryFilter.getString("extendedKeyUsages");
        }
        if (uriParams.containsKey("trustStore")) {
            trustStoreId = Long.valueOf(params.getFirst("trustStore"));
        }
        if (trustStoreId == null && jsonQueryFilter.hasProperty("trustStore")) {
            trustStoreId = jsonQueryFilter.getLong("trustStore");
        }
        if (trustStoreId != null) {
            extendedKeyUsagesSearchFilter.trustStore = pkiService.findTrustStore(trustStoreId)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUSTSTORE, "trustStore"));
        }
        if (extendedKeyUsages == null || extendedKeyUsages.isEmpty()) {
            extendedKeyUsagesSearchFilter.extendedKeyUsages = "*";
        }
        if (extendedKeyUsages != null && !extendedKeyUsages.isEmpty()) {
            if (!extendedKeyUsages.contains("*") && !extendedKeyUsages.contains("?")) {
                extendedKeyUsagesSearchFilter.extendedKeyUsages = "*" + extendedKeyUsages + "*";
            } else {
                extendedKeyUsagesSearchFilter.extendedKeyUsages = extendedKeyUsages;
            }
        }
        return extendedKeyUsagesSearchFilter;
    }


    private BiFunction<String, String, List<String>> filterKeyUsagesbySearchParam() {
        return (String usages, String searchParam) ->
                Stream.of(usages.split(","))
                        .map(usage -> usage.toLowerCase().trim())
                        .filter(x -> x.contains(searchParam.
                                replace("*", "")
                                .replace("?", "")))
                        .collect(toList());
    }

}
