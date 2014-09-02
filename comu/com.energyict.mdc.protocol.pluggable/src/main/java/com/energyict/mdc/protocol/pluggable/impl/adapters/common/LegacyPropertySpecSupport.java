package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdw.cpo.PropertySpecBuilder;

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
        return propertySpecService.basicPropertySpec(
                legacySpec.getName(),
                required,
                newInstance(LegacyValueFactoryMapping.classForLegacy(legacySpec.getValueFactory().getClass())));
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