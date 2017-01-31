/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotEmptyFilePathAndPasswordsValidator implements ConstraintValidator<NotEmptyFilePathAndPasswords, ServletBasedInboundComPort> {

    private NotEmptyFilePathAndPasswords constraintAnnotation;

    @Override
    public void initialize(NotEmptyFilePathAndPasswords constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(ServletBasedInboundComPort value, ConstraintValidatorContext context) {
        boolean violations=false;
        if (value.isHttps()) {
            if (Checks.is(value.getKeyStoreSpecsFilePath()).emptyOrOnlyWhiteSpace()) {
                context.buildConstraintViolationWithTemplate(constraintAnnotation.message()).addPropertyNode("keyStoreSpecsFilePath").addConstraintViolation();
                violations = true;
            }
            if (Checks.is(value.getKeyStoreSpecsPassword()).emptyOrOnlyWhiteSpace()) {
                context.buildConstraintViolationWithTemplate(constraintAnnotation.message()).addPropertyNode("keyStoreSpecsPassword").addConstraintViolation();
                violations = true;
            }
            if (Checks.is(value.getTrustStoreSpecsFilePath()).emptyOrOnlyWhiteSpace()) {
                context.buildConstraintViolationWithTemplate(constraintAnnotation.message()).addPropertyNode("trustStoreSpecsFilePath").addConstraintViolation();
                violations = true;
            }
            if (Checks.is(value.getTrustStoreSpecsPassword()).emptyOrOnlyWhiteSpace()) {
                context.buildConstraintViolationWithTemplate(constraintAnnotation.message()).addPropertyNode("trustStoreSpecsPassword").addConstraintViolation();
                violations = true;
            }
        }
        if (violations) {
            context.disableDefaultConstraintViolation();
        }
        return !violations;
    }
}
