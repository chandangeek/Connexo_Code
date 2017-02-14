/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

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
        List<? extends RecurrentTask> existing =
                taskService.getTaskQuery().select(
                        Where.where("name").isEqualTo(recurrentTask.getName()).and(
                                Where.where("application").isEqualTo(recurrentTask.getApplication())).and(
                                Where.where("destination").isEqualTo(recurrentTask.getDestination().getName())
                        ));
        if (existing.isEmpty()) {
            return true;
        } else if (existing.size() == 1 && recurrentTask.getId() == existing.get(0).getId()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("name").addConstraintViolation();
        return false;
    }
}
