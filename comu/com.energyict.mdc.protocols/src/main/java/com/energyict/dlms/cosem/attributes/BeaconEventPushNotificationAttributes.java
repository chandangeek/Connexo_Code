/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.dlms.cosem.attributes;


import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum BeaconEventPushNotificationAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    SEND_DESTINATION_AND_METHOD(2, 0x08),
    IS_ENABLED(4, 0x18);

    private final int attributeNumber;
    private final int shortName;

    private BeaconEventPushNotificationAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.EVENT_NOTIFICATION;
    }

    public int getShortName() {
        return shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}