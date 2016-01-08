package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueEndDeviceGroupNameValidator implements ConstraintValidator<UniqueName, EndDeviceGroup> {

    private final MeteringGroupsService meteringGroupsService;
    private String message;

    @Inject
    public UniqueEndDeviceGroupNameValidator(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(EndDeviceGroup endDeviceGroup, ConstraintValidatorContext context) {
        return checkValidity(endDeviceGroup, context);
    }

    private boolean checkValidity(EndDeviceGroup endDeviceGroup, ConstraintValidatorContext context) {
        Optional<EndDeviceGroup> alreadyExisting = meteringGroupsService.findEndDeviceGroupByName(endDeviceGroup.getName());
        return !alreadyExisting.isPresent() || !checkExisting(endDeviceGroup, alreadyExisting.get(), context);
    }

    private boolean checkExisting(EndDeviceGroup endDeviceGroup, EndDeviceGroup alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(endDeviceGroup, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areNotTheSame(EndDeviceGroup ruleSet, EndDeviceGroup alreadyExisting) {
        return ruleSet.getId() != alreadyExisting.getId();
    }
}
