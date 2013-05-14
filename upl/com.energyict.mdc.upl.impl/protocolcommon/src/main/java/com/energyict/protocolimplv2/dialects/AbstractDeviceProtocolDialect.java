package com.energyict.protocolimplv2.dialects;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.tasks.DeviceProtocolDialect;

/**
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 8:50
 */
public abstract class AbstractDeviceProtocolDialect implements DeviceProtocolDialect {

    @Override
    public boolean isRequiredProperty(String name) {
        for (PropertySpec propertySpec : getRequiredProperties()) {
            if (propertySpec.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
