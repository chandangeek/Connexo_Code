package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComSchedulingNameValidator implements ConstraintValidator<UniqueName, ComScheduleImpl> {

    private final SchedulingService schedulingService;
    private String message;

    @Inject
    public UniqueComSchedulingNameValidator(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message=constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComScheduleImpl value, ConstraintValidatorContext context) {
        if (Checks.is(value.getName()).emptyOrOnlyWhiteSpace()) {
            return true;
        }
        for (ComSchedule comSchedule : schedulingService.findAllSchedules()) {
            if (comSchedule.getName().equals(value.getName()) && comSchedule.getId()!=value.getId()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(ComScheduleImpl.Fields.NAME.fieldName())
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
