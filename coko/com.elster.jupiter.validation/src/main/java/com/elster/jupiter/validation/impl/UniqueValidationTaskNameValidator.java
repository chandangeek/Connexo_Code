package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Created by mandr on 3/16/2015.
 */
public class UniqueValidationTaskNameValidator implements ConstraintValidator<UniqueName, DataValidationTask> {

    private String message;
    private ValidationService validationService;

    @Inject
    public UniqueValidationTaskNameValidator(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(DataValidationTask task, ConstraintValidatorContext context) {
        return task == null || checkExisting(task, context);
    }

    private boolean checkExisting(DataValidationTask task, ConstraintValidatorContext context) {
        Optional<DataValidationTask> found = validationService.findValidationTaskByName(task.getName());
        if (!found.isPresent()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }
}