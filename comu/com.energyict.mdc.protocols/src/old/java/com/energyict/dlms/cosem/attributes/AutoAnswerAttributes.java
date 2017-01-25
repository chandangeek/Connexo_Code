/**
 *
 */
package com.energyict.dlms.cosem.attributes;


import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum AutoAnswerAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    MODE(2, 0x08),
    LIST_OF_ALLOWED_CALLERS(7, 0x30);

    private final int attributeNumber;
    private final int shortName;

    private AutoAnswerAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.AUTO_ANSWER;
    }

    public int getShortName() {
        return shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}