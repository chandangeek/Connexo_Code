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

}