/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.estimation.EstimationRuleBuilder;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EstimationRuleEstimateWithSamplesPostBuilder implements Consumer<EstimationRuleSet> {
    private final RelativePeriod relativePeriod;
    private final TimeDuration maxPeriodOfConsecutiveSuspects;
    private List<String> readingTypes = new ArrayList<>();

    public EstimationRuleEstimateWithSamplesPostBuilder(RelativePeriod relativePeriod, TimeDuration maxPeriodOfConsecutiveSuspects) {
        this.relativePeriod = relativePeriod;
        this.maxPeriodOfConsecutiveSuspects = maxPeriodOfConsecutiveSuspects;
    }

    @Override
    public void accept(EstimationRuleSet estimationRuleSet) {
        EstimationRuleBuilder estimationRuleBuilder = estimationRuleSet.addRule("com.elster.jupiter.estimators.impl.AverageWithSamplesEstimator", "Estimate with samples");
        readingTypes.stream().forEach(estimationRuleBuilder::withReadingType);
        estimationRuleBuilder
                .havingProperty("averagewithsamples.maxPeriodOfConsecutiveSuspects").withValue(this.maxPeriodOfConsecutiveSuspects)
                .havingProperty("averagewithsamples.minNumberOfSamples").withValue(1L)
                .havingProperty("averagewithsamples.maxNumberOfSamples").withValue(5L)
                .havingProperty("averagewithsamples.allowNegativeValues").withValue(false)
                .havingProperty("averagewithsamples.relativePeriod").withValue(this.relativePeriod)
                .havingProperty("averagewithsamples.advanceReadingsSettings").withValue(NoneAdvanceReadingsSettings.INSTANCE)
                .create();
    }

    public EstimationRuleEstimateWithSamplesPostBuilder withReadingType(String readingType) {
        this.readingTypes.add(readingType);
        return this;
    }
}
