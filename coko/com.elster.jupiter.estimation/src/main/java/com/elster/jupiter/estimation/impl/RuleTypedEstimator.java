/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Decorates an Estimator with the ability to mark EstimationBlocks with the ReadingQualityType associated with the estimation, if estimation was applied.
 */
class RuleTypedEstimator implements Estimator {

    private final int ruleId;
    private final Estimator decorated;

    RuleTypedEstimator(Estimator decorated, int ruleId) {
        this.ruleId = ruleId;
        this.decorated = decorated;
    }

    @Override
    public void init(Logger logger) {
        decorated.init(logger);
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {
        EstimationResult result = decorated.estimate(estimationBlocks, system);
        ReadingQualityType readingQualityType = ReadingQualityType.of(system, QualityCodeCategory.ESTIMATED, ruleId);
        result.estimated().forEach(block -> block.setReadingQualityType(readingQualityType));
        return result;
    }

    @Override
    public String getDisplayName() {
        return decorated.getDisplayName();
    }

    @Override
    public String getDefaultFormat() {
        return decorated.getDefaultFormat();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return decorated.getPropertySpecs();
    }

    @Override
    public NlsKey getNlsKey() {
        return decorated.getNlsKey();
    }

    @Override
    public List<String> getRequiredProperties() {
        return decorated.getRequiredProperties();
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return decorated.getSupportedQualityCodeSystems();
    }

}
