package com.energyict.protocolimplv2.nta.elster;

import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;

import java.util.List;

/**
 * The AM100 implementation of the NTA spec
 *
 * @author sva
 * @since 30/10/12 (9:58)
 */
public class AM100 extends WebRTUKP {

    @Override
    public String getProtocolDescription() {
        return "Elster AS220/AS1440 AM100 DLMS (PRE-NTA)";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    /**
     * The AM100 also supports the AT modem
     */
    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = super.getSupportedConnectionTypes();
        result.add(new SioAtModemConnectionType());
        result.add(new RxTxAtModemConnectionType());
        return result;
    }
}