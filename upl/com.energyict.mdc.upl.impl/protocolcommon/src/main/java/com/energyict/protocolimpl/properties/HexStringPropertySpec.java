package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.cbo.HexString;

/**
 * Provides an implementation for the {@link PropertySpec} interface for hex "String" values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-04 (12:47)
 */
class HexStringPropertySpec extends AbstractPropertySpec<HexString> {

    HexStringPropertySpec(String name, boolean required) {
        super(name, required);
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

}