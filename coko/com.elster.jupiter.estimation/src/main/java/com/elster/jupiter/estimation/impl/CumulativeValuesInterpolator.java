package com.elster.jupiter.estimation.impl;

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
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by igh on 6/03/2015.
 */
public class CumulativeValuesInterpolator extends AbstractEstimator {

    CumulativeValuesInterpolator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    CumulativeValuesInterpolator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
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
        List<EstimationBlock> remain = new ArrayList<EstimationBlock>();
        List<EstimationBlock> estimated = new ArrayList<EstimationBlock>();
        for (EstimationBlock block : estimationBlocks) {
            estimate(block, remain, estimated);
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    public void estimate(EstimationBlock block, List<EstimationBlock> remain, List<EstimationBlock> estimated) {
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
            estimate(block, recordBefore, recordAfter);
            estimated.add(block);
        }
    }

    private void estimate(EstimationBlock block, BaseReadingRecord recordBefore, BaseReadingRecord recordAfter) {
        ReadingType readingType = block.getReadingType();
        List<? extends Estimatable> estimatables = block.estimatables();
        int numberOfIntervals = estimatables.size();
        Quantity qtyBefore = recordBefore.getQuantity(readingType);
        Quantity qtyAfter = recordAfter.getQuantity(readingType);
        BigDecimal consumption = qtyAfter.getValue().subtract(qtyBefore.getValue());
        BigDecimal step = consumption.divide(new BigDecimal(numberOfIntervals + 1));
        BigDecimal currentValue = recordBefore.getValue();
        for (Estimatable estimatable : estimatables) {
            estimatable.setEstimation(currentValue.add(step));
        }
    }

    @Override
    public String getDefaultFormat() {
        return "Cumulative values interpolator";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}
