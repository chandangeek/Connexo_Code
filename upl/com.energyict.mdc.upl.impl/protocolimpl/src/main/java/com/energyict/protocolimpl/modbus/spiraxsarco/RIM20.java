package com.energyict.protocolimpl.modbus.spiraxsarco;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Created by cisac on 11/19/2015.
 */
public class RIM20 extends VLM20 {
    public RIM20(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:02:16 +0200 (Thu, 26 Nov 2015)$";
    }

}