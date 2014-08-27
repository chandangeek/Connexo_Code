package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeValueSelectionMode;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdw.cpo.PropertySpecBuilder;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;

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
    public static PropertySpec toPropertySpec(PropertySpecService propertySpecService, com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec legacySpec, boolean required) {
        if (legacySpec.isReference()) {
            return propertySpecService.referencePropertySpec(legacySpec.getName(), required, legacySpec.getObjectFactory());
        }
        else {
            return propertySpecService.basicPropertySpec(
                        legacySpec.getName(),
                        required,
                        newInstance(LegacyValueFactoryMapping.classForLegacy(legacySpec.getValueFactory().getClass())));
        }
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
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, valueFactoryClass);
        }
    }

    private static com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory newLegacyInstance (Class<com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory> valueFactoryClass) {
        try {
            return valueFactoryClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, valueFactoryClass);
        }
    }

}