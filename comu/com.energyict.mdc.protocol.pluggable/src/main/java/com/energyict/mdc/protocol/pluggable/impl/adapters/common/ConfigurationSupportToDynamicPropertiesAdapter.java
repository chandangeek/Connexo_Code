package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.dynamic.HasDynamicProperties;
import com.energyict.mdc.dynamic.PropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts components that implement the {@link ConfigurationSupport} interface
 * to comply with the {@link HasDynamicProperties} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-03 (10:05)
 */
public class ConfigurationSupportToDynamicPropertiesAdapter implements HasDynamicProperties {
    private ConfigurationSupport configurationSupport;

    public ConfigurationSupportToDynamicPropertiesAdapter (ConfigurationSupport configurationSupport) {
        this.configurationSupport = configurationSupport;
    }

    public ConfigurationSupport getConfigurationSupport() {
        return configurationSupport;
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.addAll(this.toPropertySpecs(this.configurationSupport.getRequiredProperties(), true));
        propertySpecs.addAll(this.toPropertySpecs(this.configurationSupport.getOptionalProperties(), false));
        return propertySpecs;
    }

    private List<PropertySpec> toPropertySpecs (List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> legacySpecs, boolean required) {
        List<PropertySpec> newSpecs = new ArrayList<>(legacySpecs.size());
        for (com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec legacySpec : legacySpecs) {
            newSpecs.add(LegacyPropertySpecSupport.toPropertySpec(legacySpec, required));
        }
        return newSpecs;
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        for (PropertySpec propertySpec : this.getPropertySpecs()) {
            if (name.equals(propertySpec.getName())) {
                return propertySpec;
            }
        }
        return null;
    }

}