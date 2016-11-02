package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.estimation.EstimationRuleSet;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class EstimationRuleValueFillPostBuilder implements Consumer<EstimationRuleSet> {
    @Override
    public void accept(EstimationRuleSet estimationRuleSet) {
        estimationRuleSet.addRule("com.elster.jupiter.estimators.impl.ValueFillEstimator", "Value fill")
                .withReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .havingProperty("valuefill.maxNumberOfConsecutiveSuspects").withValue(5L)
                .havingProperty("valuefill.fillValue").withValue(new BigDecimal(900))
                .create();
    }
}
