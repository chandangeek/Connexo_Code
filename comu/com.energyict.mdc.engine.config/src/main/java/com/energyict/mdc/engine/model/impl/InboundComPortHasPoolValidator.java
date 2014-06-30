package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.InboundComPort;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 6/27/14
 * Time: 11:27 AM
 */
public class InboundComPortHasPoolValidator implements ConstraintValidator<ActiveComPortHasInboundComPortPool, InboundComPort> {

    @Override
    public void initialize(ActiveComPortHasInboundComPortPool activeComPortHasInboundComPortPool) {

    }

    @Override
    public boolean isValid(InboundComPort inboundComPort, ConstraintValidatorContext constraintValidatorContext) {
        if (inboundComPort.isActive() && inboundComPort.getComPortPool() == null) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL + "}")
                    .addPropertyNode("comPortPool").addConstraintViolation();
            return false;
        }
        return true;
    }
}
