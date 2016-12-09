package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.StringFactory;

/**
 * Provides an implementation for the {@link StringFactory} interface
 * that validates that the length of a String exactly matches a specified length.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (11:55)
 */
final class ExactLengthStringFactory extends StringFactory {
    private final int length;

    ExactLengthStringFactory(int length) {
        this.length = length;
    }

    @Override
    public boolean isValid(String value) {
        return value.length() == this.length;
    }

}