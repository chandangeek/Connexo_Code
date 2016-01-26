package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class Cirwatt extends AbstractPrimeMeter {

    @Override
    public String getProtocolDescription() {
        return "Circutor Cirwatt B 410D DLMS (PRIME1.5)";
    }

    @Inject
    public Cirwatt(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}