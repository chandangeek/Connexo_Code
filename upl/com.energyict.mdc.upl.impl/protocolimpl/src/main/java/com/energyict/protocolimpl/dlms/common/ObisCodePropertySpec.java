package com.energyict.protocolimpl.dlms.common;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.properties.AbstractPropertySpec;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (15:47)
 */
public class ObisCodePropertySpec extends AbstractPropertySpec {

    public ObisCodePropertySpec(String name) {
        super(name, false);
    }

    public ObisCodePropertySpec(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            try {
                ObisCode.fromString((String) value);
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
            return ObisCode.fromString(stringValue);
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