package com.energyict.mdc.firmware.impl;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueNameValidator implements ConstraintValidator<UniqueName, HasUniqueName> {

    private boolean caseSensitive;

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(HasUniqueName value, ConstraintValidatorContext context) {
        if (!value.isValidName(this.caseSensitive)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name")
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}
