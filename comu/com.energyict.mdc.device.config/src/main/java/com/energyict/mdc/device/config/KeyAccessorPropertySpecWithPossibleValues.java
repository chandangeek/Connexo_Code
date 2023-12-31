/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */
package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;

import javax.inject.Provider;
import java.util.List;

/**
 * Adds possible values to an existing property spec, all other methods are delegated to the original
 *
 * The possible values can not be added by the protocol of connection type, because the values can be configured on the DeviceType and are thus
 * only available in a DeviceType context
 */
public class KeyAccessorPropertySpecWithPossibleValues implements PropertySpec {
    private final Provider<List<SecurityAccessorType>> keyAccessorTypeProvider;
    private final PropertySpec propertySpec;

    private KeyAccessorPropertySpecWithPossibleValues(Provider<List<SecurityAccessorType>> keyAccessorTypeProvider, PropertySpec propertySpec) {
        this.keyAccessorTypeProvider = keyAccessorTypeProvider;
        this.propertySpec = propertySpec;
        if (BasicPropertySpec.class.isAssignableFrom(propertySpec.getClass())) {
            ((BasicPropertySpec)propertySpec).setPossibleValues(getPossibleValues()); // Explicit call to set values to allow validation
        }
    }

    /**
     * If the PropertySpec is a reference to a KeyAccessorType, the possible values will be retrieved from the DeviceType
     * and added to as a wrapped version of the original PropertySpec
     */
    public static PropertySpec addValuesIfApplicable(Provider<List<SecurityAccessorType>> keyAccessorTypeProvider, PropertySpec propertySpec) {
        if (propertySpec.isReference() && (SecurityAccessorType.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()))) {
            return new KeyAccessorPropertySpecWithPossibleValues(keyAccessorTypeProvider, propertySpec);
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
