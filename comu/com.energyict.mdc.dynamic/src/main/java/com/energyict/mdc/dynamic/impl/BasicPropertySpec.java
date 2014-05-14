package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.ValueRequiredException;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecPossibleValues;
import com.energyict.mdc.dynamic.ValueFactory;
import java.io.Serializable;

/**
 * Provides an implementation for the {@link PropertySpec} interface.
 *
 * User: jbr
 * Date: 7/05/12
 * Time: 10:22
 */
public class BasicPropertySpec<T> implements PropertySpec<T>, Serializable {

    private String name;
    private boolean required;
    private ValueFactory<T> valueFactory;
    private PropertySpecPossibleValues<T> possibleValues;

    public BasicPropertySpec(String name, ValueFactory<T> valueFactory) {
        this(name, false, valueFactory);
    }

    public BasicPropertySpec(String name, boolean required, ValueFactory<T> valueFactory) {
        super();
        this.name = name;
        this.required = required;
        this.valueFactory = valueFactory;
    }

    @Override
    public String getName () {
        return name;
    }

    // Allow subclasses or friendly builders to specify required or optional
    public void setName (String name) {
        this.name = name;
    }

    public boolean isRequired () {
        return required;
    }

    @Override
    public boolean isReference () {
        return this.getValueFactory().isReference();
    }

    // Allow subclasses or friendly builders to specify required or optional
    public void setRequired (boolean required) {
        this.required = required;
    }

    @Override
    public ValueFactory<T> getValueFactory() {
        return valueFactory;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof PropertySpec) {
            PropertySpec that = (PropertySpec) other;
            return that.getName().equals(this.name);
        }
        else {
            return false;
        }
    }

    @Override
    public boolean validateValue (T value) throws InvalidValueException {
        return this.validateValue(value, this.required);
    }

    @Override
    public boolean validateValueIgnoreRequired(T value) throws InvalidValueException {
        return this.validateValue(value, false);
    }

    private boolean validateValue (T value, boolean required) throws InvalidValueException {
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

    private boolean isNull (T value) {
        return value == null
            || this.isNullString(value)
            || this.isNullPassword(value);
    }

    private boolean isNullString (T value) {
        return value instanceof String && this.isNullString((String) value);
    }

    private boolean isNullString (String stringValue) {
        return stringValue == null || stringValue.isEmpty();
    }

    private boolean isNullPassword (T value) {
        if (value instanceof Password) {
            Password passwordValue = (Password) value;
            return this.isNullString(passwordValue.getValue());
        }
        else {
            return false;
        }
    }

    @Override
    public PropertySpecPossibleValues<T> getPossibleValues () {
        return this.possibleValues;
    }

    // Allow subclasses or friendly builders to specify possible values
    public void setPossibleValues (PropertySpecPossibleValues<T> possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.
            append(this.getClass().getSimpleName()).
            append("(name:").
            append(this.getName()).
            append("; required:").
            append(this.required).
            append("; valueFactory:").
            append(this.valueFactory.toString()).
            append(')');
        return builder.toString();
    }

}