/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Provides an implementation for the {@link SizeForDynamicAttributeName} constraint
 * applied to the {@link ProtocolDialectConfigurationPropertyImpl} entity.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-20 (15:24)
 */
public class SizeForDynamicAttributeNameValidator implements ConstraintValidator<SizeForDynamicAttributeName, ProtocolDialectConfigurationPropertyImpl> {

    private int max;

    @Override
    public void initialize(SizeForDynamicAttributeName constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(ProtocolDialectConfigurationPropertyImpl property, ConstraintValidatorContext context) {
        if (this.length(property.getValue()) <= this.max) {
            return true;
        }
        else {
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("properties").addPropertyNode(property.getName()).addConstraintViolation()
                .disableDefaultConstraintViolation();
            return false;
        }
    }

    private int length (String string) {
        if (string == null) {
            return 0;
        }
        else {
            return string.length();
        }
    }

}