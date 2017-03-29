/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Checks;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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
import java.util.List;

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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_TASK_OVERVIEW})
    public PagedInfoList getCertificates(@BeanParam JsonQueryParameters queryParameters) {
        List<CertificateWrapperInfo> infoList = pkiService.findAllCertificates()
                .from(queryParameters)
                .stream()
                .map(certificateInfoFactory::asInfo)
                .collect(toList());

        return PagedInfoList.fromPagedList("certificates", infoList, queryParameters);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    public CertificateWrapperInfo getCertificate(@PathParam("id") long certificateId) {
        CertificateWrapper certificateWrapper = pkiService.findCertificateWrapper(certificateId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CERTIFICATE));

        return certificateInfoFactory.asInfo(certificateWrapper);
    }

    @POST // This should be PUT but has to be POST due to some 3th party issue
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{id}")
    @Transactional
    public Response importCertificateIntoExistingWrapper(
            @PathParam("id") long certificateWrapperId,
            @FormDataParam("file") InputStream certificateInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("version") long version) {
        if (contentDispositionHeader==null) {
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
    @Path("/csr")
    @Transactional
    public Response createCertificateWrapperWithKeysAndCSR(CsrInfo csrInfo) {
        if (csrInfo.keyTypeId==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyTypeId");
        }
        if (csrInfo.alias==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "alias");
        }
        if (csrInfo.keyEncryptionMethod==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyEncryptionMethod");
        }
        if (csrInfo.CN==null) {
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
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
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
                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename = "+certificateWrapper.getAlias().replaceAll("[^a-zA-Z0-9-_]", "")+".csr")
                    .build();
        } catch (IOException e) {
            throw exceptionFactory.newException(MessageSeeds.FAILED_TO_READ_CSR, e);
        }
    }

    @GET
    @Path("{id}/download/certificate")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
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
                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename = "+certificateWrapper.getAlias().replaceAll("[^a-zA-Z0-9-_]", "")+".cert")
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

    private Response doImportCertificateForCertificateWrapper(@FormDataParam("file") InputStream certificateInputStream, CertificateWrapper certificateWrapper) {
        try {
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
