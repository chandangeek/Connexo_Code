/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;
import java.util.Collection;

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
