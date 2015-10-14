package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.validation.*;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class EstimationRuleSetBuilder extends NamedBuilder<EstimationRuleSet, EstimationRuleSetBuilder> {
    private final EstimationService estimationService;

    private String description;

    @Inject
    public EstimationRuleSetBuilder(EstimationService estimationService) {
        super(EstimationRuleSetBuilder.class);
        this.estimationService = estimationService;
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
//        ValidationRuleSetVersion ruleSetVersion = ruleSet.addRuleSetVersion("Demo Default Version", Instant.EPOCH );
//        addRegisterIncreaseValidationRule(ruleSetVersion);
//        addDetectMissingValuesValidationRule(ruleSetVersion);
//        addDetectThresholdViolationValidationRule(ruleSetVersion);
        ruleSet.save();
        return ruleSet;
    }

    private void addDetectThresholdViolationValidationRule(ValidationRuleSetVersion ruleSetVersion) {
        ValidationRule rule;
        rule = ruleSetVersion.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.ThresholdValidator", "Detect threshold violation");
        rule.addReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addProperty("minimum", new BigDecimal(0));
        rule.addProperty("maximum", new BigDecimal(1000));
        rule.activate();
    }

    private void addDetectMissingValuesValidationRule(ValidationRuleSetVersion ruleSetVersion) {
        ValidationRule rule;
        rule = ruleSetVersion.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.MissingValuesValidator", "Detect missing values");
        // 15min Electricity
        rule.addReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        // Daily Electricity
        rule.addReadingType("11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0");
        // Monthly Electricity
        rule.addReadingType("13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.activate();
    }

    private void addRegisterIncreaseValidationRule(ValidationRuleSetVersion ruleSetVersion) {
        ValidationRule rule = ruleSetVersion.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.RegisterIncreaseValidator", "Register increase");
        rule.addReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addProperty("failEqualData", false);
        rule.activate();
    }
}
