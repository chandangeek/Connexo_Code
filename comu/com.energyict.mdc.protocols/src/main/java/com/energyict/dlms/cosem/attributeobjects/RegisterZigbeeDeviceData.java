/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

public class RegisterZigbeeDeviceData extends Structure {

    public RegisterZigbeeDeviceData(String address, String linkKey) throws IOException {
        this(new ZigBeeIEEEAddress(DLMSUtils.getBytesFromHexString(address, "")), OctetString.fromByteArray(DLMSUtils.getBytesFromHexString(linkKey, "")));
    }

    public RegisterZigbeeDeviceData(ZigBeeIEEEAddress address, OctetString linkKey) throws IOException {
        super(address, linkKey);
        validateLinkKey(linkKey);
        validateAddress(address);
    }

    protected void validateAddress(ZigBeeIEEEAddress address) throws IOException {
        if (address == null) {
            throw new ProtocolException("ZigBeeIEEEAddress is required and cannot be 'null'");
        }
    }

    protected void validateLinkKey(OctetString linkKey) throws IOException {
        if (linkKey == null) {
            throw new ProtocolException("Link key is required and cannot be 'null'");
        }

        int length = linkKey.getOctetStr().length;
        if (length != 16) {
            throw new ProtocolException("Link key length should be 16 bytes but was [" + length + "]!");
        }
    }
}
