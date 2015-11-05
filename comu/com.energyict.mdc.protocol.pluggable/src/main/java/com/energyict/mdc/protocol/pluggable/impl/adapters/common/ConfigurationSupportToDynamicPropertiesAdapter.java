package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;

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
    private PropertySpecService propertySpecService;
    private ConfigurationSupport configurationSupport;

    public ConfigurationSupportToDynamicPropertiesAdapter(PropertySpecService propertySpecService, ConfigurationSupport configurationSupport) {
        super();
        this.propertySpecService = propertySpecService;
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
            newSpecs.add(LegacyPropertySpecSupport.toPropertySpec(this.propertySpecService, legacySpec, required));
        }
        return newSpecs;
    }

}