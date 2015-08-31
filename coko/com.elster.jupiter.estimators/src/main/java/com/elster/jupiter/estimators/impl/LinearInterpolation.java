package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.units.Quantity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igh on 6/03/2015.
 */
public class LinearInterpolation extends AbstractEstimator {

    public static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "linearinterpolation.maxNumberOfConsecutiveSuspects";
    private static final Long MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = 10L;

    private Long maxNumberOfConsecutiveSuspects;

    LinearInterpolation(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    LinearInterpolation(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public void init() {
        this.maxNumberOfConsecutiveSuspects = getProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, Long.class)
                .orElse(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
        case MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS:
            return "Max number of consecutive suspects";
        default:
            return "";
        }
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
            try (LoggingContext context = initLoggingContext(block)) {
                if (canEstimate(block)) {
                    estimate(block, remain, estimated);
                } else {
                    remain.add(block);
                }
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
            String message = (recordBefore == null) ? "Failed estimation with {rule}: Block {block} since there is no reading just before the block."
                    : "Failed estimation with {rule}: Block {block} since there is no reading just after the block.";
            LoggingContext.get().info(getLogger(), message);
            remain.add(block);
        } else {
            Quantity qtyBefore = recordBefore.getQuantity(block.getReadingType());
            Quantity qtyAfter = recordAfter.getQuantity(block.getReadingType());
            if ((qtyBefore == null) || (qtyAfter == null)) {
                String message = (qtyBefore == null) ? "Failed estimation with {rule}: Block {block} since there is no reading value just before the block."
                        : "Failed estimation with {rule}: Block {block} since there is no reading value just after the block.";
                LoggingContext.get().info(getLogger(), message);
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
        BigDecimal step = consumption.divide(BigDecimal.valueOf(numberOfIntervals + 1), 6, RoundingMode.HALF_UP);
        BigDecimal currentValue = qtyBefore.getValue().setScale(6, BigDecimal.ROUND_HALF_UP);
        for (Estimatable estimatable : estimatables) {
            currentValue = currentValue.add(step);
            estimatable.setEstimation(currentValue);
            Logger.getAnonymousLogger().log(Level.INFO, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
        }
    }

    private boolean canEstimate(EstimationBlock block) {
        return isCumulative(block) && isBlockSizeOk(block);
    }

    private boolean isCumulative(EstimationBlock block) {
        boolean cumulative = block.getReadingType().isCumulative();
        if (!cumulative) {
            String message = "Failed estimation with {rule}: Block {block} since it contains its reading type {readingType} is not cumulative.";
            LoggingContext.get().info(getLogger(), message);
        }
        return cumulative;
    }

    private boolean isBlockSizeOk(EstimationBlock block) {
        boolean blockSizeOk = block.estimatables().size() <= maxNumberOfConsecutiveSuspects.intValue();
        if (!blockSizeOk) {
            String message = "Failed estimation with {rule}: Block {block} since it contains {0} suspects, which exceeds the maximum of {1}";
            LoggingContext.get().info(getLogger(), message, block.estimatables().size(), maxNumberOfConsecutiveSuspects);
        }
        return blockSizeOk;
    }

    @Override
    public String getDefaultFormat() {
        return "Linear interpolation";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().longPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, true, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE));
        return builder.build();
    }

    @Override
    public void validateProperties(Map<String, Object> estimatorProperties) {
        ImmutableMap.Builder<String, Consumer<Map.Entry<String, Object>>> builder = ImmutableMap.builder();
        builder.put(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, property -> {
            Long value = (Long) property.getValue();
            if (value.intValue() < 1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS, "properties." + MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
            }
        });

        ImmutableMap<String, Consumer<Map.Entry<String, Object>>> propertyValidations = builder.build();

        estimatorProperties.entrySet().forEach(property -> {
            Optional.ofNullable(propertyValidations.get(property.getKey()))
                    .ifPresent(validator -> validator.accept(property));
        });
    }
}

