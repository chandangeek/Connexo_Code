package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.obis.ObisCode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class ComportPoolPropertyObisCodeValid implements ConstraintValidator<ObisCodePropertyIsValid, ComPortPoolPropertyImpl> {
    @Override
    public void initialize(ObisCodePropertyIsValid obisCodePropertyIsValid) {

    }

    @Override
    public boolean isValid(ComPortPoolPropertyImpl value, ConstraintValidatorContext constraintValidatorContext) {
        if(value.getValue() instanceof ObisCode && ((ObisCode)value.getValue()).isInvalid()){
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("DeviceIdObisCode")
                    .addConstraintViolation();
            return false;
        }
        return true;

    }
}