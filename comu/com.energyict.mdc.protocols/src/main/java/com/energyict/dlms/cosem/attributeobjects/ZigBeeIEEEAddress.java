/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.OctetString;

public class ZigBeeIEEEAddress extends OctetString {

    public ZigBeeIEEEAddress(byte[] address) {
        super(address, address.length, 0);
    }

}
