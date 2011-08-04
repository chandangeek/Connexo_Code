package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;

/**
 * Copyrights EnergyICT
 * Date: 4/08/11
 * Time: 8:44
 */
public class RegisterZigbeeDeviceData extends Structure {

    public RegisterZigbeeDeviceData(String address, String linkKey) throws IOException {
        this(new ZigBeeIEEEAddress(getBytesFromHexString(address)), new OctetString(getBytesFromHexString(linkKey), true));
    }

    public RegisterZigbeeDeviceData(ZigBeeIEEEAddress address, OctetString linkKey) throws IOException {
        super(address, linkKey);
        validateLinkKey(linkKey);
        validateAddress(address);
    }

    private void validateAddress(ZigBeeIEEEAddress address) throws IOException {
        if (address == null) {
            throw new IOException("ZigBeeIEEEAddress is required and cannot be 'null'");
        }
    }

    private void validateLinkKey(OctetString linkKey) throws IOException {
        if (linkKey == null) {
            throw new IOException("Link key is required and cannot be 'null'");
        }

        int length = linkKey.stringValue().length();
        if (length != 16) {
            throw new IOException("Link key length should be 16 bytes but was [" + length + "]!");
        }
    }

}
