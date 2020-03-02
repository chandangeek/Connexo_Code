package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public enum NbiotPushSchedulerAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    EXECUTED_SCRIPT(2, 0x08),
    TYPE(3, 0x10),
    EXECUTION_TIME(4, 0x18);

    private final int attributeNumber;
    private final int shortName;

    private NbiotPushSchedulerAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.SINGLE_ACTION_SCHEDULE;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public int getShortName() {
        return shortName;
    }

    /**
     * @param attributeNumber
     * @return
     */
    public static NbiotPushSchedulerAttributes findByAttributeNumber(int attributeNumber) {
        for (NbiotPushSchedulerAttributes attribute : NbiotPushSchedulerAttributes.values()) {
            if (attribute.getAttributeNumber() == attributeNumber) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attributeNumber);
    }

    /**
     * @param shortName
     * @return
     */
    public static NbiotPushSchedulerAttributes findByShortName(int shortName) {
        for (NbiotPushSchedulerAttributes attribute : NbiotPushSchedulerAttributes.values()) {
            if (attribute.getShortName() == shortName) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No shortName found for id = " + shortName);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }


}
