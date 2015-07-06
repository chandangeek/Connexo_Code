package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.rest.PropertyUtils;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EstimationRuleInfoFactory {

    private final EstimationService estimationService;
    private final PropertyUtils propertyUtils;

    @Inject
    EstimationRuleInfoFactory(EstimationService estimationService, PropertyUtils propertyUtils) {
        this.estimationService = estimationService;
        this.propertyUtils = propertyUtils;
    }

    public EstimationRuleInfo createEstimationRuleInfo(EstimationRule estimationRule) {
        EstimationRuleInfo info = new EstimationRuleInfo();
        info.id = estimationRule.getId();
        info.ruleSetId = estimationRule.getRuleSet().getId();
        info.name = estimationRule.getName();
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(estimationRule.getPropertySpecs(), estimationRule.getProps());
        return info;
    }

    public Set<EstimationRuleInfo> createEstimationRulesInfo(Collection<? extends ReadingQuality> readingQualities) {
        if (readingQualities.stream().map(ReadingQualityRecord.class::cast).noneMatch(ReadingQualityRecord::isSuspect)) {
            return readingQualities.stream()
                    .map(ReadingQualityRecord.class::cast)
                    .filter(ReadingQualityRecord::hasEstimatedCategory)
                    .map(readingQuality -> estimationService.findEstimationRuleByQualityType(readingQuality.getType()))
                    .flatMap(optional -> optional.isPresent() ? Stream.of(optional.get()) : Stream.empty())
                    .map(this::createEstimationRuleInfo)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
