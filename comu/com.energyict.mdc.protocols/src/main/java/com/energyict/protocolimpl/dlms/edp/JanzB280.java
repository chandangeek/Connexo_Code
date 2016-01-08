package com.energyict.protocolimpl.dlms.edp;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 11:11
 * Author: khe
 */
public class JanzB280 extends CX20009 {

    @Inject
    public JanzB280(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService, ormClient);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-02-18 16:12:07 +0100 (di, 18 feb 2014) $";
    }

}