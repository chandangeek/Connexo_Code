package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.ValueRequiredException;
import com.energyict.mdc.common.Password;

/**
 * Provides an implementation for the {@link PropertySpec} interface.
 *
 * User: jbr
 * Date: 7/05/12
 * Time: 10:22
 */
public class BasicPropertySpec extends com.elster.jupiter.properties.BasicPropertySpec {

    public BasicPropertySpec(String name, ValueFactory valueFactory) {
        this(name, false, valueFactory);
    }

    public BasicPropertySpec(String name, boolean required, ValueFactory valueFactory) {
        super(name, required, valueFactory);
    }

    @Override
    public boolean validateValue (Object value) throws InvalidValueException {
        return this.validateValue(value, this.required);
    }

    @Override
    public boolean validateValueIgnoreRequired(Object value) throws InvalidValueException {
        return this.validateValue(value, false);
    }

    private boolean validateValue (Object value, boolean required) throws InvalidValueException {
        if (required && this.isNull(value)) {
            throw new ValueRequiredException("XisARequiredAttribute", "\"{0}\" is a required message attribute", this.getName());
        }
        else if (value == null) {
            return true;    // All non required properties support null values
        }
        else {
            if (!this.getValueFactory().getValueType().isAssignableFrom(value.getClass())) {
                throw new InvalidValueException("XisNotCompatibleWithAttributeY", "The value \"{0}\" is not compatible with the attribute specification {1}.", this.getName(), value);
            }
            if (this.isReference()) {
                try {
                    return this.valueFactory.isPersistent(value);
                }
                catch (ClassCastException e) {
                    /* Doubtfull that this will happen because we have already type-checked the value
                     * but hey, I am a defensive programmer. */
                    throw new InvalidValueException("XisNotCompatibleWithAttributeY", "The value \"{0}\" is not compatible with the attribute specification {1}.", this.getName(), value);
                }
            }
            if (possibleValues!=null && possibleValues.isExhaustive()) {
                boolean found = false;
                for (Object o : possibleValues.getAllValues()) {
                    if (o.equals(value)) {
                        found=true;
                    }
                }
                if (!found) {
                    throw new InvalidValueException("XisNotAPossibleValue", "The value \"{0}\" is not list a possible value for this property", this.getName());
                }
            }
        }
        return true;
    }

    private boolean isNull (Object value) {
        return value == null
            || this.isNullString(value)
            || this.isNullPassword(value);
    }

    private boolean isNullString (Object value) {
        return value instanceof String && this.isNullString((String) value);
    }

    private boolean isNullString (String stringValue) {
        return stringValue == null || stringValue.isEmpty();
    }

    private boolean isNullPassword (Object value) {
        if (value instanceof Password) {
            Password passwordValue = (Password) value;
            return this.isNullString(passwordValue.getValue());
        }
        else {
            return false;
        }
    }
}