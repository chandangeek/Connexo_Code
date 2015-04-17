package com.energyict.mdc.firmware.rest.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;

@Path("/devicetypes/{deviceTypeId}/firmwares")
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
    public Response getFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, @BeanParam JsonQueryFilter filter, @BeanParam QueryParameters queryParameters) {
        FirmwareVersion firmwareVersion = firmwareService.getFirmwareVersionById(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        return Response.ok().entity(FirmwareVersionInfo.from(firmwareVersion, thesaurus)).build();
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
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response saveFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId,
                @FormDataParam("firmwareFile") InputStream fileInputStream,
                @FormDataParam("firmwareFile") FormDataContentDisposition fileContentDispositionHeader,
                @FormDataParam("firmwareVersion") InputStream versionInputStream,
                @FormDataParam("firmwareVersion") FormDataContentDisposition versionContentDispositionHeader,
                @FormDataParam("firmwareType") InputStream typeInputStream,
                @FormDataParam("firmwareType") FormDataContentDisposition typeContentDispositionHeader,
                @FormDataParam("firmwareStatus") InputStream statusInputStream,
                @FormDataParam("firmwareStatus") FormDataContentDisposition statusContentDispositionHeader) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(deviceTypeId);
        String firmwareVersion = getStringValueFromStream(versionInputStream);
        FirmwareType firmwareType = FirmwareTypeInfo.FIRMWARE_TYPE_ADAPTER.unmarshal(getStringValueFromStream(typeInputStream));
        FirmwareStatus firmwareStatus = FirmwareStatusInfo.FIRMWARE_STATUS_ADAPTER.unmarshal(getStringValueFromStream(statusInputStream));

        FirmwareVersion versionToSave = firmwareService.newFirmwareVersion(deviceType, firmwareVersion, firmwareStatus, firmwareType);
        setFirmwareFile(versionToSave, fileInputStream);
        firmwareService.saveFirmwareVersion(versionToSave);
        
        return Response.status(Response.Status.CREATED).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
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
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response editFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId,
                @PathParam("id") long id,
                @FormDataParam("firmwareFile") InputStream fileInputStream,
                @FormDataParam("firmwareFile") FormDataContentDisposition fileContentDispositionHeader,
                @FormDataParam("firmwareVersion") InputStream versionInputStream,
                @FormDataParam("firmwareVersion") FormDataContentDisposition versionContentDispositionHeader,
                @FormDataParam("firmwareStatus") InputStream statusInputStream,
                @FormDataParam("firmwareStatus") FormDataContentDisposition statusContentDispositionHeader)
    {
        FirmwareVersion firmwareVersion = findFirmwareVersionOrElseThrowException(id);
        String firmwareVersionName = getStringValueFromStream(versionInputStream);
        FirmwareStatus firmwareStatus = FirmwareStatusInfo.FIRMWARE_STATUS_ADAPTER.unmarshal(getStringValueFromStream(statusInputStream));
        
        firmwareVersion.setFirmwareVersion(firmwareVersionName);
        firmwareVersion.setFirmwareStatus(firmwareStatus);
        setFirmwareFile(firmwareVersion, fileInputStream);
        firmwareService.saveFirmwareVersion(firmwareVersion);
        
        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response updateStatusOfFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo firmwareVersionInfo) {
        FirmwareVersion firmwareVersion = findFirmwareVersionOrElseThrowException(id);

        switch (firmwareVersionInfo.firmwareStatus.id) {
        case DEPRECATED:
            firmwareService.deprecateFirmwareVersion(firmwareVersion);
            break;
        case FINAL:
            firmwareVersion.setFirmwareStatus(FirmwareStatus.FINAL);
            firmwareService.saveFirmwareVersion(firmwareVersion);
            break;
        default:
        }
        return Response.ok().entity(FirmwareVersionInfo.from(firmwareVersion, thesaurus)).build();
    }

    private FirmwareVersionFilter getFirmwareFilter(JsonQueryFilter filter, DeviceType deviceType) {
        FirmwareVersionFilter firmwareVersionFilter = new FirmwareVersionFilter(deviceType);

        if (filter.hasFilters()) {
            if (filter.hasProperty(FILTER_STATUS_PARAMETER)) {
                List<String> stringFirmwareStatuses = filter.getStringList(FILTER_STATUS_PARAMETER);
                List<FirmwareStatus> firmwareStatuses = stringFirmwareStatuses.stream().map(FirmwareStatus::get).collect(Collectors.toList());
                if (!firmwareStatuses.isEmpty()) {
                    firmwareVersionFilter.setFirmwareStatuses(firmwareStatuses);
                }
            }
            if (filter.hasProperty(FILTER_TYPE_PARAMETER)) {
                List<String> stringFirmwareTypes = filter.getStringList(FILTER_TYPE_PARAMETER);
                List<FirmwareType> firmwareTypes = stringFirmwareTypes.stream().map(FirmwareType::get).collect(Collectors.toList());
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

    private void setFirmwareFile(FirmwareVersion firmwareVersion, InputStream fileInputStream) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); InputStream fis = fileInputStream) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
                if (out.size() > FirmwareService.MAX_FIRMWARE_FILE_SIZE) {
                    throw exceptionFactory.newException(MessageSeeds.MAX_FILE_SIZE_EXCEEDED);
                }
            }
            byte[] firmwareFile = out.toByteArray();
            if (firmwareFile.length > 0 ) {
                firmwareVersion.setFirmwareFile(firmwareFile);
            }
        } catch (IOException ex) {
            throw exceptionFactory.newException(MessageSeeds.FILE_IO);
        }
    }
    
    private String getStringValueFromStream(InputStream is) {
        try(Scanner s = new Scanner(is)) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }
}
