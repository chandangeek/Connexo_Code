/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.collections.KPermutation;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.configuration.rest.EstimationRuleSetRefInfo;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EstimationRuleSetResource {
    
    private final ResourceHelper resourceHelper;
    private final EstimationService estimationService;
    private final DeviceConfigurationService deviceConfigurationService;
    
    @Inject
    public EstimationRuleSetResource(ResourceHelper resourceHelper, EstimationService estimationService, DeviceConfigurationService deviceConfigurationService) {
        this.resourceHelper = resourceHelper;
        this.estimationService = estimationService;
        this.deviceConfigurationService = deviceConfigurationService;
    }
    
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getEstimationRuleSets(
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @QueryParam("linkable") boolean includeOnlyLinkableEstimationRuleSets,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<EstimationRuleSet> ruleSets = includeOnlyLinkableEstimationRuleSets ? getLinkableEstimationRuleSets(deviceConfiguration) : deviceConfiguration.getEstimationRuleSets();
        List<EstimationRuleSetRefInfo> infos = ruleSets.stream().map(ruleSet -> new EstimationRuleSetRefInfo(ruleSet, deviceConfiguration)).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("estimationRuleSets", infos, queryParameters);
    }
    
    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response addEstimationRuleSetsToDeviceConfiguration(@PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @QueryParam("all") boolean includeAll, List<EstimationRuleSetRefInfo> ruleSets) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        if (includeAll) {
            for (EstimationRuleSet ruleSet : getLinkableEstimationRuleSets(deviceConfiguration)) {
                deviceConfiguration.addEstimationRuleSet(ruleSet);
            }
        } else if (!ruleSets.isEmpty()) {
            for (EstimationRuleSetRefInfo info : ruleSets) {
                EstimationRuleSet ruleSet = resourceHelper.findEstimationRuleSetOrThrowException(info.id);
                deviceConfiguration.addEstimationRuleSet(ruleSet);
            }
        }
        deviceConfiguration.save();
        return Response.status(Status.CREATED).build();
    }
    
    @DELETE @Transactional
    @Path("/{estimationRuleSetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response deleteEstimationRuleSetFromDeviceConfiguration(
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("estimationRuleSetId") long estimationRuleSetId,
            EstimationRuleSetRefInfo info) {
        info.id = estimationRuleSetId;
        EstimationRuleSet estimationRuleSet = resourceHelper.lockEstimationRuleSetOrThrowException(info);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        deviceConfiguration.removeEstimationRuleSet(estimationRuleSet);
        return Response.status(Status.NO_CONTENT).build();
    }
    
    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response reorderEstimationRuleSetOnDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            EstimationRuleSetReorderInfo info) {
        if (info.ruleSets == null || info.ruleSets.isEmpty()) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        DeviceConfiguration deviceConfiguration = resourceHelper.lockDeviceConfigurationOrThrowException(info.parent);

        long[] current = deviceConfiguration.getEstimationRuleSets().stream().mapToLong(EstimationRuleSet::getId).toArray();
        long[] target = info.ruleSets.stream().mapToLong(ruleSetRefInfo -> ruleSetRefInfo.id).toArray();
        KPermutation kPermutation = KPermutation.of(current, target);
        if (!kPermutation.isNeutral(deviceConfiguration.getEstimationRuleSets())) {
            deviceConfiguration.reorderEstimationRuleSets(kPermutation);
            deviceConfiguration.save();
        }
        return Response.status(Status.OK).build();
    }
    
    private List<EstimationRuleSet> getLinkableEstimationRuleSets(DeviceConfiguration deviceConfiguration) {
        Set<ReadingType> relatedToConfiguration = deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration).stream().collect(Collectors.toSet());
        return estimationService.getEstimationRuleSets().stream()
                .filter(ruleSet -> !ruleSet.getRules(relatedToConfiguration).isEmpty())
                .filter(ruleSet -> !deviceConfiguration.getEstimationRuleSets().contains(ruleSet))
                .sorted(Comparator.comparing(EstimationRuleSet::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.<EstimationRuleSet>toList());
    }
}
