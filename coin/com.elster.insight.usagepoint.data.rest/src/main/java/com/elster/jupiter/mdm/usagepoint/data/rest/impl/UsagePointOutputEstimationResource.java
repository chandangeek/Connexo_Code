/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.ChannelEstimationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointEstimation;
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

public class UsagePointOutputEstimationResource {

    private final UsagePointDataModelService usagePointDataModelService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final PropertyValueInfoService propertyValueInfoService;

    private final ChannelEstimationRuleInfoFactory channelEstimationRuleInfoFactory;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;
    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    UsagePointOutputEstimationResource(UsagePointDataModelService usagePointDataModelService, UsagePointConfigurationService usagePointConfigurationService,
                                       PropertyValueInfoService propertyValueInfoService, ChannelEstimationRuleInfoFactory channelEstimationRuleInfoFactory,
                                       ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory, ExceptionFactory exceptionFactory,
                                       ResourceHelper resourceHelper) {
        this.usagePointDataModelService = usagePointDataModelService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.channelEstimationRuleInfoFactory = channelEstimationRuleInfoFactory;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION
    })
    public PagedInfoList getUsagePointChannelEstimationConfiguration(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                     @PathParam("outputId") long outputId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        ReadingType readingType = deliverable.getReadingType();
        UsagePointEstimation usagePointEstimation = usagePointDataModelService.forEstimation(usagePoint);
        List<ChannelEstimationRuleInfo> infos = usagePointConfigurationService.getEstimationRuleSets(deliverable.getMetrologyContract())
                .stream()
                .flatMap(estimationRuleSet -> estimationRuleSet.getRules(Collections.singleton(readingType)).stream())
                .map(estimationRule -> asInfo(estimationRule, readingType, usagePointEstimation))
                .collect(Collectors.toMap(Function.identity(), Function.identity(), ChannelEstimationRuleInfo::chooseEffectiveOne)).values().stream()
                .sorted(ChannelEstimationRuleInfo.defaultComparator())
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("estimation", infos, queryParameters);
    }

    private ChannelEstimationRuleInfo asInfo(EstimationRule estimationRule, ReadingType readingType, UsagePointEstimation usagePointEstimation) {
        return usagePointEstimation.findOverriddenProperties(estimationRule, readingType)
                .map(overriddenProperties -> channelEstimationRuleInfoFactory.createInfoForRule(estimationRule, readingType, overriddenProperties))
                .orElseGet(() -> channelEstimationRuleInfoFactory.createInfoForRule(estimationRule, readingType));
    }

    @GET
    @Path("/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION
    })
    public ChannelEstimationRuleInfo getUsagePointChannelEstimationRuleById(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                            @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                                            @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        EstimationRule estimationRule = resourceHelper.findEstimationRuleOrThrowException(ruleId);
        validateRuleApplicability(estimationRule, deliverable);
        UsagePointEstimation usagePointEstimation = usagePointDataModelService.forEstimation(usagePoint);
        return asInfo(estimationRule, deliverable.getReadingType(), usagePointEstimation);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION})
    public Response overrideChannelEstimationRuleProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                            @PathParam("outputId") long outputId, ChannelEstimationRuleInfo channelEstimationRuleInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        EstimationRule estimationRule = resourceHelper.findEstimationRuleOrThrowException(channelEstimationRuleInfo.ruleId);
        validateRuleApplicability(estimationRule, deliverable);
        UsagePointEstimation usagePointEstimation = usagePointDataModelService.forEstimation(usagePoint);
        UsagePointEstimation.PropertyOverrider propertyOverrider = usagePointEstimation.overridePropertiesFor(estimationRule, deliverable.getReadingType());

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

    @PUT
    @Path("/{ruleId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION})
    public Response editChannelEstimationRuleOverriddenProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                                  @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                                  ChannelEstimationRuleInfo channelEstimationRuleInfo) {
        channelEstimationRuleInfo.ruleId = ruleId;
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        EstimationRule estimationRule = resourceHelper.findEstimationRuleOrThrowException(channelEstimationRuleInfo.ruleId);
        validateRuleApplicability(estimationRule, deliverable);
        UsagePointEstimation usagePointEstimation = usagePointDataModelService.forEstimation(usagePoint);
        ChannelEstimationRuleOverriddenProperties channelEstimationRule = usagePointEstimation
                .findAndLockChannelEstimationRuleOverriddenProperties(channelEstimationRuleInfo.id, channelEstimationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(estimationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelEstimationRule(usagePointEstimation, estimationRule, deliverable.getReadingType())).supplier());

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
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION})
    public Response restoreChannelEstimationRuleProperties(@PathParam("name") String name, @PathParam("purposeId") long contractId,
                                                           @PathParam("outputId") long outputId, @PathParam("ruleId") long ruleId,
                                                           ChannelEstimationRuleInfo channelEstimationRuleInfo) {
        channelEstimationRuleInfo.ruleId = ruleId;
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        ReadingTypeDeliverable deliverable = findReadingTypeDeliverableOrThrowException(usagePoint, contractId, outputId);
        EstimationRule estimationRule = resourceHelper.findEstimationRuleOrThrowException(channelEstimationRuleInfo.ruleId);
        validateRuleApplicability(estimationRule, deliverable);
        UsagePointEstimation usagePointEstimation = usagePointDataModelService.forEstimation(usagePoint);
        ChannelEstimationRuleOverriddenProperties channelEstimationRule = usagePointEstimation
                .findAndLockChannelEstimationRuleOverriddenProperties(channelEstimationRuleInfo.id, channelEstimationRuleInfo.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(estimationRule.getDisplayName())
                        .withActualVersion(getActualVersionOfChannelEstimationRule(usagePointEstimation, estimationRule, deliverable.getReadingType())).supplier());
        channelEstimationRule.delete();
        return Response.noContent().build();
    }

    private void validateRuleApplicability(EstimationRule estimationRule, ReadingTypeDeliverable deliverable) {
        ReadingType readingType = deliverable.getReadingType();
        if (!estimationRule.getReadingTypes().contains(readingType)) {
            throw exceptionFactory.newException(MessageSeeds.ESTIMATION_RULE_IS_NOT_APPLICABLE_TO_OUTPUT, estimationRule.getId(), deliverable.getName());
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

    private Supplier<Long> getActualVersionOfChannelEstimationRule(UsagePointEstimation usagePointEstimation, EstimationRule estimationRule, ReadingType readingType) {
        return () -> usagePointEstimation.findOverriddenProperties(estimationRule, readingType)
                .map(ChannelEstimationRuleOverriddenProperties::getVersion)
                .orElse(null);
    }
}
