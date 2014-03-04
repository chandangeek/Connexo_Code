package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.InboundComPort;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ComPortTypeValidator implements ConstraintValidator<ComPortPoolTypeMatchesComPortType, InboundComPort> {

    private String message;

    @Override
    public void initialize(ComPortPoolTypeMatchesComPortType constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(InboundComPort inboundComPort, ConstraintValidatorContext context) {
        if (inboundComPort!=null && inboundComPort.getComPortPool()!=null && inboundComPort.getComPortType()!=inboundComPort.getComPortPool().getComPortType()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("comPortPool").addConstraintViolation();
            return false;
        }
        return true;
    }

}
