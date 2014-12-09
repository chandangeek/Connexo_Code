package com.energyict.protocolimplv2.nta.elster;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;

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
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }
}