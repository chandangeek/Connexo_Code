package com.energyict.protocolimpl.modbus.enerdis.enerium50;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Clock;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/10/11
 * Time: 14:38
 */
public class Enerium50 extends Enerium200 {

    @Inject
    public Enerium50(PropertySpecService propertySpecService, Clock clock) {
        super(propertySpecService, clock);
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