package com.energyict.protocolimpl.modbus.enerdis.enerium50;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/10/11
 * Time: 14:38
 */
public class Enerium50 extends Enerium200 {

    @Override
    protected List doTheGetOptionalKeys() {
        List returnList = new ArrayList();
        return returnList;
    }

    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "Enerium 50 " + getMeterInfo().getVersion();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-05-02 09:42:35 +0200 (do, 02 mei 2013) $";
    }
}
