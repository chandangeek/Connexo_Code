package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;
import com.energyict.obis.ObisCode;

/**
 * Created by iulian on 8/17/2016.
 */
public enum MBusDiagnosticAttributes implements DLMSClassAttributes {
    LOGICAL_NAME            (1, 0x00),
    RSSI                    (2, 0x08),
    CHANNEL_ID              (3, 0x10),
    LINK_STATUS             (4, 0x18),
    BROADCAST_FRAME_COUNTER (5, 0x20),
    TRANSMISSION_COUNTER    (6, 0x28),
    FCS_OK_FRAMESC_COUNTER  (7, 0X30),
    FCS_NOK_FRAMESC_COUNTER (8, 0X38),
    CAPTURE_TIME            (9, 0X40);

    private final int attributeNumber;
    private final int shortName;

    MBusDiagnosticAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.MBUS_DIAGNOSTIC;
    }

    public int getShortName() {
        return shortName;
    }
}
