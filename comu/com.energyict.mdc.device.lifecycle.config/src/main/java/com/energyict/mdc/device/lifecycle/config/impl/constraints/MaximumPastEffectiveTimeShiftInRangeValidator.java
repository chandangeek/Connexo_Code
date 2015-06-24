package com.energyict.mdc.device.lifecycle.config.impl.constraints;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleImpl;

import com.elster.jupiter.time.TimeDuration;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link MaximumPastEffectiveTimeShiftInRange} constraint
 * against a {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-24 (11:18)
 */
public class MaximumPastEffectiveTimeShiftInRangeValidator implements ConstraintValidator<MaximumPastEffectiveTimeShiftInRange, DeviceLifeCycle> {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public MaximumPastEffectiveTimeShiftInRangeValidator(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super();
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public void initialize(MaximumPastEffectiveTimeShiftInRange constraintAnnotation) {
        // No need to extract any information from the annotation
    }

    @Override
    public boolean isValid(DeviceLifeCycle deviceLifeCycle, ConstraintValidatorContext context) {
        TimeDuration timeShift = deviceLifeCycle.getMaximumPastEffectiveTimeShift();
        if (timeShift == null) {
            // Other annotations deal with null values
            return true;
        }
        else {
            if (timeShift.compareTo(this.deviceLifeCycleConfigurationService.getMaximumPastEffectiveTimeShift()) > 0) {
                // time shift > maximum
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(DeviceLifeCycleImpl.Fields.MAX_PAST_EFFECTIVE_TIME_SHIFT.fieldName())
                        .addConstraintViolation();
                return false;
            }
            else {
                return true;
            }
        }
    }

}