/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import java.util.Optional;

public class ProtocolDialectPropertyChangeItem extends AbstractConfigChangeItem {

    private static final ProtocolDialectPropertyChangeItem INSTANCE = new ProtocolDialectPropertyChangeItem();

    private ProtocolDialectPropertyChangeItem() {
    }

    static ProtocolDialectPropertyChangeItem getInstance() {
        return INSTANCE;
    }

    @Override
    public void apply(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        device.getProtocolDialectPropertiesList().stream().forEach(protocolDialectProperties -> {
            final Optional<ProtocolDialectConfigurationProperties> matchedConfigDialect = destinationDeviceConfiguration.getProtocolDialectConfigurationPropertiesList().stream().filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getDeviceProtocolDialectName().equals(protocolDialectProperties.getDeviceProtocolDialectName())).findAny();
            matchedConfigDialect.ifPresent(((ServerProtocolDialectForConfigChange) protocolDialectProperties)::setNewProtocolDialectConfigurationProperties);
        });
    }
}
