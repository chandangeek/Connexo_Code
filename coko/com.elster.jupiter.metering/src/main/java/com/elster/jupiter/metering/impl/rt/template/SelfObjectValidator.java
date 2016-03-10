package com.elster.jupiter.metering.impl.rt.template;

import javax.validation.ConstraintValidatorContext;

public interface SelfObjectValidator {

    boolean validate(ConstraintValidatorContext context);
}
