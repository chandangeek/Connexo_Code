package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public enum NbiotOrphanStateAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    THRESHOLDS(2, 0x08),
    MONITOR_VALUE(3, 0x10),
    ACTIONS(4, 0x18);

    private final int attributeNumber;
    private final int shortName;

    private NbiotOrphanStateAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.REGISTER_MONITOR;
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
    public static NbiotOrphanStateAttributes findByAttributeNumber(int attributeNumber) {
        for (NbiotOrphanStateAttributes attribute : NbiotOrphanStateAttributes.values()) {
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
    public static NbiotOrphanStateAttributes findByShortName(int shortName) {
        for (NbiotOrphanStateAttributes attribute : NbiotOrphanStateAttributes.values()) {
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
