/**
 *
 */
package com.energyict.dlms.cosem.attributes;


import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

public enum EventPushNotificationAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    PUSH_OBJECT_LIST(2, 0x08),
    SEND_DESTINATION_AND_METHOD(3, 0x10),
    NOTIFICATION_CYPHERING(-3, 0x38);

    private final int attributeNumber;
    private final int shortName;

    private EventPushNotificationAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP;
    }

    public int getShortName() {
        return shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}