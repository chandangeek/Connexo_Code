/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;


public class UniqueWorkGroupNameValidator implements ConstraintValidator<UniqueName, WorkGroupImpl> {

    private String message;
    private UserService userService;

    @Inject
    public UniqueWorkGroupNameValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(WorkGroupImpl workGroup, ConstraintValidatorContext context) {
        return workGroup == null || checkValidity(workGroup, context);
    }

    private boolean checkValidity(WorkGroupImpl workGroup , ConstraintValidatorContext context) {
        Optional<? extends WorkGroup> alreadyExisting = userService.getWorkGroup(workGroup.getName());
        return !alreadyExisting.isPresent() || !checkExisting(workGroup, alreadyExisting.get(), context);
    }

    private boolean checkExisting(WorkGroup workGroup, WorkGroup alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(workGroup, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }
    private boolean areNotTheSame(WorkGroup workGroup, WorkGroup alreadyExisting) {
        return workGroup.getId() != alreadyExisting.getId();
    }
}

