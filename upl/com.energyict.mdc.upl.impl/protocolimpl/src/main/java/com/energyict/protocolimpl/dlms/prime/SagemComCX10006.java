package com.energyict.protocolimpl.dlms.prime;

/**
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class SagemComCX10006 extends AbstractPrimeMeter {

    @Override
    public String getProtocolDescription() {
        return "SagemCom CX10006 PLC PRIME DLMS";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }
}
