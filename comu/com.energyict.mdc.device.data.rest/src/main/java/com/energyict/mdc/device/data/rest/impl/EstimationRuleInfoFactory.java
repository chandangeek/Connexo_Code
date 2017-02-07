/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;

public class EstimationRuleInfoFactory {

    private final EstimationService estimationService;
    private final ResourceHelper resourceHelper;
    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    EstimationRuleInfoFactory(EstimationService estimationService, ResourceHelper resourceHelper, PropertyValueInfoService propertyValueInfoService) {
        this.estimationService = estimationService;
        this.resourceHelper = resourceHelper;
        this.propertyValueInfoService = propertyValueInfoService;
    }

    public EstimationRuleInfo createEstimationRuleInfo(Collection<? extends ReadingQuality> readingQualities) {
        if (readingQualities.stream().map(ReadingQualityRecord.class::cast).noneMatch(ReadingQualityRecord::isSuspect)) {
            return readingQualities.stream()
                    .map(ReadingQualityRecord.class::cast)
                    .filter(ReadingQualityRecord::hasEstimatedCategory)
                    .findFirst()//because reading could be estimated by only one estimation rule
                    .flatMap(readingQuality -> estimationService.findEstimationRuleByQualityType(readingQuality.getType()))
                    .map(this::asInfo)
                    .orElse(null);
        }
        return null;
    }

    public IdWithNameInfo getEstimationApplicationInfo(ReadingQualityType readingQualityType) {
        Optional<? extends EstimationRule> estimationRule = estimationService.findEstimationRuleByQualityType(readingQualityType);
        return estimationRule.isPresent() ?  resourceHelper.getApplicationInfo(estimationRule.get().getRuleSet().getQualityCodeSystem()) : null;
    }

    private EstimationRuleInfo asInfo(EstimationRule estimationRule) {
        EstimationRuleInfo info = new EstimationRuleInfo();
        info.id = estimationRule.getId();
        info.ruleSetId = estimationRule.getRuleSet().getId();
        info.deleted = estimationRule.isObsolete();
        info.name = estimationRule.getName();
        info.properties = propertyValueInfoService.getPropertyInfos(estimationRule.getPropertySpecs(), estimationRule.getProps());
        info.application = resourceHelper.getApplicationInfo(estimationRule.getRuleSet().getQualityCodeSystem());
        return info;
    }
}
