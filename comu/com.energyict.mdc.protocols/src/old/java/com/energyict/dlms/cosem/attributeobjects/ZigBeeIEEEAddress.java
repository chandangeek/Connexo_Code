package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.OctetString;

/**
 * Copyrights EnergyICT
 * Date: 4/08/11
 * Time: 8:46
 */
public class ZigBeeIEEEAddress extends OctetString {

    public ZigBeeIEEEAddress(byte[] address) {
        super(address, address.length, 0);
    }

}
