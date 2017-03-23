/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.propertyspec;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.ValueFactory;

import java.util.Optional;

public class MockPropertySpecImpl implements PropertySpec {

    private String name;
    private String displayName;
    private String description;
    private boolean required;
    private ValueFactory valueFactory;
    private PropertySpecPossibleValues propertySpecPossibleValues;

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setValueFactory(ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        return true;
    }

    @Override
    public Optional<?> getDefaultValue() {
        if (propertySpecPossibleValues != null) {
            return Optional.of(propertySpecPossibleValues.getDefault());
        }
        return Optional.empty();
    }

    @Override
    public PropertySpecPossibleValues getPossibleValues() {
        return propertySpecPossibleValues;
    }

    public void setPropertySpecPossibleValues(PropertySpecPossibleValues propertySpecPossibleValues) {
        this.propertySpecPossibleValues = propertySpecPossibleValues;
    }

    @Override
    public boolean supportsMultiValues() {
        return false;
    }

    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }
}