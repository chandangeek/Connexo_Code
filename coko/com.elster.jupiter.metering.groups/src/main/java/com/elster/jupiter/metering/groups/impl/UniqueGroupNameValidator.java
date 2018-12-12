/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.Group;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Optional;

public class UniqueGroupNameValidator implements ConstraintValidator<UniqueName, AbstractGroup<?>> {

    private final MeteringGroupsService meteringGroupsService;
    private Class<? extends Group>[] classesToCheck;
    private String message;

    @Inject
    public UniqueGroupNameValidator(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        this.classesToCheck = constraintAnnotation.groupApisToCheck();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(AbstractGroup<?> group, ConstraintValidatorContext context) {
        Class<? extends Group> groupClass = group.getClass();
        Class<? extends Group> classToFind = Arrays.stream(classesToCheck)
                .filter(aClass -> aClass.isAssignableFrom(groupClass))
                .findFirst()
                .orElse(groupClass);
        Optional<? extends Group> alreadyExisting = meteringGroupsService.findGroupByName(group.getName(), classToFind);
        return !alreadyExisting.isPresent() || !checkExisting(group, alreadyExisting.get(), context);
    }

    private boolean checkExisting(Group<?> group, Group<?> alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(group, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areNotTheSame(Group<?> group, Group<?> alreadyExisting) {
        return group.getId() != alreadyExisting.getId();
    }
}
