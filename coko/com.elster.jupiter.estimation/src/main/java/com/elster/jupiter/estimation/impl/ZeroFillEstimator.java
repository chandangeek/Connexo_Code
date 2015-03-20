package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igh on 3/03/2015.
 */
public class ZeroFillEstimator extends AbstractEstimator {

    ZeroFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ZeroFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {

    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        return null;
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks) {
        estimationBlocks.forEach(this::estimate);
        return SimpleEstimationResult.of(Collections.<EstimationBlock>emptyList(), estimationBlocks);
    }

    public void estimate(EstimationBlock estimationBlock) {
        estimationBlock.estimatables().forEach(estimatable -> estimate(estimatable));
    }

    private void estimate(Estimatable estimatable) {
        estimatable.setEstimation(BigDecimal.ZERO);
        Logger.getAnonymousLogger().log(Level.INFO, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
    }



    @Override
    public String getDefaultFormat() {
        return "Zero fill";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}
