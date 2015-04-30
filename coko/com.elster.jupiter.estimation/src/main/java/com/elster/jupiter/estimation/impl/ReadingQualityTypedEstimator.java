package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Pair;

import java.util.List;
import java.util.logging.Logger;

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
    public void init(Logger logger) {
        decorated.init(logger);
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlock) {
        EstimationResult result = decorated.estimate(estimationBlock);
        result.estimated().forEach(block -> block.setReadingQualityType(readingQualityType));
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

    @Override
    public NlsKey getNlsKey() {
        return decorated.getNlsKey();
    }

    @Override
    public NlsKey getPropertyNlsKey(String property) {
        return decorated.getPropertyNlsKey(property);
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        return decorated.getPropertyDefaultFormat(property);
    }

    @Override
    public List<Pair<? extends NlsKey, String>> getExtraTranslations() {
        return decorated.getExtraTranslations();
    }

    @Override
    public List<String> getRequiredProperties() {
        return decorated.getRequiredProperties();
    }
}
