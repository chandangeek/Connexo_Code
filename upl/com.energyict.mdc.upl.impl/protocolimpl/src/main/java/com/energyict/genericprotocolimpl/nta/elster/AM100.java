package com.energyict.genericprotocolimpl.nta.elster;

import com.energyict.genericprotocolimpl.webrtukp.WebRTUKP;

/**
 * This is the subclass for the AM100.
 * The AM100 implements the NTA spec, but some things were not fully compliant with the original implementation.
 * 
 * Copyrights EnergyICT
 * Date: 28-mei-2010
 * Time: 13:56:15
 */
public class AM100 extends WebRTUKP{


    @Override
    public String getVersion() {
//        return super.getVersion();    //To change body of overridden methods use File | Settings | File Templates.
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }
}
