package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComSchedulingMRIDValidator implements ConstraintValidator<UniqueMRID, ComScheduleImpl> {

    private final SchedulingService schedulingService;
    private String message;

    @Inject
    public UniqueComSchedulingMRIDValidator(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Override
    public void initialize(UniqueMRID constraintAnnotation) {
        message=constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComScheduleImpl value, ConstraintValidatorContext context) {
        if (Checks.is(value.getmRID()).emptyOrOnlyWhiteSpace()) {
            return true;
        }
        for (ComSchedule comSchedule : schedulingService.findAllSchedules()) {
            if (Checks.is(comSchedule.getmRID()).equalTo(value.getmRID()) && comSchedule.getId()!=value.getId()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(ComScheduleImpl.Fields.MRID.fieldName())
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
