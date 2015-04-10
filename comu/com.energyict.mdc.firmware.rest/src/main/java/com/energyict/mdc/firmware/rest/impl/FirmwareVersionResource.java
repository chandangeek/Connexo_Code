package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import org.glassfish.jersey.internal.util.Base64;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/devicetypes/{deviceTypeId}/firmwares")
public class FirmwareVersionResource {
    private final FirmwareService firmwareService;
    private final RestQueryService restQueryService;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;

    private static final String FILTER_STATUS_PARAMETER = "firmwareStatus";
    private static final String FILTER_TYPE_PARAMETER = "firmwareType";

    @Inject
    public FirmwareVersionResource(FirmwareService firmwareService, RestQueryService restQueryService, ResourceHelper resourceHelper, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.restQueryService = restQueryService;
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getFirmwareVersions(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryFilter filter, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);

        Finder<FirmwareVersion> allFirmwaresFinder = firmwareService.findAllFirmwareVersions(getFirmwareVersionConditions(filter, deviceType));
        List<FirmwareVersion> allFirmwares = allFirmwaresFinder.from(queryParameters).find();
        List<FirmwareVersionInfo> firmwareInfos = FirmwareVersionInfo.from(allFirmwares, thesaurus);
        return PagedInfoList.fromPagedList("firmwares", firmwareInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response getFirmwareVersions(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, @BeanParam JsonQueryFilter filter, @BeanParam QueryParameters queryParameters) {
        FirmwareVersion firmwareVersion = resourceHelper.getFirmwareVersionByIdOrThrowException(id);
        return Response.status(Response.Status.OK).entity(FirmwareVersionInfo.from(firmwareVersion, thesaurus)).build();
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response validateFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, FirmwareVersionInfo firmwareVersionInfo) {
        DeviceType deviceType =  resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareVersion versionToValidate = firmwareService.newFirmwareVersion(deviceType, firmwareVersionInfo.firmwareVersion,
                firmwareVersionInfo.firmwareStatus.id, firmwareVersionInfo.firmwareType.id);

        if (firmwareVersionInfo.fileSize != null) {
            versionToValidate.setFirmwareFile(new byte[firmwareVersionInfo.fileSize]);
        }
        versionToValidate.validate();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response saveFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, FirmwareVersionInfo firmwareVersionInfo) {
        DeviceType deviceType =  resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareVersion versionToSave = firmwareService.newFirmwareVersion(deviceType, firmwareVersionInfo.firmwareVersion,
                firmwareVersionInfo.firmwareStatus.id, firmwareVersionInfo.firmwareType.id);

        if (firmwareVersionInfo.fileSize != null && firmwareVersionInfo.firmwareFile != null) {
            versionToSave.setFirmwareFile(Base64.decode(firmwareVersionInfo.firmwareFile.trim().getBytes()));
        }

        firmwareService.saveFirmwareVersion(versionToSave);
        return Response.status(Response.Status.CREATED).entity(FirmwareVersionInfo.from(versionToSave, thesaurus)).build();
    }

    @PUT
    @Path("/{id}/validate")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response validateEditFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo firmwareVersionInfo) {
        FirmwareVersion firmwareVersion = resourceHelper.getFirmwareVersionByIdOrThrowException(id);
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
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response editFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo firmwareVersionInfo) {
        FirmwareVersion firmwareVersion = resourceHelper.getFirmwareVersionByIdOrThrowException(id);
        firmwareVersion.setFirmwareVersion(firmwareVersionInfo.firmwareVersion);
        firmwareVersion.setFirmwareStatus(firmwareVersionInfo.firmwareStatus.id);
        if (firmwareVersionInfo.fileSize != null && firmwareVersionInfo.firmwareFile != null) {
            firmwareVersion.setFirmwareFile(Base64.decode(firmwareVersionInfo.firmwareFile.trim().getBytes()));
        }
        firmwareService.saveFirmwareVersion(firmwareVersion);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response deprecateFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id) {
        FirmwareVersion firmwareVersion = resourceHelper.getFirmwareVersionByIdOrThrowException(id);
        firmwareService.deprecateFirmwareVersion(firmwareVersion);
        return Response.status(Response.Status.OK).build();
    }

    private Condition getFirmwareVersionConditions(JsonQueryFilter filter, DeviceType deviceType) {
        Condition condition = where("deviceType").isEqualTo(deviceType);

        if (filter.hasFilters()) {
            if (filter.hasProperty(FILTER_STATUS_PARAMETER)) {
                List<String> stringFirmwareStatuses = filter.getStringList(FILTER_STATUS_PARAMETER);
                List<FirmwareStatus> firmwareStatuses = stringFirmwareStatuses.stream().map(FirmwareStatus::from).collect(Collectors.toList());
                if (!firmwareStatuses.isEmpty()) {
                    condition = condition.and(createMultipleConditions(firmwareStatuses, "firmwareStatus"));
                }
            }
            if (filter.hasProperty(FILTER_TYPE_PARAMETER)) {
                List<String> stringFirmwareTypes = filter.getStringList(FILTER_TYPE_PARAMETER);
                List<FirmwareType> firmwareTypes = stringFirmwareTypes.stream().map(FirmwareType::from).collect(Collectors.toList());
                if (!firmwareTypes.isEmpty()) {
                    condition = condition.and(createMultipleConditions(firmwareTypes, "firmwareType"));
                }
            }
        }
        return condition;
    }

    private <T> Condition createMultipleConditions(List<T> params, String conditionField) {
        Condition condition = Condition.FALSE;
        for (T value : params) {
            condition = condition.or(where(conditionField).isEqualTo(value));
        }
        return condition;
    }

    private void checkIfEditableOrThrowException(FirmwareVersion firmwareVersion) {
        if (firmwareService.isFirmwareVersionInUse(firmwareVersion.getId())) {
            throw new FirmwareVersionIsInUseException(thesaurus);
        }
        if (firmwareVersion.getFirmwareStatus().equals(FirmwareStatus.DEPRECATED)) {
            throw new FirmwareVersionIsDeprecatedException(thesaurus);
        }
    }
}
