package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocolimpl.properties.AbstractPropertySpec;

/**
 * Provides an implementation for the PropertySpec interface for String values
 * that represent a password that must be an exact number of characters in length.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (11:33)
 */
public class PasswordPropertySpec extends AbstractPropertySpec {

    private final int length;

    protected PasswordPropertySpec(String name, boolean required, int length) {
        super(name, required);
        this.length = length;
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            this.validateValue((String) value);
            return true;
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    private void validateValue(String value) throws InvalidPropertyException {
        if (value.length() != this.length) {
            throw new InvalidPropertyException("Password must be exactly " + this.length + " character(s)");
        }
    }

}