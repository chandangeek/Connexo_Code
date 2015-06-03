package com.elster.jupiter.issue.impl.records.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.issue.impl.records.UniqueNamed;

public class HasUniqueNameValidator implements ConstraintValidator<HasUniqueName, UniqueNamed> {

    private boolean caseSensitive;

    @Override
    public void initialize(HasUniqueName constraintAnnotation) {
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(UniqueNamed namedObject, ConstraintValidatorContext context) {
        if (!namedObject.validateUniqueName(this.caseSensitive)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name").addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }

}