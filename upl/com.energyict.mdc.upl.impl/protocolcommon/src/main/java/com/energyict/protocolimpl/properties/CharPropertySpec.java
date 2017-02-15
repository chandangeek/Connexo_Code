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
public class CharPropertySpec extends AbstractPropertySpec {

    private final Constraint constraint;

    public CharPropertySpec(String name, boolean required, String displayName, String description) {
        super(name, required, displayName, description);
        this.constraint = new NoConstraint();
    }

    public CharPropertySpec(String name, boolean required, String possibleValues, String displayName, String description) {
        super(name, required, displayName, description);
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

    @Override
    public com.energyict.mdc.upl.properties.ValueFactory getValueFactory() {
        return new ValueFactory();
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

    private static class ValueFactory implements com.energyict.mdc.upl.properties.ValueFactory {
        @Override
        public Object fromStringValue(String stringValue) {
            return stringValue;
        }

        @Override
        public String toStringValue(Object object) {
            return String.valueOf(object);
        }

        @Override
        public String getValueTypeName() {
            return String.class.getName();
        }

        @Override
        public Object valueToDatabase(Object object) {
            return object;
        }

        @Override
        public Object valueFromDatabase(Object databaseValue) {
            return databaseValue;
        }
    }

}