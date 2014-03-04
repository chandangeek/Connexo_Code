package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ComPortPoolTypeValidator implements ConstraintValidator<ComPortPoolTypeMatchesComPortType, InboundComPortPool> {

    @Override
    public void initialize(ComPortPoolTypeMatchesComPortType constraintAnnotation) {
    }

    @Override
    public boolean isValid(InboundComPortPool inboundComPortPool, ConstraintValidatorContext context) {
        for (InboundComPort inboundComPort : inboundComPortPool.getComPorts()) {
            if (inboundComPort.getComPortType()!=inboundComPortPool.getComPortType()) {
                return false;
            }
        }
        return true;
    }

}
