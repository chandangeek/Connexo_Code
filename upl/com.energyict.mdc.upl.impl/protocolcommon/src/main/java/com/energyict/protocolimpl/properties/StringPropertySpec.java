package com.energyict.protocolimpl.properties;

import com.energyict.cpo.PropertySpecPossibleValuesImpl;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides an implementation for the {@link PropertySpec} interface for "String" values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (09:01)
 */
class StringPropertySpec extends AbstractPropertySpec {

    private Constraint constraint;
    private Optional<String> defaultValue = Optional.empty();

    StringPropertySpec(String name, boolean required) {
        super(name, required);
        this.constraint = new NoConstraint();
    }

    StringPropertySpec(String name, boolean required, String... possibleValues) {
        super(name, required);
        this.constraint = new PossibleValues(possibleValues);
    }

    void setMaximumLength(int maximumLength) {
        this.constraint = new Max(maximumLength);
    }

    void setExactLength(int length) {
        this.constraint = new Exact(length);
    }

    void setDefaultValue(String defaultValue){
        this.defaultValue = Optional.ofNullable(defaultValue);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            this.constraint.validateValue((String) value, this.getName());
            return true;
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    @Override
    public PropertySpecPossibleValues getPossibleValues() {
        return new PropertySpecPossibleValuesImpl<>(true, constraint.getPossibleValues());
    }

    @Override
    public Optional<String> getDefaultValue() {
        return defaultValue;
    }

    @Override
    public com.energyict.mdc.upl.properties.ValueFactory getValueFactory() {
        return new ValueFactory();
    }

    private interface Constraint {
        void validateValue(String value, String propertyName) throws InvalidPropertyException;

        default List<String> getPossibleValues(){
            return Collections.emptyList();
        }
    }

    /**
     * Provides an implementation for the {@link Constraint} interface
     * that does not actually impose any constraint at all.
     */
    private static class NoConstraint implements Constraint {
        @Override
        public void validateValue(String value, String propertyName) throws InvalidPropertyException {
            // No constraint so no checks and no exception
        }
    }

    private static class Max implements Constraint {
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

    private static class Exact implements Constraint {
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

    private static class PossibleValues implements Constraint {
        private final Set<String> possibleValues;

        private PossibleValues(String... possibleValues) {
            this.possibleValues = new HashSet<>(Arrays.asList(possibleValues));
        }

        @Override
        public void validateValue(String value, String propertyName) throws InvalidPropertyException {
            if (!this.possibleValues.contains(value)) {
                throw  new InvalidPropertyException(value + " is not a valid value for property " + propertyName + " because it should be contained in " + this.possibleValues);
            }
        }

        @Override
        public List<String> getPossibleValues() {
            return new ArrayList<>(possibleValues);
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