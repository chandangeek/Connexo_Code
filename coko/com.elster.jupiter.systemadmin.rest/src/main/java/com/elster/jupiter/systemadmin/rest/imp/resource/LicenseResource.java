/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.systemadmin.rest.imp.response.ActionInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.LicenseInfoFactory;
import com.elster.jupiter.systemadmin.rest.imp.response.LicenseShortInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.RootEntity;
import com.elster.jupiter.util.json.JsonService;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.SignedObject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/license")
public class LicenseResource {

    private static final int MAX_LICENSE_FILE_SIZE = 1024 * 1024;
    private static final int UNPROCESSIBLE_ENTITY = 422;

    private final LicenseService licenseService;
    private final LicenseInfoFactory licenseInfoFactory;
    private final Thesaurus thesaurus;
    private final JsonService jsonService;
    private final NlsService nlsService;

    @Inject
    public LicenseResource(LicenseService licenseService, LicenseInfoFactory licenseInfoFactory, Thesaurus thesaurus, JsonService jsonService, NlsService nlsService) {
        this.licenseService = licenseService;
        this.licenseInfoFactory = licenseInfoFactory;
        this.thesaurus = thesaurus;
        this.jsonService = jsonService;
        this.nlsService = nlsService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_LICENSE)
    public PagedInfoList getLicenses(@BeanParam JsonQueryParameters queryParameters) {
        List<LicenseShortInfo> infos = licenseService.getLicensedApplicationKeys().stream()
                .map(licenseService::getLicenseForApplication)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(licenseInfoFactory::asShortInfo)
                .sorted((l1, l2) -> l1.expires.compareTo(l2.expires))
                .collect(Collectors.toList());
        List<LicenseShortInfo> pagedInfos = ListPager.of(infos).paged(queryParameters.getStart().orElse(null), queryParameters.getLimit().orElse(null)).find();
        return PagedInfoList.fromPagedList("data", pagedInfos, queryParameters);
    }

    @GET
    @Path("/{applicationkey}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_LICENSE)
    public RootEntity getLicense(@PathParam("applicationkey") String tag) {
        License license = licenseService.getLicenseForApplication(tag).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return new RootEntity<>(licenseInfoFactory.asInfo(license));
    }

    @POST
    @Transactional
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed(Privileges.Constants.UPLOAD_LICENSE)
    public Response uploadLicense(@FormDataParam("uploadField") InputStream inputStream,
                                  @FormDataParam("uploadField") FormDataContentDisposition contentDispositionHeader) {
        byte[] bytes = loadLicenseFile(inputStream);
        SignedObject signedObject = readLicense(bytes);
        ActionInfo info = addLicense(signedObject);
        return Response.ok().entity(jsonService.serialize(info)).build();
    }

    private byte[] loadLicenseFile(InputStream inputStream) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); InputStream fis = inputStream) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                out.write(buffer, 0, length);
                if (out.size() > MAX_LICENSE_FILE_SIZE) {
                    throw new WebApplicationException(Response.status(UNPROCESSIBLE_ENTITY).entity(
                            jsonService.serialize(new ConstraintViolationInfo(thesaurus).from(new InvalidLicenseFileException(MessageSeeds.MAX_FILE_SIZE_EXCEEDED, MAX_LICENSE_FILE_SIZE/1024/1024)))).build());
                }
            }
            return out.toByteArray();
        } catch (IOException ex) {
            throw new WebApplicationException(Response.status(UNPROCESSIBLE_ENTITY).entity(
                    jsonService.serialize(new ConstraintViolationInfo(thesaurus).from(new InvalidLicenseFileException(MessageSeeds.FILE_IO)))).build());
        }
    }

    private SignedObject readLicense(byte[] bytes) {
        SignedObject signedObject;
        try {
            ObjectInputStream serializedObject = new ObjectInputStream(new ByteArrayInputStream(bytes));
            signedObject = (SignedObject) serializedObject.readObject();
            serializedObject.close();
        } catch (Exception ex) {
            throw new WebApplicationException(Response.status(UNPROCESSIBLE_ENTITY).entity(
                    jsonService.serialize(new ConstraintViolationInfo(thesaurus).from(new InvalidLicenseFileException(MessageSeeds.INVALID_LICENSE_FILE)))).build());
        }
        return signedObject;
    }

    private ActionInfo addLicense(SignedObject signedObject) {
        ActionInfo info = new ActionInfo();
        try {
            List<String> licensedApps = licenseService.addLicense(signedObject).stream()
                    .map(appKey -> nlsService.getThesaurus(appKey, Layer.REST).getString(appKey, appKey))
                    .collect(Collectors.toList());
            info.setSuccess(licensedApps);
            return info;
        } catch (Exception ex) {
            info.setFailure(ex.getLocalizedMessage());
            throw new WebApplicationException(Response.status(UNPROCESSIBLE_ENTITY).entity(jsonService.serialize(info)).build());
        }
    }
}