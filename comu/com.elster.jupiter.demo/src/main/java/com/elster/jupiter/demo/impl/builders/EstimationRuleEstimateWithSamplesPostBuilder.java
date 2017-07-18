/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;

import java.util.function.Consumer;

public class EstimationRuleEstimateWithSamplesPostBuilder implements Consumer<EstimationRuleSet> {
    private final RelativePeriod relativePeriod;
    private final TimeDuration maxPeriodOfConsecutiveSuspects;

    public EstimationRuleEstimateWithSamplesPostBuilder(RelativePeriod relativePeriod, TimeDuration maxPeriodOfConsecutiveSuspects) {
        this.relativePeriod = relativePeriod;
        this.maxPeriodOfConsecutiveSuspects = maxPeriodOfConsecutiveSuspects;
    }

    @Override
    public void accept(EstimationRuleSet estimationRuleSet) {
        estimationRuleSet.addRule("com.elster.jupiter.estimators.impl.AverageWithSamplesEstimator", "Estimate with samples")
                .withReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .havingProperty("averagewithsamples.maxPeriodOfConsecutiveSuspects").withValue(this.maxPeriodOfConsecutiveSuspects)
                .havingProperty("averagewithsamples.minNumberOfSamples").withValue(1L)
                .havingProperty("averagewithsamples.maxNumberOfSamples").withValue(5L)
                .havingProperty("averagewithsamples.allowNegativeValues").withValue(false)
                .havingProperty("averagewithsamples.relativePeriod").withValue(this.relativePeriod)
                .havingProperty("averagewithsamples.advanceReadingsSettings").withValue(NoneAdvanceReadingsSettings.INSTANCE)
                .create();
    }
}
