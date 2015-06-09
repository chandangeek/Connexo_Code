package com.elster.jupiter.orm.associations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Provides an implementation for the {@link IsPresent} contraint
 * when applied to Optional fields.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (12:09)
 */
class IsPresentOptionalValidator implements ConstraintValidator<IsPresent, Optional> {

    @Override
    public void initialize(IsPresent constraintAnnotation) {
        // No need to initialize from the annotation element
    }

    @Override
    public boolean isValid(Optional value, ConstraintValidatorContext context) {
        return value.isPresent();
    }

}