package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    public void init() {
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
        List<EstimationBlock> remain = new ArrayList<EstimationBlock>();
        List<EstimationBlock> estimated = new ArrayList<EstimationBlock>();
        for (EstimationBlock block : estimationBlocks) {
            estimate (block, remain, estimated);
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    public void estimate(EstimationBlock estimationBlock, List<EstimationBlock> remain, List<EstimationBlock> estimated) {
        if (isEstimatable(estimationBlock)) {
            estimationBlock.estimatables().forEach(estimatable -> estimate(estimatable));

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
}

