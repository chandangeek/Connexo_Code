/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;

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

    public ConfigurationSupportToDynamicPropertiesAdapter(ConfigurationSupport configurationSupport) {
        super();
        this.configurationSupport = configurationSupport;
    }

    public ConfigurationSupport getConfigurationSupport() {
        return configurationSupport;
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.addAll(this.configurationSupport.getRequiredProperties());
        propertySpecs.addAll(this.configurationSupport.getOptionalProperties());
        return propertySpecs;
    }

}