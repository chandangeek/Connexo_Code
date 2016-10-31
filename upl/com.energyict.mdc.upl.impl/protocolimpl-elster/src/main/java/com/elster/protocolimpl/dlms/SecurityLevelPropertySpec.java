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

    protected SecurityLevelPropertySpec(String name, boolean required) {
        super(name, required);
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

}