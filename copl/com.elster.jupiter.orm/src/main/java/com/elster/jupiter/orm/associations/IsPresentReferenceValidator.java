/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Provides an implementation for the {@link IsPresent} contraint
 * when applied to RefAny fields
 */
class IsPresentReferenceValidator implements ConstraintValidator<IsPresent, RefAny> {

    @Override
    public void initialize(IsPresent constraintAnnotation) {
        // No need to initialize from the annotation element
    }

    @Override
    public boolean isValid(RefAny refAny, ConstraintValidatorContext constraintValidatorContext) {
        return refAny!=null && refAny.isPresent();
    }

}