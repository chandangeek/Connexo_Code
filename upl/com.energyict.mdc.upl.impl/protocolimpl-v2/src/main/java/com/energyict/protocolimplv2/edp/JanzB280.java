package com.energyict.protocolimplv2.edp;

/**
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 11:11
 * Author: khe
 */
public class JanzB280 extends CX20009 {

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public String getProtocolDescription() {
        return "Janz B280 DLMS";
    }
}
