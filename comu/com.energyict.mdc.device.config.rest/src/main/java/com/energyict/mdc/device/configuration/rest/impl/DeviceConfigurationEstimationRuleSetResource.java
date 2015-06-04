package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/estimationrulesets")
public class DeviceConfigurationEstimationRuleSetResource {

    private final EstimationService estimationService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceConfigurationEstimationRuleSetResource(EstimationService estimationService, DeviceConfigurationService deviceConfigurationService) {
        this.estimationService = estimationService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Path("/{ruleSetId}/deviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getDeviceConfigurationsForEstimationRuleSet(@PathParam("ruleSetId") long estimationRuleSetId, @BeanParam JsonQueryParameters queryParameters) {
        EstimationRuleSet estimationRuleSet = findEstimationRuleSetByIdOrThrowException(estimationRuleSetId);
        List<DeviceConfiguration> deviceConfigurations = deviceConfigurationService.findDeviceConfigurationsForEstimationRuleSet(estimationRuleSet).from(queryParameters).find();
        List<DeviceConfigurationRefInfo> infos = deviceConfigurations.stream().map(DeviceConfigurationRefInfo::from).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceConfigurations", infos, queryParameters);
    }
    
    @GET
    @Path("{ruleSetId}/linkabledeviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getLinkableDeviceConfigurations(@PathParam("ruleSetId") long estimationRuleSetId, @BeanParam JsonQueryParameters queryParameters) {
        EstimationRuleSet estimationRuleSet = findEstimationRuleSetByIdOrThrowException(estimationRuleSetId);
        List<DeviceConfiguration> linkableDeviceConfigs = computeLinkableDeviceConfigs(estimationRuleSet);
        List<DeviceConfiguration> pagedLinkableDeviceConfigs = ListPager.of(linkableDeviceConfigs).from(queryParameters).find();
        List<DeviceConfigurationRefInfo> infos = pagedLinkableDeviceConfigs.stream().map(DeviceConfigurationRefInfo::from).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceConfigurations", infos, queryParameters);
    }
    
    @POST
    @Path("/{ruleSetId}/deviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response addDeviceConfigurationsToEstimationRuleSet(@PathParam("ruleSetId") long estimationRuleSetId, @QueryParam("all") boolean includeAll, List<DeviceConfigurationRefInfo> deviceConfigs) {
        EstimationRuleSet estimationRuleSet = findEstimationRuleSetByIdOrThrowException(estimationRuleSetId);
        if (includeAll) {
            for (DeviceConfiguration deviceConfig : computeLinkableDeviceConfigs(estimationRuleSet)) {
                addRuleSetToDeviceConfig(estimationRuleSet, deviceConfig.getId(), deviceConfig.getVersion());
            }
        } else {
            for (DeviceConfigurationRefInfo deviceConfigInfo : deviceConfigs) {
                addRuleSetToDeviceConfig(estimationRuleSet, deviceConfigInfo.id, deviceConfigInfo.version);
            }
        }
        return Response.status(Status.CREATED).build();
    }
    
    private List<DeviceConfiguration> computeLinkableDeviceConfigs(EstimationRuleSet estimationRuleSet) {
        Set<ReadingType> readingTypesInRuleSet = readingTypesFor(estimationRuleSet);
        return deviceConfigurationService.findAllDeviceTypes().stream()
            .flatMap(deviceType -> deviceType.getConfigurations().stream())
            .filter(deviceConfig -> !areLinked(deviceConfig, estimationRuleSet))
            .filter(deviceConfig -> haveCommonReadingTypes(readingTypesInRuleSet, readingTypesFor(deviceConfig)))
            .sorted((dc1, dc2) -> dc1.getName().compareToIgnoreCase(dc2.getName()))
            .collect(Collectors.toList());
    }

    private Set<ReadingType> readingTypesFor(EstimationRuleSet estimationRuleSet) {
        Set<ReadingType> readingTypesInRuleSet = new HashSet<>();
        estimationRuleSet.getRules().stream().flatMap(r -> r.getReadingTypes().stream()).distinct().forEach(readingType -> {
            readingTypesInRuleSet.add(readingType);
            if (readingType.isCumulative()) {
                readingType.getCalculatedReadingType().ifPresent(readingTypesInRuleSet::add);
            }
        });
        return readingTypesInRuleSet;
    }
    
    private List<ReadingType> readingTypesFor(DeviceConfiguration configuration) {
        return deviceConfigurationService.getReadingTypesRelatedToConfiguration(configuration);
    }
    
    private boolean haveCommonReadingTypes(Set<ReadingType> readingTypesInRuleSet, List<ReadingType> readingTypes) {
        return !Collections.disjoint(readingTypesInRuleSet, readingTypes);
    }

    private void addRuleSetToDeviceConfig(EstimationRuleSet estimationRuleSet, long deviceConfigId, long deviceConfigVersion) {
        DeviceConfiguration deviceConfiguration = deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(deviceConfigId, deviceConfigVersion)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        deviceConfiguration.addEstimationRuleSet(estimationRuleSet);
        deviceConfiguration.save();
    }

    private EstimationRuleSet findEstimationRuleSetByIdOrThrowException(long estimationRuleSetId) {
        return estimationService.getEstimationRuleSet(estimationRuleSetId).orElseThrow(() -> new WebApplicationException(Status.NOT_FOUND));
    }
    
    private boolean areLinked(DeviceConfiguration deviceConfiguration, EstimationRuleSet estimationRuleSet) {
        return deviceConfiguration.getEstimationRuleSets().stream().filter(rs -> rs.getId() == estimationRuleSet.getId()).findAny().isPresent();
    }
}
