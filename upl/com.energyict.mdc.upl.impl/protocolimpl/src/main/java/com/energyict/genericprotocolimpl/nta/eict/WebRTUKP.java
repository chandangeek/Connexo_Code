package com.energyict.genericprotocolimpl.nta.eict;

/**
 * Copyrights EnergyICT
 * Date: 1-jun-2010
 * Time: 11:58:24
 */
public class WebRTUKP extends com.energyict.genericprotocolimpl.webrtukp.WebRTUKP{

    @Override
    public String getVersion() {
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }

}
