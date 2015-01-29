package com.elster.jupiter.demo.impl.pp;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import java.util.function.Consumer;

public class ProtocolPropertiesMPP implements Consumer<DeviceConfiguration> {
    @Override
    public void accept(DeviceConfiguration configuration) {
        ProtocolDialectConfigurationProperties configurationProperties = configuration.getProtocolDialectConfigurationPropertiesList().get(0);
        configurationProperties.setProperty("NTASimulationTool", "1");
        configurationProperties.setProperty("SecurityLevel", "0:0");
        configurationProperties.save();
    }
}
