package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

/**
 * Provides an implementation for the {@link PropertySpec} interface for hex "String" values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-04 (12:47)
 */
public class HexStringPropertySpec extends AbstractPropertySpec {

    public HexStringPropertySpec(String name, boolean required, String displayName, String description) {
        super(name, required, displayName, description);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            try {
                this.validateValue((String) value);
            } catch (NumberFormatException e) {
                throw InvalidPropertyException.forNameAndValue(this.getName(), value);
            }
            return true;
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    private void validateValue(String value) {
        if (value.length() == 1) {
            validateValue("0" + value);
        } else {
            byte[] data = new byte[value.length() / 2];
            int offset = 0;
            int endOffset = 2;
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) Integer.parseInt(value.substring(offset, endOffset), 16);
                offset = endOffset;
                endOffset += 2;
            }
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
            return HexString.class.getName();
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