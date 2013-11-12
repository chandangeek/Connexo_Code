package com.elster.jupiter.validation;

import com.google.common.base.Optional;

public interface ValidationService {

    ValidationRuleSet createValidationRuleSet(String name);

    Optional<ValidationRuleSet> getValidationRuleSet(long id);
}
