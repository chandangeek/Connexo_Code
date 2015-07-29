package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComTaskNameValidator implements ConstraintValidator<UniqueName, ComTask> {

    private final TaskService taskService;
    private String message;

    @Inject
    public UniqueComTaskNameValidator(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message=constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComTask comTaskUnderEvaluation, ConstraintValidatorContext context) {
        final boolean[] valid = {true};
        taskService.findAllComTasks()
                .stream()
                .filter(comTask -> comTask.getId() != comTaskUnderEvaluation.getId() && comTask.getName().equals(comTaskUnderEvaluation.getName()))
                .findAny()
                .ifPresent(comTask -> {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(message).addPropertyNode(ComTaskImpl.Fields.NAME.fieldName()).addConstraintViolation();
                    valid[0] =false;
                });

        return valid[0];
    }

}
