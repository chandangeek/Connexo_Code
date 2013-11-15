package com.elster.jupiter.validation;

import com.google.common.base.Optional;

import java.util.List;

public interface ValidationService {

    ValidationRuleSet createValidationRuleSet(String name);

    Optional<ValidationRuleSet> getValidationRuleSet(long id);


    List<ValidationRuleSet> getValidationRuleSets();
}
