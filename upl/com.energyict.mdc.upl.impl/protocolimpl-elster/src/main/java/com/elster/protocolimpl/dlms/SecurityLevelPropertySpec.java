package com.elster.protocolimpl.dlms;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocolimpl.properties.AbstractPropertySpec;

/**
 * Provides an implementation for the PropertySpec interface
 * that will model a DLMS security level property.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (11:11)
 */
public class SecurityLevelPropertySpec extends AbstractPropertySpec {

    protected SecurityLevelPropertySpec(String name, boolean required, String displayName, String description) {
        super(name, required, displayName, description);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            return this.validateValue((String) value);
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    private boolean validateValue(String value) throws InvalidPropertyException {
        if (!value.contains(":")) {
            throw new InvalidPropertyException("Security data: Invalid data (" + value + ")");
        }
        return true;
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