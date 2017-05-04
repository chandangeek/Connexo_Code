/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;

import javax.inject.Inject;

public class SagemComCX10006 extends AbstractPrimeMeter {

    @Override
    public String getProtocolDescription() {
        return "Sagemcom CX10006 DLMS (PRIME1.5)";
    }

    @Inject
    public SagemComCX10006(PropertySpecService propertySpecService, DeviceMessageFileService deviceMessageFileService) {
        super(propertySpecService, deviceMessageFileService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}