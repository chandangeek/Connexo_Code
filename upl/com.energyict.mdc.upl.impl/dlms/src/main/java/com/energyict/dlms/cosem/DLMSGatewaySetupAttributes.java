package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;
import com.energyict.obis.ObisCode;

public enum DLMSGatewaySetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    IS_ACTIVE(2, 0x08),
    PREEMPT_DC(3, 0x10),
    NOTIFICATION_RELAYING(4, 0x18),
    NOTIFICATION_DECIPHER(5, 0x20),
    NOTIFICATION_DROP_UNENCRYPTED(6, 0x28);

    private final int attributeNumber;
    private final int shortName;

    private DLMSGatewaySetupAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    @Override
    public int getAttributeNumber() {
        return attributeNumber;
    }

    @Override
    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    @Override
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.DLMS_GATEWAY_SETUP;
    }

    @Override
    public int getShortName() {
        return shortName;
    }
}
