/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.estimation.EstimationRuleBuilder;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.time.TimeDuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EstimationRuleValueFillPostBuilder implements Consumer<EstimationRuleSet> {
    private List<String> readingTypes = new ArrayList<>();
    private final int fillValue;

    public EstimationRuleValueFillPostBuilder(int fillValue) {
        this.fillValue = fillValue;
    }

    @Override
    public void accept(EstimationRuleSet estimationRuleSet) {
        EstimationRuleBuilder estimationRuleBuilder = estimationRuleSet.addRule("com.elster.jupiter.estimators.impl.ValueFillEstimator", "Value fill");
        readingTypes.stream().forEach(estimationRuleBuilder::withReadingType);
        estimationRuleBuilder
                .havingProperty("valuefill.maxPeriodOfConsecutiveSuspects").withValue(TimeDuration.minutes(75))
                .havingProperty("valuefill.fillValue").withValue(new BigDecimal(900))
                .create();
    }
    
    public EstimationRuleValueFillPostBuilder withReadingType(String readingType) {
        this.readingTypes.add(readingType);
        return this;
    }
}
