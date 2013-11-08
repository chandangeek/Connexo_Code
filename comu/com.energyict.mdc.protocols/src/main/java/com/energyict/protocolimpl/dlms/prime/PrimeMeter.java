package com.energyict.protocolimpl.dlms.prime;

/**
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:32
 * Author: khe
 */
public class PrimeMeter extends AbstractPrimeMeter {

    @Override
    public String getProtocolDescription() {
        return "Generic Prime Meter (for RTU+Server usage)";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }
}
