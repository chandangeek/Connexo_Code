/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.elster.jupiter.properties.InvalidValueException;
import com.energyict.mdc.device.data.impl.ServerDeviceProtocolPropertyForValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;

public class ValidDeviceProtocolPropertiesValidator implements ConstraintValidator<ValidDeviceProtocolProperties, ServerDeviceProtocolPropertyForValidation> {

    @Override
    public void initialize(ValidDeviceProtocolProperties constraintAnnotation) {

    }

    @Override
    public boolean isValid(ServerDeviceProtocolPropertyForValidation deviceProtocolProperty, ConstraintValidatorContext constraintValidatorContext) {
        Object value = deviceProtocolProperty.getPropertyValue();
        if (value != null) {
            try {
                deviceProtocolProperty.getPropertySpec().validateValue(
                        deviceProtocolProperty.getPropertySpec().getValueFactory().fromStringValue((String) value)
                );
            } catch (InvalidValueException e) {
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                        .addPropertyNode("properties").addPropertyNode(deviceProtocolProperty.getPropertySpec().getName()).addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
        }
        return true;
    }
}