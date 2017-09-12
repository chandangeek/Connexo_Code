/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link NoPartialConnectionTaskWhenDefaultIsUsed} constraint against a {@link ComTaskEnablementImpl}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-06-26 (11:36)
 */
public class NoPartialConnectionTaskWhenConnectionFunctionIsUsedValidator implements ConstraintValidator<NoPartialConnectionTaskWhenConnectionFunctionIsUsed, ComTaskEnablementImpl> {

    @Override
    public void initialize(NoPartialConnectionTaskWhenConnectionFunctionIsUsed constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskEnablementImpl comTaskEnablement, ConstraintValidatorContext context) {
        if (comTaskEnablement.getConnectionFunction().isPresent() && comTaskEnablement.hasPartialConnectionTask()) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(ComTaskEnablementImpl.Fields.PARTIAL_CONNECTION_TASK.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }
}