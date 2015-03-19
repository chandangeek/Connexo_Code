package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Decorates an Estimator with the ability to mark EstimationBlocks with the ReadingQualityType associated with the estimation, if estimation was applied.
 */
class ReadingQualityTypedEstimator implements Estimator {

    private final ReadingQualityType readingQualityType;
    private final Estimator decorated;

    ReadingQualityTypedEstimator(Estimator decorated, ReadingQualityType readingQualityType) {
        this.readingQualityType = readingQualityType;
        this.decorated = decorated;
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        decorated.init(channel, readingType, interval);
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlock) {
        EstimationResult result = decorated.estimate(estimationBlock);
        result.estimated().forEach(block -> block.setReadingQuailtyType(readingQualityType));
        return result;
    }

    @Override
    public String getDisplayName() {
        return decorated.getDisplayName();
    }

    @Override
    public String getDisplayName(String property) {
        return decorated.getDisplayName(property);
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
    public PropertySpec getPropertySpec(String name) {
        return decorated.getPropertySpec(name);
    }
}
