/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.impl;


import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/field")
public class DeviceGroupAndMetrologyContractResource {

    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public DeviceGroupAndMetrologyContractResource(MetrologyConfigurationService metrologyConfigurationService,
                                                   MeteringGroupsService meteringGroupsService) {

        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringGroupsService = meteringGroupsService;
    }

    @GET
    @Path("/metrologyconfigurations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public PagedInfoList getMetrologyConfigurations(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo> infos = metrologyConfigurationService.findAllMetrologyConfigurations()
                .stream()
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(mc -> new IdWithDisplayValueInfo<>(mc.getId(), mc.getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("metrologyConfigurations", infos, queryParameters);
    }

    @GET
    @Path("/metrologyconfigurations/{id}/contracts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public PagedInfoList getMetrologyContracts(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo> infos = metrologyConfigurationService.findMetrologyConfiguration(id).get().getContracts()
                .stream()
                .map(mc -> new IdWithDisplayValueInfo<>(mc.getId(), mc.getMetrologyPurpose().getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("metrologyContracts", infos, queryParameters);
    }

    @GET
    @Path("/purposes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public PagedInfoList getMetrologyPurposes(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo> infos = metrologyConfigurationService.getMetrologyPurposes()
                .stream()
                .map(mp -> new IdWithDisplayValueInfo<>(mp.getId(), mp.getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("metrologyPurposes", infos, queryParameters);
    }

    @GET
    @Path("/metergroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo> infos = meteringGroupsService.findEndDeviceGroups()
                .stream()
                .map(deviceGroup -> new IdWithDisplayValueInfo<>(deviceGroup.getId(), deviceGroup.getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("devicegroups", infos, queryParameters);
    }

    @GET
    @Path("/usagepointgroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public PagedInfoList getUsagePointGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo> infos = meteringGroupsService.findUsagePointGroups()
                .stream()
                .map(upg -> new IdWithDisplayValueInfo<>(upg.getId(), upg.getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("usagePointGroups", infos, queryParameters);
    }
}
