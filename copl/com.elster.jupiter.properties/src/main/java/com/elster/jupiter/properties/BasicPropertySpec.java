/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

public class BasicPropertySpec implements PropertySpec, Serializable {

    private String name;
    private String displayName;
    private String description;
    private boolean required;
    private boolean multiValued;
    private ValueFactory valueFactory;
    private PropertySpecPossibleValues possibleValues;

    public BasicPropertySpec() {
        super();
    }

    public BasicPropertySpec(ValueFactory valueFactory) {
        super();
        this.valueFactory = valueFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    // Allow subclasses or friendly builders to specify name
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    // Allow subclasses or friendly builders to specify display name
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    // Allow subclasses or friendly builders to specify description
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    // Allow subclasses or friendly builders to specify required or optional
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean supportsMultiValues() {
        return this.multiValued;
    }

    // Allow subclasses or friendly builders to specify required or optional
    @SuppressWarnings("unchecked")
    public void setMultiValued(boolean multiValued, String separator) {
        this.multiValued = multiValued;
        if (valueFactory.getValueType().equals(ReadingQualityPropertyValue.class)) {
            this.valueFactory = new ListReadingQualityFactory<>(this.valueFactory, separator);
        } else {
            this.valueFactory = new ListValueFactory<>(this.valueFactory, separator);
        }
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public boolean isReference() {
        return this.getValueFactory().isReference();
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof PropertySpec) {
            PropertySpec that = (PropertySpec) other;
            return that.getName().equals(this.name);
        } else {
            return false;
        }
    }

    @Override
    public boolean validateValue(Object value) throws InvalidValueException {
        return this.validateValue(value, this.required);
    }

    @Override
    public boolean validateValueIgnoreRequired(Object value) throws InvalidValueException {
        return this.validateValue(value, false);
    }

    @SuppressWarnings("unchecked")
    private boolean validateValue(Object value, boolean required) throws InvalidValueException {
        if (required && this.isNull(value)) {
            throw new ValueRequiredException("XisARequiredAttribute", "\"{0}\" is a required message attribute", this.getName());
        } else if (this.isNull(value)) {
            return true; // All non required properties support null values
        } else if (!this.getValueFactory().getValueType().isAssignableFrom(value.getClass())) {
            throw new InvalidValueException("XisNotCompatibleWithAttributeY", "The value \"{1}\" is not compatible with the attribute specification {0}.", this.getName(), value);
        } else {
            this.validateSimpleValue(value);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private void validateSimpleValue(Object value) throws InvalidValueException {
        if (this.getPossibleValues() != null && this.getPossibleValues().isExhaustive()) {
            if (!this.isPossibleValue(value)) {
                throw new InvalidValueException("XisNotAPossibleValue", "The value is not listed as a possible value for this property", this.getName());
            }
        }
        if (!this.valueFactory.isValid(value)) {
            invalidValueException();
        }
    }

    private void invalidValueException() throws InvalidValueException {
        if (valueFactory instanceof HasPropertyValidator) {
            HasPropertyValidator factory = ((HasPropertyValidator) valueFactory);
            throw new InvalidValueException(factory.invalidMessage(), this.getName(), factory.getReferenceValue());   //Guideline: property name
        }
        throw new InvalidValueException("XisNotValid", "{0} is not valid.", this.getName());
    }

    @SuppressWarnings("unchecked")
    protected boolean isPossibleValue(Object value) {
        if (value instanceof Collection && this.supportsMultiValues()) {
            Collection valueCollection = (Collection) value;
            ListValueFactory listValueFactory = (ListValueFactory) this.valueFactory;
            return valueCollection.stream().anyMatch(each -> this.isPossibleValue(each, listValueFactory.getActualFactory()));
        } else {
            return this.isPossibleValue(value, this.valueFactory);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isPossibleValue(Object value, ValueFactory valueFactory) {
        Object dbValue = valueFactory.valueToDatabase(value);
        return isDefaultEqual(dbValue, valueFactory) || getPossibleValues()
                .getAllValues()
                .stream()
                .map(valueFactory::valueToDatabase)
                .anyMatch(possibleDbValue -> possibleDbValue.equals(dbValue));
    }

    private boolean isDefaultEqual(Object dbValue, ValueFactory valueFactory) {
        Object defaultPossibleValue = getPossibleValues().getDefault();
        return Optional.ofNullable(defaultPossibleValue).isPresent() ? dbValue.equals(valueFactory.valueToDatabase(defaultPossibleValue)) : false;
    }

    @SuppressWarnings("unchecked")
    private boolean isNull(Object value) {
        return this.valueFactory.isNull(value);
    }

    @Override
    public PropertySpecPossibleValues getPossibleValues() {
        return this.possibleValues;
    }

    // Allow subclasses or friendly builders to specify possible values
    public void setPossibleValues(PropertySpecPossibleValues possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "(name:" +
                this.getName() +
                "; required:" +
                this.required +
                "; valueFactory:" +
                this.valueFactory.toString() +
                ')';
    }

}
