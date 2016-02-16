package com.elster.insight.usagepoint.config.impl.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueNameValidator implements ConstraintValidator<UniqueName, HasUniqueName> {
    private String message;

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(HasUniqueName value, ConstraintValidatorContext context) {
        if (!value.validateName()){ // delegate validation
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return false;
        }
        return true;
    }
}
