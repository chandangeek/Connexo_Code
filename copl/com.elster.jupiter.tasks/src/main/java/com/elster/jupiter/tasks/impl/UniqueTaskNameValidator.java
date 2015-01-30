package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by bvn on 1/30/15.
 */
public class UniqueTaskNameValidator implements ConstraintValidator<UniqueName, RecurrentTask> {

    private final TaskService taskService;
    private String message;

    @Inject
    public UniqueTaskNameValidator(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void initialize(UniqueName annotation) {
        message = annotation.message();
    }

    @Override
    public boolean isValid(RecurrentTask recurrentTask, ConstraintValidatorContext context) {
        if (taskService.getRecurrentTask(recurrentTask.getName()).isPresent()) {
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation().disableDefaultConstraintViolation();
                return false;
        }
        return true;
    }
}
