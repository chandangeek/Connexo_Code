/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasValidPropertiesValidator implements ConstraintValidator<HasValidProperties, CustomTaskImpl> {

    private boolean valid;
    private final ICustomTaskService customTaskService;

    @Inject
    public HasValidPropertiesValidator(ICustomTaskService customTaskService) {
        this.customTaskService = customTaskService;
    }

    @Override
    public void initialize(HasValidProperties constraintAnnotation) {
        // No need to keep track of the annotation for now
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(CustomTaskImpl customTaskImpl, ConstraintValidatorContext context) {
        customTaskService.getCustomTaskFactory(customTaskImpl.getTaskType())
                .ifPresent(customTaskFactory -> { this.valid = customTaskFactory.isValid(customTaskImpl.getCustomTaskProperties(), context);});

        return this.valid;
    }



}