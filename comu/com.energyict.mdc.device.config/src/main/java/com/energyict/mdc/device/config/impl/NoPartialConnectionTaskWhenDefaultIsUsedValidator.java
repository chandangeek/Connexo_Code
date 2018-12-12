/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link NoPartialConnectionTaskWhenDefaultIsUsed} constraint against a {@link ComTaskEnablementImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (10:55)
 */
public class NoPartialConnectionTaskWhenDefaultIsUsedValidator implements ConstraintValidator<NoPartialConnectionTaskWhenDefaultIsUsed, ComTaskEnablementImpl> {

    @Override
    public void initialize(NoPartialConnectionTaskWhenDefaultIsUsed constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskEnablementImpl comTaskEnablement, ConstraintValidatorContext context) {
        if (comTaskEnablement.usesDefaultConnectionTask() && comTaskEnablement.hasPartialConnectionTask()) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(ComTaskEnablementImpl.Fields.USE_DEFAULT_CONNECTION_TASK.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }
}