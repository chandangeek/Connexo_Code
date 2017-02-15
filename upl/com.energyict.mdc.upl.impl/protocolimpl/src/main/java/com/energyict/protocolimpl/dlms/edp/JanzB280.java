package com.energyict.protocolimpl.dlms.edp;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 11:11
 * Author: khe
 */
public class JanzB280 extends CX20009 {

    public JanzB280(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-02-18 16:12:07 +0100 (di, 18 feb 2014) $";
    }

}