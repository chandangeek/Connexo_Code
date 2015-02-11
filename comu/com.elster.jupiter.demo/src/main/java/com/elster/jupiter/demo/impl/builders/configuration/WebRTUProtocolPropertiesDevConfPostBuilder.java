package com.elster.jupiter.demo.impl.builders.configuration;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import java.util.function.Consumer;

public class WebRTUProtocolPropertiesDevConfPostBuilder implements Consumer<DeviceConfiguration> {
    @Override
    public void accept(DeviceConfiguration configuration) {
        ProtocolDialectConfigurationProperties configurationProperties = configuration.getProtocolDialectConfigurationPropertiesList().get(0);
        configurationProperties.setProperty("NTASimulationTool", "1");
        configurationProperties.save();
    }
}
