package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceConfigConflictMappingResource {

    private final Thesaurus thesaurus;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceConfigConflictMappingResource(Thesaurus thesaurus, DeviceConfigurationService deviceConfigurationService) {
        this.thesaurus = thesaurus;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public PagedInfoList getDeviceConfigConflictMappingsForDeviceType(@PathParam("deviceTypeId") long id, @QueryParam("all") boolean hasAll, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(id)
                .orElseThrow(() -> new WebApplicationException("No device with id " + id, Response.Status.NOT_FOUND));
        List<DeviceConfigConflictMappingInfo> deviceConfigConflictMappingInfos = DeviceConfigConflictMappingInfo.from(deviceType.getDeviceConfigConflictMappings(), thesaurus);
        return PagedInfoList.fromCompleteList("conflictMapping",
                hasAll ? deviceConfigConflictMappingInfos : deviceConfigConflictMappingInfos.stream().filter(solve -> !solve.isSolved).collect(Collectors.toList()),
                queryParameters);
    }

    @GET
    @Path("/{conflictId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public DeviceConfigSolutionMappingInfo getDeviceConfigConflictById(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("conflictId") long conflictId) {
        return new DeviceConfigSolutionMappingInfo(getDeviceConfigConflictById(conflictId));
    }

    @PUT
    @Path("/{conflictId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response editConflictForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("conflictId") long conflictId,
            DeviceConfigSolutionMappingInfo deviceConfigConflictInfo) {
        DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictById(conflictId);
        deviceConfigConflictInfo.connectionMethodSolutions.forEach(deviceConfigConnectionMethodSolution -> {
            ConflictingConnectionMethodSolution from = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions()
                    .stream()
                    .filter(f -> f.getOriginDataSource().getId() == deviceConfigConnectionMethodSolution.from.id)
                    .findFirst()
                    .orElseThrow(() -> new WebApplicationException("No device configuration conflict solution for original data source with id " + deviceConfigConnectionMethodSolution.from.id, Response.Status.NOT_FOUND));
            if (deviceConfigConnectionMethodSolution.action.equals(DeviceConfigConflictMapping.ConflictingMappingAction.MAP.toString())) {
                PartialConnectionTask to = from.getMappableToDataSources()
                        .stream()
                        .filter(f -> f.getId() == deviceConfigConnectionMethodSolution.to.id)
                        .findFirst()
                        .orElseThrow(() -> new WebApplicationException("No device configuration conflict solution for destination data source with id " + deviceConfigConnectionMethodSolution.from.id, Response.Status.NOT_FOUND));
                from.markSolutionAsMap(to);
            } else {
                from.markSolutionAsRemove();
            }
        });
        deviceConfigConflictInfo.securitySetSolutions.forEach(deviceConfigSecuritySetSolution -> {
            ConflictingSecuritySetSolution from = deviceConfigConflictMapping.getConflictingSecuritySetSolutions()
                    .stream()
                    .filter(f -> f.getOriginDataSource().getId() == deviceConfigSecuritySetSolution.from.id)
                    .findFirst()
                    .orElseThrow(() -> new WebApplicationException("No device configuration security set solution for original data source with id " + deviceConfigSecuritySetSolution.from.id, Response.Status.NOT_FOUND));
            if (deviceConfigSecuritySetSolution.action.equals(DeviceConfigConflictMapping.ConflictingMappingAction.MAP.toString())) {
                SecurityPropertySet to = from.getMappableToDataSources()
                        .stream()
                        .filter(f -> f.getId() == deviceConfigSecuritySetSolution.to.id)
                        .findFirst()
                        .orElseThrow(() -> new WebApplicationException("No device configuration security set solution for destination data source with id " + deviceConfigSecuritySetSolution.from.id, Response.Status.NOT_FOUND));
                from.markSolutionAsMap(to);
            } else {
                from.markSolutionAsRemove();
            }
        });
        return Response.ok().build();
    }

    private DeviceConfigConflictMapping getDeviceConfigConflictById(long id) {
        return deviceConfigurationService.findDeviceConfigConflictMapping(id)
                .orElseThrow(() -> new WebApplicationException("No device configuration conflict with id " + id, Response.Status.NOT_FOUND));
    }
}