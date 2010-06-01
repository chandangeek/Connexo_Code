package com.energyict.genericprotocolimpl.nta.iskra;

import com.energyict.genericprotocolimpl.webrtukp.WebRTUKP;

/**
 * This is the subclass for the Iskra NTA device.
 *
 * Copyrights EnergyICT
 * Date: 28-mei-2010
 * Time: 15:35:36
 */
public class Mx382 extends WebRTUKP{

    @Override
    public String getVersion() {
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }
}
