package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

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
        info.properties = propertyValueInfoService.getPropertyInfos(estimationRule.getPropertySpecs(), estimationRule.getProps());
        info.application = resourceHelper.getApplicationInfo(estimationRule.getRuleSet().getQualityCodeSystem());
        info.when = readingQualityRecord.getTimestamp();
        return info;
    }
}
