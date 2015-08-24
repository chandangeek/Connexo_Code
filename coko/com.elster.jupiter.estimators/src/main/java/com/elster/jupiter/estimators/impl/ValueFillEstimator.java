package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.estimators.MessageSeeds;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by igh on 3/03/2015.
 */
public class ValueFillEstimator extends AbstractEstimator {

    static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "valuefill.maxNumberOfConsecutiveSuspects";
    private static final Long MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = 10L;

    static final String FILL_VALUE = "valuefill.fillValue";
    private static final BigDecimal DEFAULT_FILL_VALUE = BigDecimal.ZERO;

    private Long maxNumberOfConsecutiveSuspects;
    private BigDecimal fillValue;

    ValueFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ValueFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public void init() {
        maxNumberOfConsecutiveSuspects = getProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, Long.class)
                .orElse(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);
        fillValue = getProperty(FILL_VALUE, BigDecimal.class)
                .orElse(DEFAULT_FILL_VALUE);
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
        case MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS:
            return "Max number of consecutive suspects";
        case FILL_VALUE:
            return "Fill value";
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
            estimate (block, remain, estimated);
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    public void estimate(EstimationBlock block, List<EstimationBlock> remain, List<EstimationBlock> estimated) {
        try (LoggingContext context = initLoggingContext(block)) {
            if (canEstimate(block)) {
                block.estimatables().forEach(this::estimate);

                estimated.add(block);
            } else {
                remain.add(block);
            }
        }
    }

    private void estimate(Estimatable estimatable) {
        estimatable.setEstimation(fillValue);
//        Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
    }

    private boolean canEstimate(EstimationBlock block) {
        return isBlockSizeOk(block);
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
        return "Value fill";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().longPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, true, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE));
        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                FILL_VALUE, true, DEFAULT_FILL_VALUE));
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

