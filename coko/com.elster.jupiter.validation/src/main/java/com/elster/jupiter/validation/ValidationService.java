package com.elster.jupiter.validation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.List;

public interface ValidationService {

    String COMPONENTNAME = "VAL";

    ValidationRuleSet createValidationRuleSet(String name);

    ValidationRuleSet createValidationRuleSet(String name, String description);

    Optional<ValidationRuleSet> getValidationRuleSet(long id);

    void applyRuleSet(ValidationRuleSet ruleSet, MeterActivation meterActivation);

    List<ValidationRuleSet> getValidationRuleSets();

    List<Validator> getAvailableValidators();

    void validate(MeterActivation meterActivation, Interval interval);

    Optional<ValidationRule> getValidationRule(long id);
}
