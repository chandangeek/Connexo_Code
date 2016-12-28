package com.energyict.protocolimpl.modbus.enerdis.enerium50;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/10/11
 * Time: 14:38
 */
public class Enerium50 extends Enerium200 {

    public Enerium50(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "Enerium 50 " + getMeterInfo().getVersion();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-05-02 09:42:35 +0200 (do, 02 mei 2013) $";
    }

}