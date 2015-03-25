package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.common.collect.ImmutableList;
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
public class ValueFillEstimator extends AbstractEstimator {

    static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "maxNumberOfConsecutiveSuspects";
    static final BigDecimal MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = new BigDecimal(10);

    static final String FILL_VALUE = "fillValue";
    static final BigDecimal DEFAULT_FILL_VALUE = new BigDecimal(0);

    private BigDecimal numberOfConsecutiveSuspects;
    private BigDecimal fillValue;

    ValueFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ValueFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        numberOfConsecutiveSuspects = (BigDecimal) properties.get(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
        if (numberOfConsecutiveSuspects == null) {
            this.numberOfConsecutiveSuspects = MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE;
        }
        fillValue = (BigDecimal) properties.get(FILL_VALUE);
        if (fillValue == null) {
            this.fillValue = DEFAULT_FILL_VALUE;
        }
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
        estimatable.setEstimation(fillValue);
        Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
    }



    @Override
    public String getDefaultFormat() {
        return "Zero fill";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, true, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE));
        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                FILL_VALUE, true, DEFAULT_FILL_VALUE));
        return builder.build();
    }
}
