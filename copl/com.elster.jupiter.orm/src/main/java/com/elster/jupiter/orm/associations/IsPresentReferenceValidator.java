package com.elster.jupiter.orm.associations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Provides an implementation for the {@link IsPresent} contraint
 * when applied to Reference fields.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (12:26)
 */
class IsPresentReferenceValidator implements ConstraintValidator<IsPresent, Reference> {

    @Override
    public void initialize(IsPresent constraintAnnotation) {
        // No need to initialize from the annotation element
    }

    @Override
    public boolean isValid(Reference value, ConstraintValidatorContext context) {
        return value.isPresent();
    }

}