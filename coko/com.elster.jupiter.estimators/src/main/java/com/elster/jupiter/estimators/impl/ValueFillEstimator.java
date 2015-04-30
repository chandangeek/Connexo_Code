package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimators.MessageSeeds;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
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
 * Created by igh on 3/03/2015.
 */
public class ValueFillEstimator extends AbstractEstimator {

    static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "valuefill.maxNumberOfConsecutiveSuspects";
    private static final BigDecimal MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = BigDecimal.valueOf(10);

    static final String FILL_VALUE = "valuefill.fillValue";
    private static final BigDecimal DEFAULT_FILL_VALUE = BigDecimal.ZERO;

    private BigDecimal numberOfConsecutiveSuspects;
    private BigDecimal fillValue;

    ValueFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ValueFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public void init() {
        numberOfConsecutiveSuspects = getProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.class)
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

    public void estimate(EstimationBlock estimationBlock, List<EstimationBlock> remain, List<EstimationBlock> estimated) {
        if (isEstimatable(estimationBlock)) {
            estimationBlock.estimatables().forEach(this::estimate);

            estimated.add(estimationBlock);
        } else {
            remain.add(estimationBlock);
        }
    }

    private void estimate(Estimatable estimatable) {
        estimatable.setEstimation(fillValue);
        Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
    }

    private boolean isEstimatable(EstimationBlock block) {
        return (block.estimatables().size() <= numberOfConsecutiveSuspects.intValue());
    }



    @Override
    public String getDefaultFormat() {
        return "Value fill";
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

    @Override
    public void validateProperties(List<EstimationRuleProperties> estimatorProperties) {
        ImmutableMap.Builder<String, Consumer<EstimationRuleProperties>> builder = ImmutableMap.builder();
        builder.put(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, property -> {
            BigDecimal value = (BigDecimal) property.getValue();
            if (hasFractionalPart(value)) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS_SHOULD_BE_INTEGER_VALUE, "properties." + MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
            }
            if (value.intValue() < 1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS, "properties." + MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
            }
        });

        ImmutableMap<String, Consumer<EstimationRuleProperties>> propertyValidations = builder.build();

        estimatorProperties.forEach(property -> {
            Optional.ofNullable(propertyValidations.get(property.getName()))
                    .ifPresent(validator -> validator.accept(property));
        });
    }

    private boolean hasFractionalPart(BigDecimal value) {
        return value.setScale(0, RoundingMode.DOWN).compareTo(value) != 0;
    }


}

