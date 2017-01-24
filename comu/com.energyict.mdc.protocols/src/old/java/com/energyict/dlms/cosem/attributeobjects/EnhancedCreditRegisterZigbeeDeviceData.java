package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 4/08/11
 * Time: 8:44
 */
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