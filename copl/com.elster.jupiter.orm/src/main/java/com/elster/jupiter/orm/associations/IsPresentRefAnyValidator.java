/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.orm.associations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Provides an implementation for the {@link IsPresent} contraint
 * when applied to RefAny fields.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (12:26)
 */
public class IsPresentRefAnyValidator implements ConstraintValidator<IsPresent, RefAny> {

    @Override
    public void initialize(IsPresent constraintAnnotation) {
        // No need to initialize from the annotation element
    }

    @Override
    public boolean isValid(RefAny refAny, ConstraintValidatorContext constraintValidatorContext) {
        return refAny != null && refAny.isPresent();
    }

}