/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.security.ServerDeviceForValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
import java.util.Map;

public class ValidSecurityPropertiesValidator implements ConstraintValidator<ValidSecurityProperties, Device> {
    @Override
    public void initialize(ValidSecurityProperties constraintAnnotation) {

    }

    /**
     * Iterate over all dirty security properties on a given device and validate their values
     */
    @Override
    public boolean isValid(Device device, ConstraintValidatorContext constraintValidatorContext) {
        Map<SecurityPropertySet, TypedProperties> dirtySecurityProperties = ((ServerDeviceForValidation) device).getDirtySecurityProperties();
        boolean valid = true;
        for (SecurityPropertySet securityPropertySet : dirtySecurityProperties.keySet()) {
            TypedProperties typedProperties = dirtySecurityProperties.get(securityPropertySet);

            for (PropertySpec propertySpec : securityPropertySet.getPropertySpecs()) {
                Object value = typedProperties.getProperty(propertySpec.getName());
                if (value != null) {
                    try {
                        propertySpec.validateValue(value);
                    } catch (InvalidValueException e) {
                        constraintValidatorContext
                                .buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                                .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation()
                                .disableDefaultConstraintViolation();
                        valid = false;
                    }
                }
            }
        }
        return valid;
    }
}