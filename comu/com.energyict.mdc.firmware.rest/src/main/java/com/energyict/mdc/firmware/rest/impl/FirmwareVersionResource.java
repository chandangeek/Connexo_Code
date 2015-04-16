package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.*;
import org.glassfish.jersey.internal.util.Base64;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;


import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Path("/devicetypes/{deviceTypeId}/firmwares")
@MultipartConfig(maxFileSize=FirmwareService.MAX_FIRMWARE_FILE_SIZE)
public class FirmwareVersionResource {
    private final FirmwareService firmwareService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Thesaurus thesaurus;
    private final ExceptionFactory exceptionFactory;

    private static final String FILTER_STATUS_PARAMETER = "firmwareStatus";
    private static final String FILTER_TYPE_PARAMETER = "firmwareType";

    @Inject
    public FirmwareVersionResource(FirmwareService firmwareService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus, ExceptionFactory exceptionFactory) {
        this.firmwareService = firmwareService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getFirmwareVersions(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryFilter filter, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(deviceTypeId);

        Finder<FirmwareVersion> allFirmwaresFinder = firmwareService.findAllFirmwareVersions(getFirmwareFilter(filter, deviceType));
        List<FirmwareVersion> allFirmwares = allFirmwaresFinder.from(queryParameters).find();
        List<FirmwareVersionInfo> firmwareInfos = FirmwareVersionInfo.from(allFirmwares, thesaurus);
        return PagedInfoList.fromPagedList("firmwares", firmwareInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response getFirmwareVersions(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, @BeanParam JsonQueryFilter filter, @BeanParam QueryParameters queryParameters) {
        FirmwareVersion firmwareVersion = firmwareService.getFirmwareVersionById(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        return Response.status(Response.Status.OK).entity(FirmwareVersionInfo.from(firmwareVersion, thesaurus)).build();
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response validateFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, FirmwareVersionInfo firmwareVersionInfo) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareVersion versionToValidate = firmwareService.newFirmwareVersion(deviceType, firmwareVersionInfo.firmwareVersion,
                firmwareVersionInfo.firmwareStatus.id, firmwareVersionInfo.firmwareType.id);

        if (firmwareVersionInfo.fileSize != null) {
            versionToValidate.setFirmwareFile(new byte[firmwareVersionInfo.fileSize]);
        }
        versionToValidate.validate();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response saveFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @FormDataParam("firmwareFile") InputStream fileInputStream,
                                        @FormDataParam("firmwareFile") FormDataContentDisposition contentDispositionHeader, @FormDataParam("data") FirmwareVersionInfo firmwareVersionInfo, @FormDataParam("data") FormDataContentDisposition firmwareVersionContent) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareVersion versionToSave = firmwareService.newFirmwareVersion(deviceType, firmwareVersionInfo.firmwareVersion,
                firmwareVersionInfo.firmwareStatus.id, firmwareVersionInfo.firmwareType.id);

        setFirmwareFile(versionToSave, fileInputStream, contentDispositionHeader);

        firmwareService.saveFirmwareVersion(versionToSave);
        return Response.status(Response.Status.CREATED).entity(FirmwareVersionInfo.from(versionToSave, thesaurus)).build();
    }

    @PUT
    @Path("/{id}/validate")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response validateEditFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo firmwareVersionInfo) {
        FirmwareVersion firmwareVersion = findFirmwareVersionOrElseThrowException(id);
        checkIfEditableOrThrowException(firmwareVersion);
        firmwareVersion.setFirmwareVersion(firmwareVersionInfo.firmwareVersion);
        firmwareVersion.setFirmwareStatus(firmwareVersionInfo.firmwareStatus.id);
        if (firmwareVersionInfo.fileSize != null) {
            firmwareVersion.setFirmwareFile(new byte[firmwareVersionInfo.fileSize]);
        }
        firmwareVersion.validate();
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response editFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, @FormDataParam("firmwareFile") InputStream fileInputStream,
                                        @FormDataParam("firmwareFile") FormDataContentDisposition contentDispositionHeader, @FormDataParam("data") FirmwareVersionInfo firmwareVersionInfo, @FormDataParam("data") FormDataContentDisposition firmwareVersionContent) {
        FirmwareVersion firmwareVersion = findFirmwareVersionOrElseThrowException(id);

        if(FirmwareStatus.DEPRECATED.equals(firmwareVersionInfo.firmwareStatus.id)) {
            firmwareService.deprecateFirmwareVersion(firmwareVersion);
        } else {
            firmwareVersion.setFirmwareVersion(firmwareVersionInfo.firmwareVersion);

            firmwareVersion.setFirmwareStatus(firmwareVersionInfo.firmwareStatus.id);
            setFirmwareFile(firmwareVersion, fileInputStream, contentDispositionHeader);
            firmwareService.saveFirmwareVersion(firmwareVersion);
        }
        return Response.status(Response.Status.OK).entity(FirmwareVersionInfo.from(firmwareVersion, thesaurus)).build();
    }

    private FirmwareVersionFilter getFirmwareFilter(JsonQueryFilter filter, DeviceType deviceType) {
        FirmwareVersionFilter firmwareVersionFilter = new FirmwareVersionFilter(deviceType);

        if (filter.hasFilters()) {
            if (filter.hasProperty(FILTER_STATUS_PARAMETER)) {
                List<String> stringFirmwareStatuses = filter.getStringList(FILTER_STATUS_PARAMETER);
                List<FirmwareStatus> firmwareStatuses = stringFirmwareStatuses.stream().map(FirmwareStatus::from).collect(Collectors.toList());
                if (!firmwareStatuses.isEmpty()) {
                    firmwareVersionFilter.setFirmwareStatuses(firmwareStatuses);
                }
            }
            if (filter.hasProperty(FILTER_TYPE_PARAMETER)) {
                List<String> stringFirmwareTypes = filter.getStringList(FILTER_TYPE_PARAMETER);
                List<FirmwareType> firmwareTypes = stringFirmwareTypes.stream().map(FirmwareType::from).collect(Collectors.toList());
                if (!firmwareTypes.isEmpty()) {
                    firmwareVersionFilter.setFirmwareTypes(firmwareTypes);
                }
            }
        }
        return firmwareVersionFilter;
    }

    private DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }

    private FirmwareVersion findFirmwareVersionOrElseThrowException(long id) {
        return firmwareService.getFirmwareVersionById(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.FIRMWARE_VERSION_NOT_FOUND, id));
    }

    private void checkIfEditableOrThrowException(FirmwareVersion firmwareVersion) {
        if (firmwareService.isFirmwareVersionInUse(firmwareVersion.getId())) {
            throw exceptionFactory.newException(MessageSeeds.VERSION_IN_USE);
        }
        if (firmwareVersion.getFirmwareStatus().equals(FirmwareStatus.DEPRECATED)) {
            throw exceptionFactory.newException(MessageSeeds.VERSION_IS_DEPRECATED);
        }
    }

    private void setFirmwareFile(FirmwareVersion firmwareVersion, InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader)  {
        if (contentDispositionHeader != null) {
            if (contentDispositionHeader.getSize() > Integer.MAX_VALUE) {
                throw exceptionFactory.newException(MessageSeeds.MAX_FILE_SIZE_EXCEEDED);
            }
            if (contentDispositionHeader.getSize() > 0) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    byte[] buffer = new byte[(int) contentDispositionHeader.getSize()];
                    int length;
                    while ((length = fileInputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                    }
                    fileInputStream.close();
                    firmwareVersion.setFirmwareFile(Base64.decode(out.toByteArray()));
                    out.close();
                } catch(IOException ex) {
                    throw exceptionFactory.newException(MessageSeeds.FILE_IO);
                }
            }
        }
    }
}
