package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

/**
 * Provides an implementation for the {@link PropertySpec} interface for "int" values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-28 (16:32)
 */
class IntegerPropertySpec extends AbstractPropertySpec {

    IntegerPropertySpec(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof Integer) {
            return true;
        } else if (value instanceof String) {
            try {
                Integer.parseInt((String) value);
                return true;
            } catch (NumberFormatException e) {
                throw InvalidPropertyException.forNameAndValue(this.getName(), value, e);
            }
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

}