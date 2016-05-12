package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:32
 * Author: khe
 */
public class PrimeMeter extends AbstractPrimeMeter {

    @Override
    public String getProtocolDescription() {
        return "Generic PRIME E-Meter DLMS (PRIME1.5)";
    }

    @Inject
    public PrimeMeter(PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService) {
        super(propertySpecService, deviceConfigurationService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}