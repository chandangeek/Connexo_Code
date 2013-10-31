package com.energyict.protocolimpl.dlms.prime;

/**
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class AS330D extends AbstractPrimeMeter {

    @Override
    public String getProtocolDescription() {
        return "Elster AS330D PLC PRIME DLMS";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }
}
