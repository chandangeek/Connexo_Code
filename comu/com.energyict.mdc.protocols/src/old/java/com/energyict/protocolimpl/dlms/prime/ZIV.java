package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;

import javax.inject.Inject;

/**
 * Class for the PRIME meter ZIV 5CTM - E2C
 *
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class ZIV extends AbstractPrimeMeter {

    @Override
    public String getProtocolDescription() {
        return "Ziv 5CTM E2C DLMS (PRIME1.5)";
    }

    @Inject
    public ZIV(PropertySpecService propertySpecService, DeviceMessageFileService deviceMessageFileService) {
        super(propertySpecService, deviceMessageFileService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}