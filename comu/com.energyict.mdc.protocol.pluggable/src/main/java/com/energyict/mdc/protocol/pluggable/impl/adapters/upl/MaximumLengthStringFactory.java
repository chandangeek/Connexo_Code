package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.StringFactory;

/**
 * Provides an implementation for the {@link StringFactory} interface
 * that validates that the length of a String does not exceed a specified maximum.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (11:56)
 */
final class MaximumLengthStringFactory extends StringFactory {
    private final int maxLength;

    MaximumLengthStringFactory(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean isValid(String value) {
        return value.length() <= this.maxLength;
    }

}