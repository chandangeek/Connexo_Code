package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.ValueRequiredException;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.dynamic.EncryptedStringFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PasswordFactory;

/**
 * Provides an implementation for the {@link com.elster.jupiter.properties.PropertySpec} interface.
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
                throw new InvalidValueException("XisNotCompatibleWithAttributeY", "The value \"{1}\" is not compatible with the attribute specification {0}.", this.getName(), value);
            }
            if (this.isReference()) {
                return this.validateReference(value);
            }
            else {
                return this.validateSimpleValue(value);
            }
        }
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

    private boolean validateReference(Object value) throws InvalidValueException {
        try {
            return this.valueFactory.isPersistent(value);
        }
        catch (ClassCastException e) {
            /* Doubtfull that this will happen because we have already type-checked the value
             * but hey, I am a defensive programmer. */
            throw new InvalidValueException("XisNotCompatibleWithAttributeY", "The value \"{0}\" is not compatible with the attribute specification {1}.", this.getName(), value);
        }
    }

    private boolean validateSimpleValue(Object value) throws InvalidValueException {
        if (StringFactory.class.equals(this.getValueFactory())) {
            String stringValue = (String) value;
            if (stringValue.length() > StringFactory.MAX_SIZE) {
                throw new InvalidValueException("XisToBig", "The value is too large for this property (max length=" + StringFactory.MAX_SIZE + ")", this.getName());
            }
        }
        else if (EncryptedStringFactory.class.equals(this.getValueFactory())) {
            EncryptedStringFactory encryptedStringFactory = (EncryptedStringFactory) this.getValueFactory();
            String stringValue = (String) value;
            encryptedStringFactory.validate(stringValue, this.getName());
        }
        else if (HexStringFactory.class.equals(this.getValueFactory())) {
            HexStringFactory hexStringFactory = (HexStringFactory) this.getValueFactory();
            String stringValue = (String) value;
            hexStringFactory.validate(stringValue, this.getName());
        }
        else if (PasswordFactory.class.equals(this.getValueFactory())) {
            PasswordFactory passwordFactory = (PasswordFactory) this.getValueFactory();
            Password password = (Password) value;
            passwordFactory.validate(password, this.getName());
        }
        if (this.possibleValues != null && this.possibleValues.isExhaustive()) {
            if (!this.possibleValues.getAllValues().contains(value)) {
                throw new InvalidValueException("XisNotAPossibleValue", "The value is not listed as a possible value for this property", this.getName());
            }
        }
        return true;
    }

}