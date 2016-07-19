package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.time.TimeService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class EstimationRuleSetBuilder extends NamedBuilder<EstimationRuleSet, EstimationRuleSetBuilder> {

    private final EstimationService estimationService;
    private final TimeService timeService;
    private String description;

    @Inject
    public EstimationRuleSetBuilder(EstimationService estimationService, TimeService timeService) {
        super(EstimationRuleSetBuilder.class);
        this.estimationService = estimationService;
        this.timeService = timeService;
    }

    public EstimationRuleSetBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public Optional<EstimationRuleSet> find() {
        return estimationService.getEstimationRuleSetQuery()
                .select(where("name").isEqualTo(getName()))
                .stream().map(EstimationRuleSet.class::cast)
                .findFirst();
    }

    @Override
    public EstimationRuleSet create() {
        EstimationRuleSet ruleSet = estimationService.createEstimationRuleSet(getName(), QualityCodeSystem.MDC, description);
        addEstimateWithSamplesEstimationRule(ruleSet);
        addValueFillEstimationRule(ruleSet);
        ruleSet.save();
        return ruleSet;
    }

    private void addEstimateWithSamplesEstimationRule(EstimationRuleSet ruleSet) {
        EstimationRule rule = ruleSet.addRule("com.elster.jupiter.estimators.impl.AverageWithSamplesEstimator", "Estimate with samples")
                .withReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .havingProperty("averagewithsamples.maxNumberOfConsecutiveSuspects").withValue(10L)
                .havingProperty("averagewithsamples.minNumberOfSamples").withValue(1L)
                .havingProperty("averagewithsamples.maxNumberOfSamples").withValue(5L)
                .havingProperty("averagewithsamples.allowNegativeValues").withValue(false)
                .havingProperty("averagewithsamples.relativePeriod").withValue(timeService.getAllRelativePeriod())
                .havingProperty("averagewithsamples.advanceReadingsSettings").withValue(NoneAdvanceReadingsSettings.INSTANCE)
                .create();
    }

    private void addValueFillEstimationRule(EstimationRuleSet ruleSet) {
        EstimationRule rule = ruleSet.addRule("com.elster.jupiter.estimators.impl.ValueFillEstimator", "Value fill")
                .withReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .havingProperty("valuefill.maxNumberOfConsecutiveSuspects").withValue(5L)
                .havingProperty("valuefill.fillValue").withValue(new BigDecimal(900))
                .create();
    }
}
