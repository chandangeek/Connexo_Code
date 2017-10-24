/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasUniqueNamePerDeviceTypeValidator implements ConstraintValidator<HasUniqueNamePerDeviceType, SecurityAccessorTypeImpl> {
    @Override
    public void initialize(HasUniqueNamePerDeviceType hasUniqueNamePerDeviceType) {

    }

    @Override
    public boolean isValid(SecurityAccessorTypeImpl keyAccessorType, ConstraintValidatorContext context) {
        if (!nameIsUnique(keyAccessorType)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name").addConstraintViolation();
            return false;
        }
        else {
            return true;
        }
    }

    private boolean nameIsUnique(SecurityAccessorTypeImpl keyAccessorType) {
        return keyAccessorType.getDeviceType().getSecurityAccessorTypes().stream()
                .filter(kat -> kat.getName().equals(keyAccessorType.getName()))
                .noneMatch(kat -> kat.getId()!=keyAccessorType.getId());
    }

}
