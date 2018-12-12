/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ComPortPoolTypeValidator implements ConstraintValidator<ComPortPoolTypeMatchesComPortType, ComPortPool> {

    @Override
    public void initialize(ComPortPoolTypeMatchesComPortType constraintAnnotation) {
    }

    @Override
    public boolean isValid(ComPortPool inboundComPortPool, ConstraintValidatorContext context) {
        for (ComPort comPort : inboundComPortPool.getComPorts()) {
            if (comPort.getComPortType()!=inboundComPortPool.getComPortType()) {
                return false;
            }
        }
        return true;
    }

}
