package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComSchedulingNameValidator implements ConstraintValidator<UniqueName, ComScheduleImpl> {

    private final SchedulingService schedulingService;

    @Inject
    public UniqueComSchedulingNameValidator(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
    }

    @Override
    public boolean isValid(ComScheduleImpl value, ConstraintValidatorContext context) {
        for (ComSchedule comSchedule : schedulingService.findAllSchedules()) {
            if (comSchedule.getName().equals(value.getName()) && comSchedule.getId()!=value.getId()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+Constants.NOT_UNIQUE+"}")
                        .addPropertyNode(ComScheduleImpl.Fields.NAME.fieldName())
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
