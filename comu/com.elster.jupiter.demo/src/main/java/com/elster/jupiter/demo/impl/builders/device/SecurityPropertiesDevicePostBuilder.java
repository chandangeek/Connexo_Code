package com.elster.jupiter.demo.impl.builders.device;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class SecurityPropertiesDevicePostBuilder implements Consumer<Device> {

    @Override
    public void accept(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        for (SecurityPropertySet securityPropertySet : configuration.getSecurityPropertySets()) {
            TypedProperties typedProperties = TypedProperties.empty();
            typedProperties.setProperty("ClientMacAddress", new BigDecimal(1));
            device.setSecurityProperties(securityPropertySet, typedProperties);
        }
    }
}
