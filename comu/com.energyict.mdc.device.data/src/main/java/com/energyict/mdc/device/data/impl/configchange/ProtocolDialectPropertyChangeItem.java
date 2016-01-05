package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 05.10.15
 * Time: 13:08
 */
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
