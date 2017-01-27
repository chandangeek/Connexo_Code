package com.energyict.protocolimplv2.dialects;

import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 8:50
 */
public abstract class AbstractDeviceProtocolDialect implements DeviceProtocolDialect {

    protected final PropertySpecService propertySpecService;

    public AbstractDeviceProtocolDialect(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }
}