package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.IPBasedInboundComPort;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniquePortNumberValidator implements ConstraintValidator<UniquePortNumber, IPBasedInboundComPort> {

    private String message;

    @Override
    public void initialize(UniquePortNumber constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(IPBasedInboundComPort comPortUnderEvaluation, ConstraintValidatorContext context) {
        for (ComPort comPort : comPortUnderEvaluation.getComServer().getComPorts()) {
            if(comPort.getId() != comPortUnderEvaluation.getId()){
                if(IPBasedInboundComPort.class.isAssignableFrom(comPort.getClass())){
                    IPBasedInboundComPort ipBasedInboundComPort = (IPBasedInboundComPort) comPort;
                    if(ipBasedInboundComPort.getPortNumber() == comPortUnderEvaluation.getPortNumber()){
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(message).addPropertyNode("portNumber").addConstraintViolation();
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
