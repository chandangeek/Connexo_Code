/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;

import javax.inject.Inject;

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