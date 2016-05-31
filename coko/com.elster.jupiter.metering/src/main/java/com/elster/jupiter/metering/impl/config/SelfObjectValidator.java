package com.elster.jupiter.metering.impl.config;

import javax.validation.ConstraintValidatorContext;

public interface SelfObjectValidator {

    boolean validate(ConstraintValidatorContext context);
}
