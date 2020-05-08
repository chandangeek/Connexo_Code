/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl.constraints;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleImpl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link MaximumFutureEffectiveTimeShiftInRange} constraint
 * against a {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-24 (11:01)
 */
public class MaximumFutureEffectiveTimeShiftInRangeValidator implements ConstraintValidator<MaximumFutureEffectiveTimeShiftInRange, DeviceLifeCycle> {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public MaximumFutureEffectiveTimeShiftInRangeValidator(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super();
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public void initialize(MaximumFutureEffectiveTimeShiftInRange constraintAnnotation) {
        // No need to extract any information from the annotation
    }

    @Override
    public boolean isValid(DeviceLifeCycle deviceLifeCycle, ConstraintValidatorContext context) {
        TimeDuration timeShift = deviceLifeCycle.getMaximumFutureEffectiveTimeShift();
        if (timeShift == null) {
            // Other annotations deal with null values
            return true;
        } else {
            return timeShift.compareTo(this.deviceLifeCycleConfigurationService.getMaximumFutureEffectiveTimeShift()) <= 0;
        }
    }

}