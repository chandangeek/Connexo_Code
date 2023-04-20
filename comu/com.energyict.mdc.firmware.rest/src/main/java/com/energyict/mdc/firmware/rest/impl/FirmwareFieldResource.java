/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.device.config.DeviceConfigConstants;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/field")
public class FirmwareFieldResource extends FieldResource {

    private final FirmwareService firmwareService;
    private final ResourceHelper resourceHelper;
    private final FirmwareMessageInfoFactory firmwareMessageInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public FirmwareFieldResource(Thesaurus thesaurus, FirmwareService firmwareService, ResourceHelper resourceHelper, FirmwareMessageInfoFactory firmwareMessageInfoFactory,
                                 ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService) {
        super(thesaurus);
        this.firmwareService = firmwareService;
        this.resourceHelper = resourceHelper;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Transactional
    @Path("/firmwareStatuses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Object getFirmwareStatuses() {
        return asJsonArrayObjectWithTranslation("firmwareStatuses", "id", Stream.of(FirmwareStatus.values()).map(FirmwareStatus::getStatus).collect(Collectors.toList()));
    }

    /**
     * TODO: how is this method used? Is it needed and up-to-date? (see similar {@link FirmwareTypesResource#getSupportedFirmwareTypes(long)})
     */
    @GET
    @Transactional
    @Path("/firmwareTypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Object getFirmwareTypes(@QueryParam("deviceType") Long deviceTypeId) {
        List<String> firmwareTypes = new FirmwareTypeFieldAdapter().getClientSideValues();
        if (deviceTypeId != null) {
            DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
            if (deviceType.getDeviceProtocolPluggableClass().isPresent()) {
                if (!deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol().supportsCommunicationFirmwareVersion()) {
                    firmwareTypes.remove(FirmwareType.COMMUNICATION.getType());
                }
                if (!deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol().supportsAuxiliaryFirmwareVersion()) {
                    firmwareTypes.remove(FirmwareType.AUXILIARY.getType());
                }
            }
        }
        return asJsonArrayObjectWithTranslation("firmwareTypes", "id", firmwareTypes);
    }

    @GET
    @Transactional
    @Path("/devicetypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE})
    public Response getDeviceTypesWhichSupportFirmwareUpgrade() {
        List<IdWithLocalizedValue> deviceTypes = firmwareService.getDeviceTypesWhichSupportFirmwareManagement()
                .stream()
                .map(IdWithLocalizedValue::from)
                .collect(Collectors.toList());
        return Response.ok(deviceTypes).build();
    }

    @GET
    @Transactional
    @Path("/devicetypes/{deviceTypeId}/{firmwareOption}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE})
    public Response getUploadOptionSpecForDeviceType(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("firmwareOption") String firmwareOption, @QueryParam("firmwareType") String firmwareType) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        DeviceMessageSpec firmwareMessageSpec = resourceHelper.findFirmwareMessageSpecOrThrowException(deviceType, firmwareOption);
        return Response.ok(firmwareMessageInfoFactory.from(firmwareMessageSpec, deviceType, firmwareOption, firmwareType)).build();
    }

    @GET
    @Transactional
    @Path("/devicetypes/{deviceTypeId}/firmwares")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getFilteredFirmwareVersions(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        FirmwareVersionFilter fwFilter = resourceHelper.getFirmwareFilter(filter, deviceType);
        List<IdWithNameInfo> foundFirmwares = firmwareService.findAllFirmwareVersions(fwFilter)
                .from(queryParameters)
                .stream()
                .map(fw -> new IdWithNameInfo(fw.getId(), fw.getFirmwareVersion()))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("firmwares", foundFirmwares, queryParameters);
    }

    @GET
    @Transactional
    @Path("/firmwares/{id}/previous")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getFilteredPreviousFirmwareVersions(@PathParam("id") long id, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(id);
        FirmwareVersionFilter fwFilter = resourceHelper.getFirmwareFilter(filter, firmwareVersion.getDeviceType());
        fwFilter.setRankRange(Range.lessThan(firmwareVersion.getRank()));
        List<IdWithNameInfo> foundFirmwares = firmwareService.findAllFirmwareVersions(fwFilter)
                .from(queryParameters)
                .stream()
                .map(fw -> new IdWithNameInfo(fw.getId(), fw.getFirmwareVersion()))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("firmwares", foundFirmwares, queryParameters);
    }


    @GET
    @Transactional
    @Path("/comtasks")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response getComTasks(@QueryParam("type") long deviceTypeId) {

        Set<IdWithNameInfo> comTasks = new TreeSet<>(Comparator.comparing(IdWithNameInfo::getName));

        deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId))
                .getConfigurations().stream()
                .flatMap(cnf -> cnf.getComTaskEnablements().stream())
                .filter(cte -> cte.getComTask().isManualSystemTask())
                .forEach(comTaskEnb -> comTasks.add(new IdWithNameInfo(comTaskEnb.getComTask().getId(), comTaskEnb.getComTask().getName())));

        return Response.ok(comTasks).build();
    }

    @GET
    @Transactional
    @Path("/firmwareuploadcomtasks")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response getFirmwareUploadComTasks(@QueryParam("type") long deviceTypeId) {

        Set<IdWithNameInfo> comTasks = new TreeSet<>(Comparator.comparing(IdWithNameInfo::getName));

        deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId))
                .getConfigurations().stream()
                .flatMap(cnf -> cnf.getComTaskEnablements().stream())
                .filter(cte -> cte.getComTask().getProtocolTasks().stream().anyMatch(FirmwareManagementTask.class::isInstance))
                .forEach(comTaskEnb -> comTasks.add(new IdWithNameInfo(comTaskEnb.getComTask().getId(), comTaskEnb.getComTask().getName())));

        return Response.ok(comTasks).build();
    }

}
