package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.util.KorePagedInfoList;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.util.collections.KPermutation;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
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
    
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public KorePagedInfoList getEstimationRuleSets(@PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @QueryParam("linkable") boolean includeOnlyLinkableEstimationRuleSets,
            @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<EstimationRuleSet> ruleSets = includeOnlyLinkableEstimationRuleSets ? getLinkableEstimationRuleSets(deviceConfiguration) : deviceConfiguration.getEstimationRuleSets();
        List<EstimationRuleSet> pagedRuleSets = ListPager.of(ruleSets).from(queryParameters).find();
        List<EstimationRuleSetRefInfo> infos = pagedRuleSets.stream().map(ruleSet -> new EstimationRuleSetRefInfo(ruleSet, deviceConfiguration)).collect(Collectors.toList());
        return KorePagedInfoList.asJson("estimationRuleSets", infos, queryParameters);
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response addEstimationRuleSetsToDeviceConfiguration(@PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @QueryParam("all") boolean includeAll, List<EstimationRuleSetRefInfo> ruleSets) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        if (includeAll) {
            DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
            for (EstimationRuleSet ruleSet : getLinkableEstimationRuleSets(deviceConfiguration)) {
                deviceConfiguration.addEstimationRuleSet(ruleSet);
            }
            deviceConfiguration.save();
        } else if (!ruleSets.isEmpty()) {
            DeviceConfiguration deviceConfiguration = deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(ruleSets.get(0).parent.id, ruleSets.get(0).parent.version).orElseThrow(() -> new WebApplicationException(Status.CONFLICT));
            for (EstimationRuleSetRefInfo info : ruleSets) {
                EstimationRuleSet ruleSet = estimationService.getEstimationRuleSet(info.id).orElseThrow(() -> new WebApplicationException(Status.NOT_FOUND));
                deviceConfiguration.addEstimationRuleSet(ruleSet);
            }
            deviceConfiguration.save();
        }
        return Response.status(Status.CREATED).build();
    }
    
    @DELETE
    @Path("/{estimationRuleSetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response deleteEstimationRuleSetFromDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("estimationRuleSetId") long estimationRuleSetId,
            EstimationRuleSetRefInfo estimationRuleSetRefInfo) {
        resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(deviceConfigurationId, estimationRuleSetRefInfo.parent.version)
                .orElseThrow(() -> new WebApplicationException(Status.CONFLICT));
        EstimationRuleSet estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Status.NOT_FOUND));
        deviceConfiguration.removeEstimationRuleSet(estimationRuleSet);
        deviceConfiguration.save();
        return Response.status(Status.NO_CONTENT).build();
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response reorderEstimationRuleSetOnDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            List<EstimationRuleSetRefInfo> estimationRuleSetRefInfos) {
        resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        if (estimationRuleSetRefInfos.isEmpty()) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        DeviceConfiguration deviceConfiguration = deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(deviceConfigurationId, estimationRuleSetRefInfos.get(0).parent.version)
                .orElseThrow(() -> new WebApplicationException(Status.CONFLICT));

        long[] current = deviceConfiguration.getEstimationRuleSets().stream().mapToLong(EstimationRuleSet::getId).toArray();
        long[] target = estimationRuleSetRefInfos.stream().mapToLong(ruleSetRefInfo -> ruleSetRefInfo.id).toArray();
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
                .sorted((rs1, rs2) -> rs1.getName().compareToIgnoreCase(rs2.getName()))
                .collect(Collectors.<EstimationRuleSet>toList());
    }
}
