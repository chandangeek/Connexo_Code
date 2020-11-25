package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Checks;

import com.google.common.io.ByteStreams;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemReader;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 9/26/17.
 */
@Path("/keypairs")
public class KeypairWrapperResource {

    private final SecurityManagementService securityManagementService;
    private final KeypairInfoFactory keypairInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public KeypairWrapperResource(SecurityManagementService securityManagementService, KeypairInfoFactory keypairInfoFactory, ExceptionFactory exceptionFactory) {
        this.securityManagementService = securityManagementService;
        this.keypairInfoFactory = keypairInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CERTIFICATES, Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public PagedInfoList getKeypairs(@BeanParam JsonQueryParameters queryParameters) {
        List<KeypairWrapperInfo> infoList = securityManagementService.findAllKeypairs()
                .from(queryParameters)
                .stream()
                .map(keypairInfoFactory::asInfo)
                .collect(toList());
        return PagedInfoList.fromPagedList("keypairs", infoList, queryParameters);
    }

    @GET
    @Path("{id}/download/publickey")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON+";charset=UTF-8"})
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response downloadPublicKey(@PathParam("id") long keypairId) {
        KeypairWrapper keypairWrapper = securityManagementService.findKeypairWrapper(keypairId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_KEYPAIR));
        PublicKey publicKey = keypairWrapper.getPublicKey()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_PUBLIC_KEY_PRESENT));
        StreamingOutput streamingOutput = output -> {
            output.write(serializePublicKey(publicKey));
            output.flush();
        };
        return Response
                .ok(streamingOutput, MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename = "+keypairWrapper.getAlias().replaceAll("[^a-zA-Z0-9-_]", "")+".pem")
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    @Transactional
    public KeypairWrapperInfo generateNewKeypair(KeypairWrapperInfo keypairWrapperInfo) {
        if (keypairWrapperInfo.keyType==null || keypairWrapperInfo.keyType.id==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyType");
        }
        if (Checks.is(keypairWrapperInfo.alias).emptyOrOnlyWhiteSpace()) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "alias");
        }
        if (Checks.is(keypairWrapperInfo.keyEncryptionMethod).emptyOrOnlyWhiteSpace()) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyEncryptionMethod");
        }
        KeyType keyType = securityManagementService.findAllKeyTypes()
                .stream()
                .filter(keyType1 -> keyType1.getId() == Long.valueOf((Integer)keypairWrapperInfo.keyType.id))
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_KEY_TYPE));
        KeypairWrapper keypairWrapper = securityManagementService.newKeypairWrapper(keypairWrapperInfo.alias, keyType, keypairWrapperInfo.keyEncryptionMethod);
        keypairWrapper.generateValue();
        return keypairInfoFactory.asInfo(keypairWrapper);
    }

    @POST // This should be PUT but has to be POST due to some 3th party issue
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    @Path("/import")
    @Transactional
    public KeypairWrapperInfo importNewKeypair(KeypairWrapperInfo keypairWrapperInfo) {
        if (keypairWrapperInfo.keyType==null || keypairWrapperInfo.keyType.id==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyType");
        }
        if (Checks.is(keypairWrapperInfo.alias).emptyOrOnlyWhiteSpace()) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "alias");
        }
        if (Checks.is(keypairWrapperInfo.keyEncryptionMethod).emptyOrOnlyWhiteSpace()) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "keyEncryptionMethod");
        }
        if(Checks.is(keypairWrapperInfo.key).emptyOrOnlyWhiteSpace()){
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "key");
        }
        KeyType keyType = securityManagementService.findAllKeyTypes()
                .stream()
                .filter(keyType1 -> keyType1.getId() == Long.valueOf((Integer)keypairWrapperInfo.keyType.id))
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_KEY_TYPE));
        KeypairWrapper keypairWrapper = securityManagementService.newKeypairWrapper(keypairWrapperInfo.alias, keyType, keypairWrapperInfo.keyEncryptionMethod);
        try {
            byte[] bytes = new PemReader(new StringReader(keypairWrapperInfo.key)).readPemObject().getContent();
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance(keyType.getKeyAlgorithm());
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            PlaintextPrivateKeyWrapper privateKeyWrapper = (PlaintextPrivateKeyWrapper) keypairWrapper.getPrivateKeyWrapper().get();
            privateKeyWrapper.setPrivateKey(privateKey);
            privateKeyWrapper.save();
            keypairWrapper.setPublicKey(privateKeyWrapper.getPublicKey());
            keypairWrapper.save();
        } catch (Exception e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_KEY,"key");
        }

        return keypairInfoFactory.asInfo(keypairWrapper);
    }

    @POST // This should be PUT but has to be POST due to some 3th party issue
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    @Path("/{id}/publickey")
    @Transactional
    public Response importPublicKeyIntoExistingWrapper(
            @PathParam("id") long keypairWrapperId,
            @FormDataParam("file") InputStream publicKeyInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("version") long version) {
        if (contentDispositionHeader==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "file");
        }
        KeypairWrapper keypairWrapper = securityManagementService.findAndLockKeypairWrapper(keypairWrapperId, version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_KEYPAIR));
        try {
            doImportPublicKeyForKeypairWrapper(publicKeyInputStream, keypairWrapper);
        } catch (IOException e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_PUBLIC_KEY, "file");
        }
        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    @POST // This should be PUT but has to be POST due to some 3th party issue
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    @Path("/{id}/keypair")
    @Transactional
    public Response importKeypairIntoExistingWrapper(
            @PathParam("id") long keypairWrapperId,
            @FormDataParam("file") InputStream keypairInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("version") long version) {
        if (contentDispositionHeader==null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "file");
        }
        KeypairWrapper keypairWrapper = securityManagementService.findAndLockKeypairWrapper(keypairWrapperId, version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_KEYPAIR));
        try {
            doImportKeypairForKeypairWrapper(keypairInputStream, keypairWrapper);
        } catch (IOException e) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_PUBLIC_KEY, "file");
        }
        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    private void doImportPublicKeyForKeypairWrapper(InputStream publicKeyInputStream, KeypairWrapper keypairWrapper) throws
            IOException {
        byte[] key = ByteStreams.toByteArray(publicKeyInputStream);
        keypairWrapper.setPublicKey(key);
        keypairWrapper.save();
    }


    private void doImportKeypairForKeypairWrapper(InputStream publicKeyInputStream, KeypairWrapper keypairWrapper) throws
            IOException {
        // TODO, we would need the real properties here
    }


    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CERTIFICATES})
    public Response removeKeypair(@PathParam("id") long keypairId) {
        KeypairWrapper keypairWrapper = securityManagementService.findKeypairWrapper(keypairId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_KEYPAIR));
        keypairWrapper.delete();
        return Response.status(Response.Status.OK).build();
    }

    private byte[] serializePublicKey(final PublicKey publicKey) throws IOException {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(new OutputStreamWriter(outStream,
                StandardCharsets.UTF_8));
        jcaPEMWriter.writeObject(publicKey);
        jcaPEMWriter.flush();
        jcaPEMWriter.close();
        return outStream.toByteArray();
    }

}
