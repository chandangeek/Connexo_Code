package com.energyict.protocolimpl.modbus.enerdis.enerium50;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/10/11
 * Time: 14:38
 */
public class Enerium50 extends Enerium200 {

    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "Enerium 50 " + getMeterInfo().getVersion();
    }
}
