package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.associations.Reference;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNullReferenceValidator implements ConstraintValidator<NotNullReference, Reference>{

    @Override
    public void initialize(NotNullReference constraintAnnotation) {
    }

    @Override
    public boolean isValid(Reference value, ConstraintValidatorContext context) {
        return value.isPresent();
    }
}
