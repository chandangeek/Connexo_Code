package com.elster.jupiter.fileimport.impl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;

public class NonEmptyPathValidator implements ConstraintValidator<NonEmptyPath, Path> {

    private String message;

    @Inject
    public NonEmptyPathValidator() {
    }

    @Override
    public void initialize(NonEmptyPath constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Path path, ConstraintValidatorContext context) {
        return path == null || !path.toString().isEmpty();
    }

}