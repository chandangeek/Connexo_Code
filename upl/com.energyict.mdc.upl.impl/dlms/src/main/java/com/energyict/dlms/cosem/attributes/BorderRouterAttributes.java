package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Border Router Setup IC attributes
 * class id = 20020, version = 0, logical name = 0-168:96.176.0.255 (00A860B000FF)
 * The border router setup IC allows for configuring the G3-PLC layer 3 router functionality.
 */
public enum BorderRouterAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1,0x00),
    IS_ACTIVE(2,0x08),
    ROUTING_ENTRIES(3,0x10),
    NAT_CONFIGURATION(4,0x18),
    PREFIX_DELEGATION(5,0x20),
    RA_CONFIG(6,0x28),
    RIP_CONFIG(7,0x30),
    ;

    private final int attributeNumber;
    private final int shortName;

    private BorderRouterAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.BORDER_ROUTER;
    }

    public int getShortName() {
        return shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}
