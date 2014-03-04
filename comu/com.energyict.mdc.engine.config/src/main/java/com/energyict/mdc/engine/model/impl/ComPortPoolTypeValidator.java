package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
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
