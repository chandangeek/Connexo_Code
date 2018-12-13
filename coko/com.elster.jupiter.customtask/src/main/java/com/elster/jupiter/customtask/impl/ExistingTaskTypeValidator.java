/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class ExistingTaskTypeValidator implements ConstraintValidator<IsExistingTaskType, CustomTaskImpl> {

    private final ICustomTaskService customTaskService;

    @Inject
    public ExistingTaskTypeValidator(ICustomTaskService customTaskService) {
        this.customTaskService = customTaskService;
    }

    @Override
    public void initialize(IsExistingTaskType annotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(CustomTaskImpl customTaskImpl, ConstraintValidatorContext context) {
        return customTaskService.getAvailableCustomTasks(getApplicationCodeFromName(customTaskImpl.getApplication())).stream()
                .map(customTaskFactory -> customTaskFactory.getName())
                .anyMatch(name -> name.equals(customTaskImpl.getTaskType()));
    }

    private String getApplicationCodeFromName(String appCode) {
        switch (appCode) {
            case "MultiSense":
                return "MDC";
            case "Insight":
                return "INS";
            default:
                return appCode;
        }
    }
}