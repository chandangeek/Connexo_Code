package com.energyict.mdw.cpo;

import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.ValueRequiredException;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeValueSelectionMode;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecPossibleValues;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;

import java.io.Serializable;

/**
 * Provides an implementation for the {@link PropertySpec} interface.
 * <p/>
 * User: jbr
 * Date: 7/05/12
 * Time: 10:22
 */
public class BasicPropertySpec<T> implements PropertySpec<T>, Serializable {

    private String name;
    private ValueFactory<T> valueFactory;
    private ValueDomain domain;
    private PropertySpecPossibleValues<T> possibleValues;

    // For xml serialization purposes only
    public BasicPropertySpec() {
        super();
    }

    public BasicPropertySpec(String name, ValueFactory<T> valueFactory, ValueDomain domain) {
        super();
        this.name = name;
        this.valueFactory = valueFactory;
        this.domain = domain;
    }

    protected BasicPropertySpec(String name, ValueFactory<T> valueFactory) {
        this(name, valueFactory, new ValueDomain(valueFactory.getValueType()));
    }

    @Override
    public String getKey() {
        return this.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    // For xml serialization only
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ValueFactory<T> getValueFactory() {
        return valueFactory;
    }

    // For xml serialization only
    public void setValueFactory(ValueFactory<T> valueFactory) {
        this.valueFactory = valueFactory;
    }

    @Override
    public ValueDomain getDomain() {
        return domain;
    }

    // For xml serialization only
    public void setDomain(ValueDomain domain) {
        this.domain = domain;
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
    public boolean validateValue(T value, boolean isRequired) throws InvalidValueException {
        if (isRequired &&
                (value == null ||
                        (value instanceof String && ((String) value).length() == 0) ||
                        (value instanceof Password && (((Password) value).getValue() == null || ((Password) value).getValue().length() == 0))
                )
                ) {
            throw new ValueRequiredException("XisARequiredAttribute", "\"{0}\" is a required message attribute", this.getKey());
        }
        else if (value == null) {
            return true;    // All non required properties support null values
        }
        else if (this.getDomain().isReference()) {
            try {
                IdBusinessObject idBusinessObject = (IdBusinessObject) value;
                return this.getDomain().isValidReference(idBusinessObject);
            }
            catch (ClassCastException e) {
                throw new InvalidValueException("XisNotCompatibleWithAttributeY", "The value \"{0}\" is not compatible with the attribute specification {1}.", this.getKey(), value);
            }
        }
        else {
            if (!this.getValueFactory().getValueType().isAssignableFrom(value.getClass())) {
                throw new InvalidValueException("XisNotCompatibleWithAttributeY", "The value \"{0}\" is not compatible with the attribute specification {1}.", this.getKey(), value);
            }
        }
        return true;
    }

    @Override
    public PropertySpecPossibleValues<T> getPossibleValues() {
        return this.possibleValues;
    }

    // Allow xml serialization mechanism, subclasses or friendly builders to specify possible values
    public void setPossibleValues(PropertySpecPossibleValues<T> possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public boolean isReference() {
        return this.getDomain().isReference();
    }

    @Override
    public IdBusinessObjectFactory getObjectFactory() {
        if (this.isReference()) {
            return (IdBusinessObjectFactory) this.getDomain().getFactory();
        }
        else {
            return null;
        }
    }

    @Override
    public AttributeValueSelectionMode getSelectionMode() {
        PropertySpecPossibleValues<T> possibleValues = this.getPossibleValues();
        if (possibleValues != null
                && !possibleValues.getAllValues().isEmpty()
                && possibleValues.isExhaustive()) {
            return AttributeValueSelectionMode.COMBOBOX;
        }
        else {
            return AttributeValueSelectionMode.UNSPECIFIED;
        }
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, boolean required) {
        throw new UnsupportedOperationException("PropertySpec#getEditorSeed(DynamicAttributeOwner) is no longer supported");
    }

}