package com.energyict.mdc.tasks.task.impl;

import com.energyict.mdc.tasks.task.ComTask;
import com.energyict.mdc.tasks.task.TaskService;
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
        for (ComTask comTask : taskService.getComTasks()) {
            if (comTask.getId()!=comTaskUnderEvaluation.getId() && comTask.getName().equals(comTaskUnderEvaluation.getName())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(ComTaskImpl.Fields.NAME.fieldName()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }

}
