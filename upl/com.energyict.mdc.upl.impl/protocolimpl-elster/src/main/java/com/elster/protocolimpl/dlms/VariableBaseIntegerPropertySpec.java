package com.elster.protocolimpl.dlms;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocolimpl.properties.AbstractPropertySpec;

/**
 * Provides an implementation for the {@link PropertySpec} interface for "int" values
 * that are specified as a String and the base value for parsing (i.e. base 10 or base 16)
 * is only known at parse time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (11:05)
 */
class VariableBaseIntegerPropertySpec extends AbstractPropertySpec {

    VariableBaseIntegerPropertySpec(String name, boolean required, String displayName, String discription) {
        super(name, required, displayName, discription);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof Integer) {
            return true;
        } else if (value instanceof String) {
            this.validateValue((String) value);
            return true;
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    private boolean validateValue(String value) throws InvalidPropertyException {
        if (value.toUpperCase().startsWith("0X")) {
            return validateValue(value.substring(2), 16);
        } else {
            return validateValue(value, 10);
        }
    }

    private boolean validateValue(String value, int base) throws InvalidPropertyException {
        try {
            Integer.parseInt(value, base);
            return true;
        } catch (NumberFormatException e) {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value, e);
        }
    }

    @Override
    public com.energyict.mdc.upl.properties.ValueFactory getValueFactory() {
        return new ValueFactory();
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
            return Integer.class.getName();
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