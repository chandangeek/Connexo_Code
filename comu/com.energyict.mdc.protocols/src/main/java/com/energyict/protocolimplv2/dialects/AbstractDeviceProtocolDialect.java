package com.energyict.protocolimplv2.dialects;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

/**
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 8:50
 */
public abstract class AbstractDeviceProtocolDialect implements DeviceProtocolDialect {
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
