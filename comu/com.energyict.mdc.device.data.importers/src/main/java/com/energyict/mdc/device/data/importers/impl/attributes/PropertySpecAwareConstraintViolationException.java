/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes;

import com.elster.jupiter.properties.PropertySpec;

import javax.validation.ConstraintViolationException;

/**
 * This class wraps the original {@link ConstraintViolationException} and adds knowledge about property spec so the catching
 * logger knows which PropertySpec was being imported at the time the exception was thrown.
 */
public class PropertySpecAwareConstraintViolationException extends RuntimeException {
    private final PropertySpec propertySpec;
    private final ConstraintViolationException constraintViolationException;

    public PropertySpecAwareConstraintViolationException(PropertySpec propertySpec, ConstraintViolationException constraintViolationException) {
        super(constraintViolationException);
        this.propertySpec = propertySpec;
        this.constraintViolationException = constraintViolationException;
    }

    public PropertySpec getPropertySpec() {
        return propertySpec;
    }

    public ConstraintViolationException getConstraintViolationException() {
        return constraintViolationException;
    }
}
