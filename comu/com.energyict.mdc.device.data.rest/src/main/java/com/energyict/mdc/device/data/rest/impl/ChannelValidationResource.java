/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ChannelValidationResource {

    private final PropertyValueInfoService propertyValueInfoService;

    private final ChannelValidationRuleInfoFactory channelValidationRuleInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;
    private final ResourceHelper resourceHelper;

    private Function<Device, ReadingType> collectedReadingTypeProvider;
    private Function<Device, Optional<ReadingType>> calculatedReadingTypeProvider;

    @Inject
    public ChannelValidationResource(PropertyValueInfoService propertyValueInfoService, ChannelValidationRuleInfoFactory channelValidationRuleInfoFactory,
                                     ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory,
                                     ResourceHelper resourceHelper) {
        this.propertyValueInfoService = propertyValueInfoService;
        this.channelValidationRuleInfoFactory = channelValidationRuleInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
        this.resourceHelper = resourceHelper;
    }

    ChannelValidationResource init(Function<Device, ReadingType> collectedReadingTypeProvider, Function<Device, Optional<ReadingType>> calculatedReadingTypeProvider) {
        this.collectedReadingTypeProvider = collectedReadingTypeProvider;
        this.calculatedReadingTypeProvider = calculatedReadingTypeProvider;
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_DEVICE,
            Privileges.Constants.ADMINISTRATE_DEVICE,
            Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION
    })
    public Map<String, Object> getChannelValidationConfiguration(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Map<String, Object> info = new HashMap<>();
        ReadingType collectedReadingType = this.collectedReadingTypeProvider.apply(device);
        info.put("rulesForCollectedReadingType", getReadingTypeValidationConfigurationInfos(device, collectedReadingType));
        this.calculatedReadingTypeProvider.apply(device).ifPresent(calculatedReadingType ->
                info.put("rulesForCalculatedReadingType", getReadingTypeValidationConfigurationInfos(device, calculatedReadingType)));
        return info;
    }

    private List<ChannelValidationRuleInfo> getReadingTypeValidationConfigurationInfos(Device device, ReadingType readingType) {
        return device.getDeviceConfiguration()
                .getValidationRules(Collections.singleton(readingType))
                .stream()
                .map(validationRule -> asInfo(validationRule, readingType, device.forValidation()))
                .collect(Collectors.toMap(Function.identity(), Function.identity(), ChannelValidationRuleInfo::chooseEffectiveOne)).values().stream()
                .sorted(ChannelValidationRuleInfo.defaultComparator())
                .collect(Collectors.toList());
    }

    private ChannelValidationRuleInfo asInfo(ValidationRule validationRule, ReadingType readingType, DeviceValidation deviceValidation) {
        return deviceValidation.findOverriddenProperties(validationRule, readingType)
                .map(overriddenProperties -> channelValidationRuleInfoFactory.createInfoForRule(validationRule, readingType, overriddenProperties))
                .orElseGet(() -> channelValidationRuleInfoFactory.createInfoForRule(validationRule, readingType));
    }

    @GET
    @Path("/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_DEVICE,
            Privileges.Constants.ADMINISTRATE_DEVICE,
            Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION
    })
    public ChannelValidationRuleInfo getChannelValidationRuleById(@PathParam("name") String name, @PathParam("ruleId") long ruleId, @QueryParam("readingType") String readingTypeMrid) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ReadingType readingType = this.findReadingTypeOrThrowException(device, readingTypeMrid);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(ruleId);
        validateRuleApplicability(validationRule, readingType);
        return asInfo(validationRule, readingType, device.forValidation());
    }

    private ReadingType findReadingTypeOrThrowException(Device device, String readingTypeMrid) {
        ReadingType collected = this.collectedReadingTypeProvider.apply(device);
        if (collected.getMRID().equals(readingTypeMrid)) {
            return collected;
        }
        return this.calculatedReadingTypeProvider.apply(device)
                .filter(readingType -> readingType.getMRID().equals(readingTypeMrid))
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_READINGTYPE_ON_CHANNEL, readingTypeMrid));
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION})
    public Response overrideChannelValidationRuleProperties(@PathParam("name") String name, ChannelValidationRuleInfo channelValidationRuleInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ReadingType readingType = this.findReadingTypeOrThrowException(device, channelValidationRuleInfo.readingType.mRID);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        validateRuleApplicability(validationRule, readingType);

        DeviceValidation.PropertyOverrider propertyOverrider = device.forValidation().overridePropertiesFor(validationRule, readingType);
        validationRule.getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT).stream()
                .map(propertySpec -> {
                    String propertyName = propertySpec.getName();
                    Object propertyValue = propertyValueInfoService.findPropertyValue(propertySpec, toPropertyInfoList(channelValidationRuleInfo.properties));
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
    @RolesAllowed({Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION})
    public Response editChannelValidationRuleOverriddenProperties(@PathParam("name") String name, @PathParam("ruleId") long ruleId,
                                                                  ChannelValidationRuleInfo channelValidationRuleInfo) {
        channelValidationRuleInfo.ruleId = ruleId;
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ReadingType readingType = this.findReadingTypeOrThrowException(device, channelValidationRuleInfo.readingType.mRID);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        validateRuleApplicability(validationRule, readingType);
        DeviceValidation deviceValidation = device.forValidation();
        ChannelValidationRuleOverriddenProperties channelValidationRule = deviceValidation
                .findAndLockChannelValidationRuleOverriddenProperties(channelValidationRuleInfo.id, channelValidationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(validationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelValidationRule(deviceValidation, validationRule, readingType)).supplier());

        Map<String, Object> overriddenProperties = validationRule.getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT)
                .stream()
                .map(propertySpec -> {
                    String propertyName = propertySpec.getName();
                    Object propertyValue = propertyValueInfoService.findPropertyValue(propertySpec, toPropertyInfoList(channelValidationRuleInfo.properties));
                    return Pair.of(propertyName, propertyValue);
                })
                .filter(property -> property.getLast() != null && !"".equals(property.getLast()))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        channelValidationRule.setProperties(overriddenProperties);
        channelValidationRule.update();

        return Response.ok().build();
    }

    @DELETE
    @Path("/{ruleId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION})
    public Response restoreChannelValidationRuleOverriddenProperties(@PathParam("name") String name, @PathParam("ruleId") long ruleId,
                                                                     ChannelValidationRuleInfo channelValidationRuleInfo) {
        channelValidationRuleInfo.ruleId = ruleId;
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ReadingType readingType = this.findReadingTypeOrThrowException(device, channelValidationRuleInfo.readingType.mRID);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        validateRuleApplicability(validationRule, readingType);
        DeviceValidation deviceValidation = device.forValidation();
        ChannelValidationRuleOverriddenProperties channelValidationRule = deviceValidation
                .findAndLockChannelValidationRuleOverriddenProperties(channelValidationRuleInfo.id, channelValidationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(validationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelValidationRule(deviceValidation, validationRule, readingType)).supplier());
        channelValidationRule.delete();
        return Response.noContent().build();
    }

    private void validateRuleApplicability(ValidationRule validationRule, ReadingType readingType) {
        if (!validationRule.getReadingTypes().contains(readingType)) {
            throw exceptionFactory.newException(MessageSeeds.VALIDATION_RULE_IS_NOT_APPLICABLE_TO_READINGTYPE, validationRule.getId(), readingType.getFullAliasName());
        }
    }

    private Supplier<Long> getActualVersionOfChannelValidationRule(DeviceValidation deviceValidation, ValidationRule validationRule, ReadingType readingType) {
        return () -> deviceValidation.findOverriddenProperties(validationRule, readingType)
                .map(ChannelValidationRuleOverriddenProperties::getVersion)
                .orElse(null);
    }
}
