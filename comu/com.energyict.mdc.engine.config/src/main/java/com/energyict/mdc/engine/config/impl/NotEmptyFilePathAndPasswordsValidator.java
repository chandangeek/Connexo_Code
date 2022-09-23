/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.IPBasedInboundComPort;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotEmptyFilePathAndPasswordsValidator implements ConstraintValidator<NotEmptyFilePathAndPasswords, IPBasedInboundComPort> {

    private NotEmptyFilePathAndPasswords constraintAnnotation;

    @Override
    public void initialize(NotEmptyFilePathAndPasswords constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(IPBasedInboundComPort value, ConstraintValidatorContext context) {
        if (value instanceof CoapBasedInboundComPort) {
            return isCoapBasedInboundComPortValid((CoapBasedInboundComPort) value, context);
        }
        if (value instanceof ServletBasedInboundComPort) {
            return isServletBasedInboundComPortValid((ServletBasedInboundComPort) value, context);
        }
        return false;
    }

    private boolean isCoapBasedInboundComPortValid(CoapBasedInboundComPort value, ConstraintValidatorContext context) {
        boolean violations = false;
        if (value.isDtls()) {
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

    private boolean isServletBasedInboundComPortValid(ServletBasedInboundComPort value, ConstraintValidatorContext context) {
        boolean violations = false;
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
