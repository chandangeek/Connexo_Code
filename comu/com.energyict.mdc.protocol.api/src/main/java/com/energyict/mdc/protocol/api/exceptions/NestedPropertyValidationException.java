package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.upl.properties.PropertyValidationException;

/**
 * Wraps a {@link PropertyValidationException} as a RuntimeExeption.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-26 (11:20)
 */
public class NestedPropertyValidationException extends RuntimeException {
    private final PropertyValidationException uplException;

    public NestedPropertyValidationException(PropertyValidationException uplException) {
        this.uplException = uplException;
    }

    public PropertyValidationException getUplException() {
        return uplException;
    }
}