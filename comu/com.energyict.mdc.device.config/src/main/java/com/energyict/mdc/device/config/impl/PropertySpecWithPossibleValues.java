/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;

import javax.inject.Provider;
import java.util.List;

/**
 * Adds possible values to an existing property spec, all other methods are delegated to the original
 */
public class PropertySpecWithPossibleValues implements PropertySpec {
    private final Provider<List<KeyAccessorType>> keyAccessorTypeProvider;
    private final PropertySpec propertySpec;

    PropertySpecWithPossibleValues(Provider<List<KeyAccessorType>> keyAccessorTypeProvider, PropertySpec propertySpec) {
        this.keyAccessorTypeProvider = keyAccessorTypeProvider;
        this.propertySpec = propertySpec;
    }

    /**
     * If the PropertySpec is a reference to a KeyAccessorType, the possible values will be retrieved from the DeviceType
     * and added to as a wrapped version of the original PropertySpec
     */
    static PropertySpec addValuesIfApplicable(Provider<List<KeyAccessorType>> keyAccessorTypeProvider, PropertySpec propertySpec) {
        if (propertySpec.isReference() && (KeyAccessorType.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()))) {
            return new PropertySpecWithPossibleValues(keyAccessorTypeProvider, propertySpec);
        } else {
            return propertySpec;
        }
    }

    public String getName() {
        return propertySpec.getName();
    }

    public ValueFactory getValueFactory() {
        return propertySpec.getValueFactory();
    }

    public String getDisplayName() {
        return propertySpec.getDisplayName();
    }

    public boolean validateValue(Object value) throws InvalidValueException {
        return propertySpec.validateValue(value);
    }

    public PropertySpecPossibleValues getPossibleValues() {
        return new PropertySpecPossibleValues() {
            @Override
            public PropertySelectionMode getSelectionMode() {
                return PropertySelectionMode.COMBOBOX;
            }

            @Override
            public List getAllValues() {
                return keyAccessorTypeProvider.get();
            }

            @Override
            public boolean isExhaustive() {
                return true;
            }

            @Override
            public boolean isEditable() {
                return false;
            }

            @Override
            public Object getDefault() {
                return null;
            }
        };
    }

    public String getDescription() {
        return propertySpec.getDescription();
    }

    public boolean validateValueIgnoreRequired(Object value) throws InvalidValueException {
        return propertySpec.validateValueIgnoreRequired(value);
    }

    public boolean isReference() {
        return propertySpec.isReference();
    }

    public boolean supportsMultiValues() {
        return propertySpec.supportsMultiValues();
    }

    public boolean isRequired() {
        return propertySpec.isRequired();
    }
}
