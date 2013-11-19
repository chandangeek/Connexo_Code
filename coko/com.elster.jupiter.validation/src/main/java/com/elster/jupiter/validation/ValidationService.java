package com.elster.jupiter.validation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.google.common.base.Optional;

import java.util.List;

public interface ValidationService {

    ValidationRuleSet createValidationRuleSet(String name);

    Optional<ValidationRuleSet> getValidationRuleSet(long id);

    void applyRuleSet(ValidationRuleSet ruleSet, MeterActivation meterActivation);

    List<ValidationRuleSet> getValidationRuleSets();

}
