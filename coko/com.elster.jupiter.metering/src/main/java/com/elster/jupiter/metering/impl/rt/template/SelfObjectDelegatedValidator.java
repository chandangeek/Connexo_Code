package com.elster.jupiter.metering.impl.rt.template;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

final class SelfObjectDelegatedValidator implements ConstraintValidator<SelfValid, SelfObjectValidator> {

    @Override
    public void initialize(SelfValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(SelfObjectValidator value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.validate(context);
    }
}
