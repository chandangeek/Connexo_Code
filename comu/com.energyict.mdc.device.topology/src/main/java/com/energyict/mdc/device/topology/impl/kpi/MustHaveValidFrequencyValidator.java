/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiFrequency;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MustHaveValidFrequencyValidator implements ConstraintValidator<MustHaveValidFrequency, RegisteredDevicesKpi> {

    @Override
    public void initialize(MustHaveValidFrequency mustHaveValidFrequency) {

    }

    @Override
    public boolean isValid(RegisteredDevicesKpi registeredDevicesKpi, ConstraintValidatorContext constraintValidatorContext) {
        if (!RegisteredDevicesKpiFrequency.valueOf(registeredDevicesKpi.getFrequency()).isPresent()) {
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("frequency")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}

