/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.ChannelValidationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRule;

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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UsagePointOutputValidationResource {

    private final UsagePointDataModelService usagePointDataModelService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final PropertyValueInfoService propertyValueInfoService;

    private final ChannelValidationRuleInfoFactory channelValidationRuleInfoFactory;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    UsagePointOutputValidationResource(UsagePointDataModelService usagePointDataModelService, UsagePointConfigurationService usagePointConfigurationService,
                                       PropertyValueInfoService propertyValueInfoService, ChannelValidationRuleInfoFactory channelValidationRuleInfoFactory,
                                       ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory, ResourceHelper resourceHelper) {
        this.usagePointDataModelService = usagePointDataModelService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.channelValidationRuleInfoFactory = channelValidationRuleInfoFactory;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public PagedInfoList getUsagePointChannelValidationConfiguration(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                     @PathParam("outputId") long outputId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        ReadingType readingType = deliverable.getReadingType();
        UsagePointValidation usagePointValidation = usagePointDataModelService.forValidation(usagePoint);
        List<ChannelValidationRuleInfo> infos = usagePointConfigurationService.getValidationRuleSets(deliverable.getMetrologyContract())
                .stream()
                .flatMap(validationRuleSet -> validationRuleSet.getRuleSetVersions().stream())
                .flatMap(validationRuleSetVersion -> validationRuleSetVersion.getRules(Collections.singleton(readingType)).stream())
                .map(validationRule -> asInfo(validationRule, readingType, usagePointValidation))
                .collect(Collectors.toMap(Function.identity(), Function.identity(), ChannelValidationRuleInfo::chooseEffectiveOne)).values().stream()
                .sorted(ChannelValidationRuleInfo.defaultComparator())
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("validation", infos, queryParameters);
    }

    private ChannelValidationRuleInfo asInfo(ValidationRule validationRule, ReadingType readingType, UsagePointValidation usagePointValidation) {
        return usagePointValidation.findOverriddenProperties(validationRule, readingType)
                .map(overriddenProperties -> channelValidationRuleInfoFactory.createInfoForRule(validationRule, readingType, overriddenProperties))
                .orElseGet(() -> channelValidationRuleInfoFactory.createInfoForRule(validationRule, readingType));
    }

    @GET
    @Path("/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public ChannelValidationRuleInfo getUsagePointChannelValidationRuleById(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                            @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                                            @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(ruleId);
        ReadingType readingType = deliverable.getReadingType();
        UsagePointValidation usagePointValidation = usagePointDataModelService.forValidation(usagePoint);
        return asInfo(validationRule, readingType, usagePointValidation);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public Response overrideChannelValidationRuleProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                            @PathParam("outputId") long outputId, ChannelValidationRuleInfo channelValidationRuleInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingType readingType = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId).getReadingType();
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        UsagePointValidation usagePointValidation = usagePointDataModelService.forValidation(usagePoint);
        UsagePointValidation.PropertyOverrider propertyOverrider = usagePointValidation.overridePropertiesFor(validationRule, readingType);

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

    @PUT
    @Path("/{ruleId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public Response editChannelValidationRuleOverriddenProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                  @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                                  ChannelValidationRuleInfo channelValidationRuleInfo) {
        channelValidationRuleInfo.ruleId = ruleId;
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingType readingType = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId).getReadingType();
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        UsagePointValidation usagePointValidation = usagePointDataModelService.forValidation(usagePoint);
        ChannelValidationRuleOverriddenProperties channelValidationRule = usagePointValidation
                .findAndLockChannelValidationRuleOverriddenProperties(channelValidationRuleInfo.id, channelValidationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(validationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelValidationRule(usagePointValidation, validationRule, readingType)).supplier());

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
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public Response restoreChannelValidationRuleProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                           @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                           ChannelValidationRuleInfo channelValidationRuleInfo) {
        channelValidationRuleInfo.ruleId = ruleId;
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingType readingType = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId).getReadingType();
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        UsagePointValidation usagePointValidation = usagePointDataModelService.forValidation(usagePoint);
        ChannelValidationRuleOverriddenProperties channelValidationRule = usagePointValidation
                .findAndLockChannelValidationRuleOverriddenProperties(channelValidationRuleInfo.id, channelValidationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(validationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelValidationRule(usagePointValidation, validationRule, readingType)).supplier());
        channelValidationRule.delete();
        return Response.noContent().build();
    }

    private List<PropertyInfo> toPropertyInfoList(Collection<OverriddenPropertyInfo> infos) {
        return infos.stream().map(info -> info.propertyInfo).collect(Collectors.toList());
    }

    private ReadingTypeDeliverable findReadingTypeDeliverableOrThrowException(UsagePoint usagePoint, long contractId, long outputId) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMetrologyConfiguration, contractId);
        return resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, usagePoint.getName());
    }

    private Supplier<Long> getActualVersionOfChannelValidationRule(UsagePointValidation usagePointValidation, ValidationRule validationRule, ReadingType readingType) {
        return () -> usagePointValidation.findOverriddenProperties(validationRule, readingType)
                .map(ChannelValidationRuleOverriddenProperties::getVersion)
                .orElse(null);
    }
}
