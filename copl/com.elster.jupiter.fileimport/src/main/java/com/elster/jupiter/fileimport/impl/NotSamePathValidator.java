/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;

public class NotSamePathValidator implements ConstraintValidator<NotSamePath, ImportSchedule> {

    private String message;

    @Override
    public void initialize(NotSamePath constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ImportSchedule importSchedule, ConstraintValidatorContext context) {
        return importSchedule == null || checkImportFolder(importSchedule, context);
    }

    private boolean checkImportFolder(ImportSchedule importSchedule, ConstraintValidatorContext context) {
        if (areTheSame(importSchedule)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("importDirectory").addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean areTheSame(ImportSchedule importSchedule) {
        if(importSchedule.getImportDirectory() != null) {
            Path importDirectory = importSchedule.getImportDirectory();

            return importDirectory.equals(importSchedule.getInProcessDirectory()) || importDirectory.equals(importSchedule.getSuccessDirectory()) ||
                    importDirectory.equals(importSchedule.getFailureDirectory());
        }
        return false;
    }
}
