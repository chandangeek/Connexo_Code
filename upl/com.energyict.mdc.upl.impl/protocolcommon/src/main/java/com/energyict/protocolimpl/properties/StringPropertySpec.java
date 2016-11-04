package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

/**
 * Provides an implementation for the {@link PropertySpec} interface for "String" values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (09:01)
 */
class StringPropertySpec extends AbstractPropertySpec {

    private LengthConstraint lengthConstraint;

    StringPropertySpec(String name, boolean required) {
        super(name, required);
        this.lengthConstraint = new NoConstraint();
    }

    void setMaximumLength(int maximumLength) {
        this.lengthConstraint = new Max(maximumLength);
    }

    void setExactLength(int length) {
        this.lengthConstraint = new Exact(length);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            this.lengthConstraint.validateValue((String) value, this.getName());
            return true;
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    private interface LengthConstraint {
        void validateValue(String value, String propertyName) throws InvalidPropertyException;
    }

    /**
     * Provides an implementation for the {@link LengthConstraint} interface
     * that does not actually impose any constraint at all.
     */
    private static class NoConstraint implements LengthConstraint {
        @Override
        public void validateValue(String value, String propertyName) throws InvalidPropertyException {
            // No constraint so no checks and no exception
        }
    }

    private static class Max implements LengthConstraint {
        private final int maxLength;

        private Max(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void validateValue(String value, String propertyName) throws InvalidPropertyException {
            if (value != null && value.length() > this.maxLength) {
                throw  new InvalidPropertyException(value + " is not a valid value for property " + propertyName + " because the length should not exceed " + this.maxLength + " character(s)");
            }
        }
    }

    private static class Exact implements LengthConstraint {
        private final int length;

        private Exact(int length) {
            this.length = length;
        }

        @Override
        public void validateValue(String value, String propertyName) throws InvalidPropertyException {
            if (value != null && value.length() != this.length) {
                throw  new InvalidPropertyException(value + " is not a valid value for property " + propertyName + " because the length should be exactly" + this.length + " character(s)");
            }
        }
    }

}