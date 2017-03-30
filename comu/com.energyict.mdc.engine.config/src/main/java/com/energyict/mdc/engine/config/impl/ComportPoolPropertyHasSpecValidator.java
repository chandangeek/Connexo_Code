package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.InboundComPortPool;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ComportPoolPropertyHasSpecValidator implements ConstraintValidator<ComportPoolPropertyMustHaveSpec, ComPortPoolPropertyImpl> {
    @Override
    public void initialize(ComportPoolPropertyMustHaveSpec comportPoolPropertyMustHaveSpec) {

    }

    @Override
    public boolean isValid(ComPortPoolPropertyImpl comPortPoolProperty, ConstraintValidatorContext constraintValidatorContext) {
        ComPortPool comPortPool = comPortPoolProperty.getComPortPool();
        if(comPortPool.isInbound()) {
            InboundComPortPool inboundComPortPool = (InboundComPortPool) comPortPool;
            return inboundComPortPool.getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol().getPropertySpec(comPortPoolProperty.getName()).isPresent();
        }
        return false;
    }
}
