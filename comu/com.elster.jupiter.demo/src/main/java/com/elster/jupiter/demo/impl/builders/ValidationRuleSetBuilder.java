package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.validation.*;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
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
        ValidationRuleSetVersion ruleSetVersion = ruleSet.addRuleSetVersion("DEMO_VERSION", "Demo Default Version", Instant.EPOCH );
        addRegisterIncreaseValidationRule(ruleSetVersion);
        addDetectMissingValuesValidationRule(ruleSetVersion);
        addDetectThresholdViolationValidationRule(ruleSetVersion);
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
