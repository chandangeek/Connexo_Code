package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.estimation.*;
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

    public EstimationRuleSetBuilder withDescription(String description){
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
        EstimationRuleSet ruleSet = estimationService.createEstimationRuleSet(getName(), description);
        addEstimateWithSamplesEstimationRule(ruleSet);
        addValueFillEstimationRule(ruleSet);
        ruleSet.save();
        return ruleSet;
    }

    private void addEstimateWithSamplesEstimationRule(EstimationRuleSet ruleSet) {
        EstimationRule rule = ruleSet.addRule("com.elster.jupiter.estimators.impl.AverageWithSamplesEstimator", "Estimate with samples");
        rule.addReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addProperty("averagewithsamples.maxNumberOfConsecutiveSuspects", 10L);
        rule.addProperty("averagewithsamples.minNumberOfSamples", 1L);
        rule.addProperty("averagewithsamples.maxNumberOfSamples", 5L);
        rule.addProperty("averagewithsamples.allowNegativeValues",false);
        rule.addProperty("averagewithsamples.relativePeriod", timeService.getAllRelativePeriod());
        rule.addProperty("averagewithsamples.advanceReadingsSettings",NoneAdvanceReadingsSettings.INSTANCE);
        rule.activate();
    }

    private void addValueFillEstimationRule(EstimationRuleSet ruleSet) {
        EstimationRule rule = ruleSet.addRule("com.elster.jupiter.estimators.impl.ValueFillEstimator", "Value fill");
        rule.addReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addProperty("valuefill.maxNumberOfConsecutiveSuspects", 5L);
        rule.addProperty("valuefill.fillValue", new BigDecimal(900));
        rule.activate();
    }
}
