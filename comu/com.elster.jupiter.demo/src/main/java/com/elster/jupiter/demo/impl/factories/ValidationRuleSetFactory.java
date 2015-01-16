package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class ValidationRuleSetFactory extends NamedFactory<ValidationRuleSetFactory, ValidationRuleSet> {
    private final ValidationService validationService;

    private String description;

    @Inject
    public ValidationRuleSetFactory(ValidationService validationService) {
        super(ValidationRuleSetFactory.class);
        this.validationService = validationService;
    }

    public ValidationRuleSetFactory withDescription(String description){
        this.description = description;
        return this;
    }

    @Override
    public ValidationRuleSet get() {
        Log.write(this);
        Optional<ValidationRuleSet> existingRuleSet = validationService.getValidationRuleSet(getName());
        if (existingRuleSet.isPresent()){
            System.out.println("==> Validation rule set " + getName() + " already exists, skip step.");
            return existingRuleSet.get();
        }
        ValidationRuleSet ruleSet = validationService.createValidationRuleSet(Constants.Validation.RULE_SET_NAME);
        ruleSet.setDescription(this.description);
        addRegisterIncreaseValidationRule(ruleSet);
        addDetectMissingValuesValidationRule(ruleSet);
        addDetectThresholdViolationValidationRule(ruleSet);
        ruleSet.save();
        return ruleSet;
    }

    private void addDetectThresholdViolationValidationRule(ValidationRuleSet ruleSet) {
        ValidationRule rule;
        rule = ruleSet.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.ThresholdValidator", Constants.Validation.DETECT_THRESHOLD_VIOLATION);
        rule.addReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addProperty("minimum", new BigDecimal(0));
        rule.addProperty("maximum", new BigDecimal(1000));
        rule.activate();
    }

    private void addDetectMissingValuesValidationRule(ValidationRuleSet ruleSet) {
        ValidationRule rule;
        rule = ruleSet.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.MissingValuesValidator", Constants.Validation.DETECT_MISSING_VALUES);
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
        ValidationRule rule = ruleSet.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.RegisterIncreaseValidator", Constants.Validation.REGISTER_INCREASE);
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
