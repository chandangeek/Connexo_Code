package com.elster.protocolimpl.dlms;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.elster.dlms.types.basic.ObisCode;
import com.energyict.protocolimpl.properties.AbstractPropertySpec;

/**
 * Provides an implementation for the PropertySpec interface for {@link com.elster.dlms.types.basic.ObisCode}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (11:21)
 */
class ObisCodePropertySpec extends AbstractPropertySpec<ObisCode> {

    ObisCodePropertySpec(String name, boolean required) {
        super(name, required);
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
}