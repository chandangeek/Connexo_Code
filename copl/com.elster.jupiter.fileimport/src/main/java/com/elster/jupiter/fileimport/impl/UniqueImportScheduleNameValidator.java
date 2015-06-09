package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueImportScheduleNameValidator implements ConstraintValidator<UniqueName, ImportSchedule> {

    private String message;
    private FileImportService fileImportService;

    @Inject
    public UniqueImportScheduleNameValidator(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ImportSchedule importSchedule, ConstraintValidatorContext context) {
        return importSchedule == null || checkValidity(importSchedule, context);
    }

    private boolean checkValidity(ImportSchedule importSchedule , ConstraintValidatorContext context) {
        Optional<? extends ImportSchedule > alreadyExisting = fileImportService.getImportSchedule(importSchedule.getName());
        return !alreadyExisting.isPresent() || !checkExisting(importSchedule, alreadyExisting.get(), context);
    }

    private boolean checkExisting(ImportSchedule importSchedule, ImportSchedule alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(importSchedule, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areNotTheSame(ImportSchedule importSchedule, ImportSchedule alreadyExisting) {
        return importSchedule.getId() != alreadyExisting.getId();
    }


}