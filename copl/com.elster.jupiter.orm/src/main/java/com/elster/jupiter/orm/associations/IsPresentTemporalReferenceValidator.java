package com.elster.jupiter.orm.associations;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Clock;

/**
 * Provides an implementation for the {@link IsPresent} contraint
 * when applied to TemporalReference fields.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (12:09)
 */
class IsPresentTemporalReferenceValidator implements ConstraintValidator<IsPresent, TemporalReference> {

    private final Clock clock;

    @Inject
    IsPresentTemporalReferenceValidator(Clock clock) {
        super();
        this.clock = clock;
    }

    @Override
    public void initialize(IsPresent constraintAnnotation) {
        // No need to initialize from the annotation element
    }

    @Override
    public boolean isValid(TemporalReference value, ConstraintValidatorContext context) {
        return value.effective(this.clock.instant()).isPresent();
    }

}