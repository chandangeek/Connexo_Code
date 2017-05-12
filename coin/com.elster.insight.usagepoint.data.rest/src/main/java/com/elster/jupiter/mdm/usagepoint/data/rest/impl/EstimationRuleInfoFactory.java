/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.mdm.usagepoint.data.ChannelEstimationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EstimationRuleInfoFactory {

    private final EstimationService estimationService;
    private final ResourceHelper resourceHelper;
    private final PropertyValueInfoService propertyValueInfoService;
    private final UsagePointService usagePointService;

    @Inject
    EstimationRuleInfoFactory(EstimationService estimationService, ResourceHelper resourceHelper,
                              PropertyValueInfoService propertyValueInfoService, UsagePointService usagePointService) {
        this.estimationService = estimationService;
        this.resourceHelper = resourceHelper;
        this.propertyValueInfoService = propertyValueInfoService;
        this.usagePointService = usagePointService;
    }

    public EstimationQuantityInfo createEstimationRuleInfo(Collection<? extends ReadingQuality> readingQualities) {
        return readingQualities.stream()
                .map(ReadingQualityRecord.class::cast)
                .filter(ReadingQualityRecord::hasEstimatedCategory)
                .findFirst()
                .flatMap(readingQualityRecord -> estimationService.findEstimationRuleByQualityType(readingQualityRecord.getType()).map(rule -> asInfo(rule, readingQualityRecord)))
                .orElse(null);
    }

    private EstimationQuantityInfo asInfo(EstimationRule estimationRule, ReadingQualityRecord readingQualityRecord) {
        EstimationQuantityInfo info = new EstimationQuantityInfo();
        info.id = estimationRule.getId();
        info.ruleSetId = estimationRule.getRuleSet().getId();
        info.deleted = estimationRule.isObsolete();
        info.name = estimationRule.getName();
        info.properties = propertyValueInfoService.getPropertyInfos(estimationRule.getPropertySpecs(EstimationPropertyDefinitionLevel.ESTIMATION_RULE), estimationRule.getProps());
        info.application = resourceHelper.getApplicationInfo(estimationRule.getRuleSet().getQualityCodeSystem());
        info.when = readingQualityRecord.getTimestamp();
        info.markProjected = estimationRule.isMarkProjected();
        return info;
    }

    public EstimationRuleInfo createEstimationRuleInfo(EstimationRule estimationRule, UsagePoint usagePoint, ReadingType readingType) {
        EstimationRuleInfo info = new EstimationRuleInfo();
        info.id = estimationRule.getId();
        info.estimatorImpl = estimationRule.getImplementation();
        info.ruleSetId = estimationRule.getRuleSet().getId();
        info.deleted = estimationRule.isObsolete();
        info.name = estimationRule.getName();
        info.estimatorName = estimationRule.getDisplayName();
        info.application = resourceHelper.getApplicationInfo(estimationRule.getRuleSet().getQualityCodeSystem());
        info.markProjected = estimationRule.isMarkProjected();
        estimationRule.getComment().ifPresent(comment -> {
            info.commentId = comment.getId();
            info.commentValue = comment.getComment();
        });
        Map<String, Object> overriddenProperties = this.usagePointService.forEstimation(usagePoint)
                .findOverriddenProperties(estimationRule, readingType)
                .map(ChannelEstimationRuleOverriddenProperties::getProperties)
                .orElseGet(Collections::emptyMap);
        Map<String, Object> actualProperties = new HashMap<>(estimationRule.getProps());
        actualProperties.putAll(overriddenProperties);
        info.properties = propertyValueInfoService.getPropertyInfos(estimationRule.getPropertySpecs(), actualProperties);
        return info;
    }
}
