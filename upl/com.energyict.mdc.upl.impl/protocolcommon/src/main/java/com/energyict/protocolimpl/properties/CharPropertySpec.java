package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

/**
 * Provides an implementation for the {@link PropertySpec} interface for "char" values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-07 (12:57)
 */
class CharPropertySpec extends AbstractPropertySpec<Character> {

    private final Constraint constraint;

    CharPropertySpec(String name, boolean required) {
        super(name, required);
        this.constraint = new NoConstraint();
    }

    CharPropertySpec(String name, boolean required, String possibleValues) {
        super(name, required);
        this.constraint = new Set(possibleValues);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            this.validateStringValue((String) value);
            return true;
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    private void validateStringValue(String value) throws MissingPropertyException, InvalidPropertyException {
        if (this.isRequired() && value.isEmpty()) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value.length() > 1) {
            throw  new InvalidPropertyException(value + " is not a valid value for property " + this.getName() + " because the length should not exceed 1 character");
        } else {
            this.constraint.validateValue(value.substring(0, 1), this.getName());
        }
    }

    private interface Constraint {
        void validateValue(String character, String propertyName) throws InvalidPropertyException;
    }

    private static class NoConstraint implements Constraint {
        @Override
        public void validateValue(String character, String propertyName) throws InvalidPropertyException {
        }
    }

    private static class Set implements Constraint {
        private final String possibleValues;

        private Set(String possibleValues) {
            this.possibleValues = possibleValues;
        }

        @Override
        public void validateValue(String character, String propertyName) throws InvalidPropertyException {
            if (!this.possibleValues.contains(character)) {
                throw  new InvalidPropertyException(character + " is not a valid value for property " + propertyName + " because it should be contained in " + this.possibleValues);
            }
        }
    }
}