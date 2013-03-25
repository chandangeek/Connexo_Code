package com.energyict.protocolimpl.dlms.prime;

/**
 * Class for the PRIME meter ZIV 5CTM - E2C
 *
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class ZIV extends AbstractPrimeMeter {

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }
}
