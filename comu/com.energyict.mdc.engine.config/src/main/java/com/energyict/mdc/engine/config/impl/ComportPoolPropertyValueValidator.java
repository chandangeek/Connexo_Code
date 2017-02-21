package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.InboundComPortPool;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Created by TVN on 25/01/2017.
 */
public class ComportPoolPropertyValueValidator implements ConstraintValidator<ComportPoolPropertyValueHasCorrectType, ComPortPoolPropertyImpl> {
    @Override
    public void initialize(ComportPoolPropertyValueHasCorrectType comportPoolPropertyValueHasCorrectType) {

    }

    @Override
    public boolean isValid(ComPortPoolPropertyImpl value, ConstraintValidatorContext constraintValidatorContext) {
        ComPortPool comPortPool = value.getComPortPool();
        if(comPortPool.isInbound()) {
            InboundComPortPool inboundComPortPool = (InboundComPortPool) comPortPool;
            Optional<PropertySpec> propertySpec = inboundComPortPool.getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol().getPropertySpec(value.getName());
            return !propertySpec.isPresent() || value.getValue() == null || propertySpec.get().getValueFactory().getValueType().isInstance(value.getValue());
        }
        return false;

    }
}
