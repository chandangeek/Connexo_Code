/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;

import java.io.IOException;

public class EnhancedCreditRegisterZigbeeDeviceData extends RegisterZigbeeDeviceData {

    public EnhancedCreditRegisterZigbeeDeviceData(String address, String linkKey) throws IOException {
        this(new ZigBeeIEEEAddress(DLMSUtils.getBytesFromHexString(address, "")), OctetString.fromByteArray(DLMSUtils.getBytesFromHexString(linkKey, "")));
    }

    public EnhancedCreditRegisterZigbeeDeviceData(ZigBeeIEEEAddress address, OctetString linkKey) throws IOException {
        super(address, linkKey);                // address - link key
        super.addDataType(new TypeEnum(0));     // device type enum
        validateLinkKey(linkKey);
        validateAddress(address);
    }
}