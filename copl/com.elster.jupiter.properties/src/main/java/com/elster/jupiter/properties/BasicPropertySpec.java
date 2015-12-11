package com.elster.jupiter.properties;

import java.io.Serializable;

public class BasicPropertySpec implements PropertySpec, Serializable {

    private String name;
    private String displayName;
    private String description;
    private boolean required;
    private boolean multiValued;
    private ValueFactory valueFactory;
    private PropertySpecPossibleValues possibleValues;

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
    public void setMultiValued(boolean multiValued) {
        this.multiValued = multiValued;
    }

    @Override
    public boolean isReference() {
        return false;
    }

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
        } else if (value == null) {
            return true; // All non required properties support null values
        } else {
            if (!this.getValueFactory().getValueType().isAssignableFrom(value.getClass())) {
                throw new InvalidValueException("XisNotCompatibleWithAttributeY", "The value \"{1}\" is not compatible with the attribute specification {0}.", this.getName(), value);
            }
            else if (StringFactory.class.equals(this.getValueFactory().getClass())) {
                String stringValue = (String) value;
                if (stringValue.length() > StringFactory.MAX_SIZE) {
                    throw new InvalidValueException("XisToBig", "The value is too large for this property (max length=" + StringFactory.MAX_SIZE + ")", this.getName());
                }
            }
            if (possibleValues != null && possibleValues.isExhaustive()) {
                if (!isValuePossible(value)) {
                    throw new InvalidValueException("XisNotAPossibleValue", "The value is not listed as a possible value for this property", this.getName());
                }
            }
        }
        return true;
    }

    protected boolean isValuePossible(Object value) {
        return possibleValues.getAllValues().contains(value);
    }

    private boolean isNull(Object value) {
        return value == null || this.isNullString(value);
    }

    private boolean isNullString(Object value) {
        return value instanceof String && this.isNullString((String) value);
    }

    private boolean isNullString(String stringValue) {
        return stringValue == null || stringValue.isEmpty();
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