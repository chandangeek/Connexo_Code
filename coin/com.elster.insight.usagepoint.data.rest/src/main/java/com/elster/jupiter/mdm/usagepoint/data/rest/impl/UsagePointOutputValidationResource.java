/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.ChannelValidationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

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

    private final UsagePointService usagePointService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final PropertyValueInfoService propertyValueInfoService;

    private final ChannelValidationRuleInfoFactory channelValidationRuleInfoFactory;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;
    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;

    @Inject
    UsagePointOutputValidationResource(UsagePointService usagePointService, UsagePointConfigurationService usagePointConfigurationService,
                                       PropertyValueInfoService propertyValueInfoService, ChannelValidationRuleInfoFactory channelValidationRuleInfoFactory,
                                       ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory, ExceptionFactory exceptionFactory,
                                       ResourceHelper resourceHelper, ValidationService validationService) {
        this.usagePointService = usagePointService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.channelValidationRuleInfoFactory = channelValidationRuleInfoFactory;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION
    })
    public PagedInfoList getUsagePointChannelValidationConfiguration(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                     @PathParam("outputId") long outputId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        ReadingTypeDeliverable deliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, usagePoint.getName());
        ChannelsContainer channelsContainer = resourceHelper.findChannelsContainerOrThrowException(currentEffectiveMC, metrologyContract);
        ReadingType readingType = deliverable.getReadingType();
        UsagePointValidation usagePointValidation = usagePointService.forValidation(usagePoint);
        List<ChannelValidationRuleInfo> infos = usagePointConfigurationService.getValidationRuleSets(metrologyContract)
                .stream()
                .flatMap(validationRuleSet -> validationRuleSet.getRuleSetVersions().stream())
                .flatMap(validationRuleSetVersion -> validationRuleSetVersion.getRules(Collections.singleton(readingType)).stream())
                .map(validationRule -> asInfo(validationRule, readingType, usagePointValidation, metrologyContract, channelsContainer))
                .collect(Collectors.toMap(Function.identity(), Function.identity(), ChannelValidationRuleInfo::chooseEffectiveOne)).values().stream()
                .sorted(ChannelValidationRuleInfo.defaultComparator())
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("validation", infos, queryParameters);
    }

    private ChannelValidationRuleInfo asInfo(ValidationRule validationRule, ReadingType readingType, UsagePointValidation usagePointValidation, MetrologyContract metrologyContract, ChannelsContainer channelsContainer) {
        boolean validationActive = validationService.isValidationActive(channelsContainer);
        List<ValidationRuleSet> activeRuleSets = validationActive ?
                usagePointConfigurationService.getActiveValidationRuleSets(metrologyContract, channelsContainer) :
                Collections.emptyList();

        return usagePointValidation.findOverriddenProperties(validationRule, readingType)
                .map(overriddenProperties -> channelValidationRuleInfoFactory.createInfoForRule(validationRule, readingType, overriddenProperties, activeRuleSets, validationActive))
                .orElseGet(() -> channelValidationRuleInfoFactory.createInfoForRule(validationRule, readingType, activeRuleSets, validationActive));
    }

    @GET
    @Path("/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION
    })
    public ChannelValidationRuleInfo getUsagePointChannelValidationRuleById(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                            @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                                            @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        ReadingTypeDeliverable deliverable = resourceHelper.findReadingTypeDeliverableOrThrowException(metrologyContract, outputId, usagePoint.getName());
        ChannelsContainer channelsContainer = resourceHelper.findChannelsContainerOrThrowException(currentEffectiveMC, metrologyContract);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(ruleId);
        validateRuleApplicability(validationRule, deliverable);
        UsagePointValidation usagePointValidation = usagePointService.forValidation(usagePoint);
        return asInfo(validationRule, deliverable.getReadingType(), usagePointValidation, metrologyContract, channelsContainer);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION})
    public Response overrideChannelValidationRuleProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                            @PathParam("outputId") long outputId, ChannelValidationRuleInfo channelValidationRuleInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        validateRuleApplicability(validationRule, deliverable);
        UsagePointValidation usagePointValidation = usagePointService.forValidation(usagePoint);
        UsagePointValidation.PropertyOverrider propertyOverrider = usagePointValidation.overridePropertiesFor(validationRule, deliverable.getReadingType());

        List<PropertyInfo> propertyInfos = toPropertyInfoList(channelValidationRuleInfo.properties);
        validationRule.getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT).stream()
                .map(propertySpec -> {
                    String propertyName = propertySpec.getName();
                    Object propertyValue = propertyValueInfoService.findPropertyValue(propertySpec, propertyInfos);
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
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION})
    public Response editChannelValidationRuleOverriddenProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                  @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                                  ChannelValidationRuleInfo channelValidationRuleInfo) {
        channelValidationRuleInfo.ruleId = ruleId;
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        validateRuleApplicability(validationRule, deliverable);
        UsagePointValidation usagePointValidation = usagePointService.forValidation(usagePoint);
        ChannelValidationRuleOverriddenProperties channelValidationRule = usagePointValidation
                .findAndLockChannelValidationRuleOverriddenProperties(channelValidationRuleInfo.id, channelValidationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(validationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelValidationRule(usagePointValidation, validationRule, deliverable.getReadingType())).supplier());

        List<PropertyInfo> propertyInfos = toPropertyInfoList(channelValidationRuleInfo.properties);
        Map<String, Object> overriddenProperties = validationRule.getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT)
                .stream()
                .map(propertySpec -> {
                    String propertyName = propertySpec.getName();
                    Object propertyValue = propertyValueInfoService.findPropertyValue(propertySpec, propertyInfos);
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
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION})
    public Response restoreChannelValidationRuleProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                           @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                           ChannelValidationRuleInfo channelValidationRuleInfo) {
        channelValidationRuleInfo.ruleId = ruleId;
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        ValidationRule validationRule = resourceHelper.findValidationRuleOrThrowException(channelValidationRuleInfo.ruleId);
        validateRuleApplicability(validationRule, deliverable);
        UsagePointValidation usagePointValidation = usagePointService.forValidation(usagePoint);
        ChannelValidationRuleOverriddenProperties channelValidationRule = usagePointValidation
                .findAndLockChannelValidationRuleOverriddenProperties(channelValidationRuleInfo.id, channelValidationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(validationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelValidationRule(usagePointValidation, validationRule, deliverable.getReadingType())).supplier());
        channelValidationRule.delete();
        return Response.noContent().build();
    }

    private void validateRuleApplicability(ValidationRule validationRule, ReadingTypeDeliverable deliverable) {
        ReadingType readingType = deliverable.getReadingType();
        if (!validationRule.getReadingTypes().contains(readingType)) {
            throw exceptionFactory.newException(MessageSeeds.VALIDATION_RULE_IS_NOT_APPLICABLE_TO_OUTPUT, validationRule.getId(), deliverable.getName());
        }
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
