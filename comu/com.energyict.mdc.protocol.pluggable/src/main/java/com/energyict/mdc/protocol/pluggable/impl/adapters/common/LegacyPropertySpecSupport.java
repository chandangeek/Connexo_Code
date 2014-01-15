package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.ValueFactory;
import com.energyict.mdc.dynamic.impl.BasicPropertySpec;
import com.energyict.mdc.dynamic.impl.ReferencePropertySpec;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeValueSelectionMode;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides support to convert {@link com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec legacy PropertySpecs}
 * to the new {@link PropertySpec} or vice versa.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-03 (14:27)
 */
public final class LegacyPropertySpecSupport {

    @SuppressWarnings("unchecked")
    public static PropertySpec toPropertySpec (com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec legacySpec, boolean required) {
        if (legacySpec.isReference()) {
            ReferencePropertySpec propertySpec =
                    new ReferencePropertySpec(legacySpec.getName(), false, legacySpec.getObjectFactory());
            propertySpec.setRequired(required);
            return propertySpec;
        }
        else {
            // Basic
            BasicPropertySpec propertySpec =
                    new BasicPropertySpec(
                            legacySpec.getName(),
                            newInstance(LegacyValueFactoryMapping.classForLegacy(legacySpec.getValueFactory().getClass())));
            propertySpec.setRequired(required);
            return propertySpec;
        }
    }

    public static List<PropertySpec> toPropertySpecs (List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> legacyPropertySpecs, boolean required){
        List<PropertySpec> propertySpecs = new ArrayList<>();
        for (com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec legacyPropertySpec : legacyPropertySpecs) {
            propertySpecs.add(toPropertySpec(legacyPropertySpec, required));
        }
        return propertySpecs;
    }

    public static com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec toLegacyPropertySpec (PropertySpec newSpec) {
        if (newSpec.isReference()) {
            return PropertySpecBuilder.
                    forReference(
                            newSpec.getValueFactory().getValueType(),
                            new ValueDomain(Environment.DEFAULT.get().findFactory(newSpec.getValueFactory().getValueType().getName())),
                            AttributeValueSelectionMode.SEARCH_AND_SELECT).
                    name(newSpec.getName()).
                    finish();
        }
        else {
            return PropertySpecBuilder.
                    forClass(
                            newSpec.getValueFactory().getValueType(),
                            newLegacyInstance(LegacyValueFactoryMapping.lecacyClassFor(newSpec.getValueFactory().getClass()))).
                    name(newSpec.getName()).
                    finish();
        }
    }

    public static List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> toLegacyPropertySpecs(List<PropertySpec> newPropertySpecs){
        List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> legacyPropertySpecs = new ArrayList<>();
        for (PropertySpec newPropertySpec : newPropertySpecs) {
            legacyPropertySpecs.add(toLegacyPropertySpec(newPropertySpec));
        }
        return legacyPropertySpecs;
    }

    private static ValueFactory newInstance (Class<ValueFactory> valueFactoryClass) {
        try {
            return valueFactoryClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(e, valueFactoryClass);
        }
    }

    private static com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory newLegacyInstance (Class<com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory> valueFactoryClass) {
        try {
            return valueFactoryClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(e, valueFactoryClass);
        }
    }

}