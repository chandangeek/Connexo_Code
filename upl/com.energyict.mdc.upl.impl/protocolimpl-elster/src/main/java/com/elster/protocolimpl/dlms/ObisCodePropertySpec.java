package com.elster.protocolimpl.dlms;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.elster.dlms.types.basic.ObisCode;
import com.energyict.protocolimpl.properties.AbstractPropertySpec;

/**
 * Provides an implementation for the PropertySpec interface for {@link ObisCode}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (11:21)
 */
class ObisCodePropertySpec extends AbstractPropertySpec {

    ObisCodePropertySpec(String name, boolean required, String displayName, String description) {
        super(name, required, displayName, description);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            try {
                new ObisCode((String) value);
                return true;
            } catch (IllegalArgumentException e) {
                throw InvalidPropertyException.forNameAndValue(this.getName(), value);
            }
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    @Override
    public com.energyict.mdc.upl.properties.ValueFactory getValueFactory() {
        return new ValueFactory();
    }

    private static class ValueFactory implements com.energyict.mdc.upl.properties.ValueFactory {
        @Override
        public Object fromStringValue(String stringValue) {
            return new ObisCode(stringValue);
        }

        @Override
        public String toStringValue(Object object) {
            return this.toStringValue((ObisCode) object);
        }

        private String toStringValue(ObisCode obisCode) {
            return obisCode.toString();
        }

        @Override
        public String getValueTypeName() {
            return ObisCode.class.getName();
        }

        @Override
        public Object valueToDatabase(Object object) {
            return this.toStringValue(object);
        }

        @Override
        public Object valueFromDatabase(Object databaseValue) {
            return this.fromStringValue((String) databaseValue);
        }
    }

}