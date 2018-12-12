/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;


public class NonNullPathValidator implements ConstraintValidator<NonNullPath, Path> {

    private String message;

    @Inject
    public NonNullPathValidator() {
    }

    @Override
    public void initialize(NonNullPath constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Path path, ConstraintValidatorContext context) {
        return path != null ;
    }
}
