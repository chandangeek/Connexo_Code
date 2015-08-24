package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Path("/devicetypes/{deviceTypeId}/firmwares")
public class FirmwareVersionResource {
    private final FirmwareService firmwareService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final FirmwareVersionInfoFactory versionFactory;

    private static final String FILTER_STATUS_PARAMETER = "firmwareStatus";
    private static final String FILTER_TYPE_PARAMETER = "firmwareType";

    @Inject
    public FirmwareVersionResource(FirmwareService firmwareService, ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, FirmwareVersionInfoFactory versionFactory) {
        this.firmwareService = firmwareService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.versionFactory = versionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getFilteredFirmwareVersions(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        Finder<FirmwareVersion> allFirmwaresFinder = firmwareService.findAllFirmwareVersions(getFirmwareFilter(filter, deviceType));
        List<FirmwareVersion> allFirmwares = allFirmwaresFinder.from(queryParameters).find();
        return PagedInfoList.fromPagedList("firmwares", versionFactory.from(allFirmwares), queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response getFirmwareVersionById(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id) {
        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(id);
        return Response.ok().entity(versionFactory.fullInfo(firmwareVersion)).build();
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response validateFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, FirmwareVersionInfo firmwareVersionInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareVersion versionToValidate = firmwareService.newFirmwareVersion(deviceType, firmwareVersionInfo.firmwareVersion,
                firmwareVersionInfo.firmwareStatus.id, firmwareVersionInfo.firmwareType.id);

        if (firmwareVersionInfo.fileSize != null) {
            versionToValidate.setExpectedFirmwareSize(firmwareVersionInfo.fileSize);
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
        DeviceType deviceType =  resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        String firmwareVersion = getStringValueFromStream(versionInputStream);
        FirmwareType firmwareType = parseFirmwareTypeField(typeInputStream).orElse(null);
        FirmwareStatus firmwareStatus = parseFirmwareStatusField(statusInputStream).orElse(null);

        FirmwareVersion versionToSave = firmwareService.newFirmwareVersion(deviceType, firmwareVersion, firmwareStatus, firmwareType);
        setFirmwareFile(versionToSave, fileInputStream);
        versionToSave.save();

        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    @PUT
    @Path("/{id}/validate")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response validateEditFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo firmwareVersionInfo) {
        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(id);
        checkIfEditableOrThrowException(firmwareVersion);
        firmwareVersion.setFirmwareVersion(firmwareVersionInfo.firmwareVersion);
        firmwareVersion.setFirmwareStatus(firmwareVersionInfo.firmwareStatus.id);
        if (firmwareVersionInfo.fileSize != null) {
            firmwareVersion.setExpectedFirmwareSize(firmwareVersionInfo.fileSize);
        }
        firmwareVersion.validate();
        return Response.ok().build();
    }

    @POST
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
        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(id);

        firmwareVersion.setFirmwareVersion(getStringValueFromStream(versionInputStream));
        parseFirmwareStatusField(statusInputStream).ifPresent(firmwareVersion::setFirmwareStatus);
        setFirmwareFile(firmwareVersion, fileInputStream);
        firmwareVersion.save();

        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response updateStatusOfFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo firmwareVersionInfo) {
        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(id);

        switch (firmwareVersionInfo.firmwareStatus.id) {
        case DEPRECATED:
            firmwareVersion.deprecate();
            break;
        case FINAL:
            firmwareVersion.setFirmwareStatus(FirmwareStatus.FINAL);
            firmwareVersion.save();
            break;
        default:
        }
        return Response.ok().entity(versionFactory.fullInfo(firmwareVersion)).build();
    }

    private FirmwareVersionFilter getFirmwareFilter(JsonQueryFilter filter, DeviceType deviceType) {
        FirmwareVersionFilter firmwareVersionFilter = new FirmwareVersionFilter(deviceType);

        if (filter.hasFilters()) {
            if (filter.hasProperty(FILTER_STATUS_PARAMETER)) {
                List<String> stringFirmwareStatuses = filter.getStringList(FILTER_STATUS_PARAMETER);
                List<FirmwareStatus> firmwareStatuses = stringFirmwareStatuses.stream().map(FirmwareStatus::from).collect(Collectors.toList());
                if (!firmwareStatuses.isEmpty()) {
                    firmwareVersionFilter.addFirmwareStatuses(firmwareStatuses);
                }
            }
            if (filter.hasProperty(FILTER_TYPE_PARAMETER)) {
                List<String> stringFirmwareTypes = filter.getStringList(FILTER_TYPE_PARAMETER);
                List<FirmwareType> firmwareTypes = stringFirmwareTypes.stream().map(FirmwareType::from).collect(Collectors.toList());
                if (!firmwareTypes.isEmpty()) {
                    firmwareVersionFilter.addFirmwareTypes(firmwareTypes);
                }
            }
        }
        return firmwareVersionFilter;
    }

    private void checkIfEditableOrThrowException(FirmwareVersion firmwareVersion) {
        if (FirmwareStatus.FINAL.equals(firmwareVersion.getFirmwareStatus())
            && firmwareService.isFirmwareVersionInUse(firmwareVersion.getId())) {
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
            while ((length = fis.read(buffer)) != -1) {
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

    private Optional<FirmwareStatus> parseFirmwareStatusField(InputStream is) {
        String firmwareStatus = getStringValueFromStream(is);
        if(firmwareStatus == null || firmwareStatus.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(FirmwareStatusInfo.FIRMWARE_STATUS_ADAPTER.unmarshal(firmwareStatus));
    }

    private Optional<FirmwareType> parseFirmwareTypeField(InputStream is) {
        String firmwareType = getStringValueFromStream(is);
        if(firmwareType == null || firmwareType.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(FirmwareTypeInfo.FIRMWARE_TYPE_ADAPTER.unmarshal(firmwareType));
    }

    private String getStringValueFromStream(InputStream is) {
        if (is != null) {
            try (Scanner s = new Scanner(is)) {
                s.useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            }
        }
        return null;
    }
}
