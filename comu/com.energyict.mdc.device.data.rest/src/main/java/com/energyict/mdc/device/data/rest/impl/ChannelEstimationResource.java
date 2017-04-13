/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.ChannelEstimationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.security.Privileges;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ChannelEstimationResource {

    private final PropertyValueInfoService propertyValueInfoService;
    private final Clock clock;

    private final ChannelEstimationRuleInfoFactory channelEstimationRuleInfoFactory;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public ChannelEstimationResource(PropertyValueInfoService propertyValueInfoService, Clock clock, ChannelEstimationRuleInfoFactory channelEstimationRuleInfoFactory,
                                     ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory, ResourceHelper resourceHelper) {
        this.propertyValueInfoService = propertyValueInfoService;
        this.clock = clock;
        this.channelEstimationRuleInfoFactory = channelEstimationRuleInfoFactory;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_DEVICE,
            Privileges.Constants.ADMINISTRATE_DEVICE,
            Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION
    })
    public Map<String, Object> getChannelEstimationConfiguration(@PathParam("name") String name, @PathParam("channelid") long channelId,
                                                                 @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        Map<String, Object> info = new HashMap<>();
        ReadingType mainReadingType = channel.getReadingType();
        info.put("rulesForCollectedReadingType", getReadingTypeEstimationConfigurationInfos(device, mainReadingType));
        channel.getCalculatedReadingType(clock.instant()).ifPresent(calculatedReadingType ->
                info.put("rulesForCalculatedReadingType", getReadingTypeEstimationConfigurationInfos(device, calculatedReadingType)));
        return info;
    }

    private List<ChannelEstimationRuleInfo> getReadingTypeEstimationConfigurationInfos(Device device, ReadingType readingType) {
        return device.getDeviceConfiguration().getEstimationRuleSets()
                .stream()
                .flatMap(estimationRuleSet -> estimationRuleSet.getRules(Collections.singleton(readingType)).stream())
                .map(estimationRule -> asInfo(estimationRule, readingType, device.forEstimation()))
                .collect(Collectors.toMap(Function.identity(), Function.identity(), ChannelEstimationRuleInfo::chooseEffectiveOne)).values().stream()
                .sorted(ChannelEstimationRuleInfo.defaultComparator())
                .collect(Collectors.toList());
    }

    private ChannelEstimationRuleInfo asInfo(EstimationRule estimationRule, ReadingType readingType, DeviceEstimation deviceEstimation) {
        return deviceEstimation.findOverriddenProperties(estimationRule, readingType)
                .map(overriddenProperties -> channelEstimationRuleInfoFactory.createInfoForRule(estimationRule, readingType, overriddenProperties))
                .orElseGet(() -> channelEstimationRuleInfoFactory.createInfoForRule(estimationRule, readingType));
    }

    @GET
    @Path("/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_DEVICE,
            Privileges.Constants.ADMINISTRATE_DEVICE,
            Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION
    })
    public ChannelEstimationRuleInfo getChannelEstimationRuleById(@PathParam("name") String name, @PathParam("channelid") long channelId, @PathParam("ruleId") long ruleId,
                                                                  @QueryParam("readingType") String readingTypeMrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        ReadingType readingType = resourceHelper.findChannelReadingTypeOrThrowException(channel, readingTypeMrid);
        EstimationRule estimationRule = resourceHelper.findEstimationRuleOrThrowException(ruleId);
        return asInfo(estimationRule, readingType, device.forEstimation());
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION})
    public Response overrideChannelEstimationRuleProperties(@PathParam("name") String name, @PathParam("channelid") long channelId,
                                                            ChannelEstimationRuleInfo channelEstimationRuleInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        ReadingType readingType = resourceHelper.findChannelReadingTypeOrThrowException(channel, channelEstimationRuleInfo.readingType.mRID);
        EstimationRule estimationRule = resourceHelper.findEstimationRuleOrThrowException(channelEstimationRuleInfo.ruleId);

        DeviceEstimation.PropertyOverrider propertyOverrider = device.forEstimation().overridePropertiesFor(estimationRule, readingType);
        estimationRule.getPropertySpecs(EstimationPropertyDefinitionLevel.TARGET_OBJECT).stream()
                .map(propertySpec -> {
                    String propertyName = propertySpec.getName();
                    Object propertyValue = propertyValueInfoService.findPropertyValue(propertySpec, toPropertyInfoList(channelEstimationRuleInfo.properties));
                    return Pair.of(propertyName, propertyValue);
                })
                .filter(property -> property.getLast() != null && !"".equals(property.getLast()))
                .forEach(property -> propertyOverrider.override(property.getFirst(), property.getLast()));
        propertyOverrider.complete();

        return Response.ok().build();
    }

    private List<PropertyInfo> toPropertyInfoList(Collection<OverriddenPropertyInfo> infos) {
        return infos.stream().map(info -> info.propertyInfo).collect(Collectors.toList());
    }

    @PUT
    @Path("/{ruleId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION})
    public Response editChannelEstimationRuleOverriddenProperties(@PathParam("name") String name, @PathParam("channelid") long channelId,
                                                                  @PathParam("ruleId") long ruleId, ChannelEstimationRuleInfo channelEstimationRuleInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        ReadingType readingType = resourceHelper.findChannelReadingTypeOrThrowException(channel, channelEstimationRuleInfo.readingType.mRID);
        EstimationRule estimationRule = resourceHelper.findEstimationRuleOrThrowException(channelEstimationRuleInfo.ruleId);
        DeviceEstimation deviceEstimation = device.forEstimation();
        ChannelEstimationRuleOverriddenProperties channelEstimationRule = deviceEstimation
                .findAndLockChannelEstimationRuleOverriddenProperties(channelEstimationRuleInfo.id, channelEstimationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(estimationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelEstimationRule(deviceEstimation, estimationRule, readingType)).supplier());

        Map<String, Object> overriddenProperties = estimationRule.getPropertySpecs(EstimationPropertyDefinitionLevel.TARGET_OBJECT)
                .stream()
                .map(propertySpec -> {
                    String propertyName = propertySpec.getName();
                    Object propertyValue = propertyValueInfoService.findPropertyValue(propertySpec, toPropertyInfoList(channelEstimationRuleInfo.properties));
                    return Pair.of(propertyName, propertyValue);
                })
                .filter(property -> property.getLast() != null && !"".equals(property.getLast()))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        channelEstimationRule.setProperties(overriddenProperties);
        channelEstimationRule.update();

        return Response.ok().build();
    }

    @DELETE
    @Path("/{ruleId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE})
    public Response restoreChannelEstimationRuleOverriddenProperties(@PathParam("name") String name, @PathParam("channelid") long channelId,
                                                                     @PathParam("ruleId") long ruleId, ChannelEstimationRuleInfo channelEstimationRuleInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Channel channel = resourceHelper.findChannelOnDeviceOrThrowException(device, channelId);
        ReadingType readingType = resourceHelper.findChannelReadingTypeOrThrowException(channel, channelEstimationRuleInfo.readingType.mRID);
        EstimationRule estimationRule = resourceHelper.findEstimationRuleOrThrowException(channelEstimationRuleInfo.ruleId);
        DeviceEstimation deviceEstimation = device.forEstimation();
        ChannelEstimationRuleOverriddenProperties channelEstimationRule = deviceEstimation
                .findAndLockChannelEstimationRuleOverriddenProperties(channelEstimationRuleInfo.id, channelEstimationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(estimationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelEstimationRule(deviceEstimation, estimationRule, readingType)).supplier());
        channelEstimationRule.delete();
        return Response.noContent().build();
    }

    private Supplier<Long> getActualVersionOfChannelEstimationRule(DeviceEstimation deviceEstimation, EstimationRule estimationRule, ReadingType readingType) {
        return () -> deviceEstimation.findOverriddenProperties(estimationRule, readingType)
                .map(ChannelEstimationRuleOverriddenProperties::getVersion)
                .orElse(null);
    }
}
