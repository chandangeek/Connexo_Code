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

    public ObisCodePropertySpec(String name, String translatedName, String description) {
        super(name, false, translatedName, description);
    }

    public ObisCodePropertySpec(String name, boolean required, String translatedName, String description) {
        super(name, required, translatedName, description);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        boolean isInvalid = false;
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof ObisCode) {
            isInvalid = ((ObisCode) value).isInvalid();
        } else if (value instanceof String) {
            isInvalid = ObisCode.fromString((String) value).isInvalid();
        }

        if (isInvalid) {
            throw InvalidPropertyException.forNameAndValue(this.getDisplayName(), value);
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
            ObisCode obisCode = ObisCode.fromString(stringValue);
            return obisCode.isInvalid() ? "Invalid" : obisCode;
        }

        @Override
        public String toStringValue(Object object) {
            return object instanceof ObisCode
                    ? this.toStringValue((ObisCode) object)
                    : object.toString();
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