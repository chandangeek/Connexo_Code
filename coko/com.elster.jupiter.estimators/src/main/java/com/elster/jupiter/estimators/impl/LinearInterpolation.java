package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igh on 6/03/2015.
 */
public class LinearInterpolation extends AbstractEstimator {

    static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "maxNumberOfConsecutiveSuspects";
    static final BigDecimal MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = new BigDecimal(10);

    private BigDecimal numberOfConsecutiveSuspects;

    LinearInterpolation(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    LinearInterpolation(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        BigDecimal value = (BigDecimal) properties.get(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
        if (value == null) {
            this.numberOfConsecutiveSuspects = MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE;
        } else {
            this.numberOfConsecutiveSuspects = (BigDecimal) properties.get(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
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
        List<EstimationBlock> remain = new ArrayList<EstimationBlock>();
        List<EstimationBlock> estimated = new ArrayList<EstimationBlock>();
        for (EstimationBlock block : estimationBlocks) {
            if (isEstimatable(block)) {
                estimate(block, remain, estimated);
            } else {
                remain.add(block);
            }
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    private void estimate(EstimationBlock block, List<EstimationBlock> remain, List<EstimationBlock> estimated) {
        List<? extends Estimatable> estimatables = block.estimatables();
        Channel channel = block.getChannel();
        // find the reading before the first reading to be estimated
        BaseReadingRecord recordBefore =
                channel.getReading(
                        channel.getPreviousDateTime(
                                estimatables.get(0).getTimestamp())).orElse(null);
        // find the reading after the last reading to be estimated
        BaseReadingRecord recordAfter =
                channel.getReading(
                        channel.getNextDateTime(
                                estimatables.get(estimatables.size() - 1).getTimestamp())).orElse(null);
        if ((recordBefore == null) || (recordAfter == null)) {
            remain.add(block);
        } else {
            Quantity qtyBefore = recordBefore.getQuantity(block.getReadingType());
            Quantity qtyAfter = recordAfter.getQuantity(block.getReadingType());
            if ((qtyBefore == null) || (qtyAfter == null)) {
                remain.add(block);
            } else {
                estimate(block, qtyBefore, qtyAfter);
                estimated.add(block);
            }
        }
    }

    private void estimate(EstimationBlock block, Quantity qtyBefore, Quantity qtyAfter) {
        List<? extends Estimatable> estimatables = block.estimatables();
        int numberOfIntervals = estimatables.size();
        BigDecimal consumption = qtyAfter.getValue().subtract(qtyBefore.getValue());
        BigDecimal step = consumption.divide(new BigDecimal(numberOfIntervals + 1), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal currentValue = qtyBefore.getValue();
        for (Estimatable estimatable : estimatables) {
            currentValue = currentValue.add(step);
            estimatable.setEstimation(currentValue);
            Logger.getAnonymousLogger().log(Level.INFO, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
        }
    }

    private boolean isEstimatable(EstimationBlock block) {
        return ((block.getReadingType().isCumulative()) &&
                (block.estimatables().size() <= numberOfConsecutiveSuspects.intValue()));
    }

    @Override
    public String getDefaultFormat() {
        return "Linear interpolation";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, true, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE));
        return builder.build();
    }
}

