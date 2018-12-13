package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * VPN setup IC, attributes
 * class id = 20029, version = 0, logical name = 0-160:96.128.0.255 (00A0608000FF)
 * The VPN setup IC is a manufacturer-specific COSEM IC which can be used for configuring and quering the VPN link status. Changes made to the attributes of this IC take effect after
 * closing the DLMS association, or after invoking the refresh_vpn_config method.
 */
public enum VPNSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1,0x00),
    VPN_ENABLED(2,0x08),
    VPN_TYPE(3,0x10),
    GATEWAY_ADDRESS(4,0x18),
    AUTHENTICATION_TYPE(5,0x20),
    LOCAL_IDENTIFIER(6,0x28),
    REMOTE_IDENTIFIER(7,0x30),
    LOCAL_CERTIFICATE(8,0x38),
    REMOTE_CERTIFICATE(9,0x40),
    SHARED_SECRET(10,0x48),
    REQUEST_VIRTUAL_IP(11,0x50),
    COMPRESSION_ENABLED(12,0x58),
    VPN_STATUS(13,0x60);

    private final int attributeNumber;
    private final int shortName;

    private VPNSetupAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.VPN_SETUP;
    }

    public int getShortName() {
        return shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}
