package com.energyict.protocolimpl.dlms.prime;

/**
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class Cirwatt extends AbstractPrimeMeter {

    @Override
    public String getProtocolDescription() {
        return "Circutor Cirwatt B 410D PLC PRIME DLMS";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }
}
