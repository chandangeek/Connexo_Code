package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class ValidationRuleSetBuilder extends NamedBuilder<ValidationRuleSet, ValidationRuleSetBuilder> {
    private final ValidationService validationService;

    private String description;

    @Inject
    public ValidationRuleSetBuilder(ValidationService validationService) {
        super(ValidationRuleSetBuilder.class);
        this.validationService = validationService;
    }

    public ValidationRuleSetBuilder withDescription(String description){
        this.description = description;
        return this;
    }

    @Override
    public Optional<ValidationRuleSet> find() {
        return validationService.getValidationRuleSet(getName());
    }

    @Override
    public ValidationRuleSet create() {
        ValidationRuleSet ruleSet = validationService.createValidationRuleSet(getName());
        ruleSet.setDescription(this.description);
        addRegisterIncreaseValidationRule(ruleSet);
        addDetectMissingValuesValidationRule(ruleSet);
        addDetectThresholdViolationValidationRule(ruleSet);
        ruleSet.save();
        return ruleSet;
    }

    private void addDetectThresholdViolationValidationRule(ValidationRuleSet ruleSet) {
        ValidationRule rule;
        rule = ruleSet.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.ThresholdValidator", "Detect threshold violation");
        rule.addReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addProperty("minimum", new BigDecimal(0));
        rule.addProperty("maximum", new BigDecimal(1000));
        rule.activate();
    }

    private void addDetectMissingValuesValidationRule(ValidationRuleSet ruleSet) {
        ValidationRule rule;
        rule = ruleSet.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.MissingValuesValidator", "Detect missing values");
        // 15min Electricity
        rule.addReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        // Daily Electricity
        rule.addReadingType("11.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0");
        // Monthly Electricity
        rule.addReadingType("13.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.activate();
    }

    private void addRegisterIncreaseValidationRule(ValidationRuleSet ruleSet) {
        ValidationRule rule = ruleSet.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.RegisterIncreaseValidator", "Register increase");
        rule.addReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addProperty("failEqualData", false);
        rule.activate();
    }
}
