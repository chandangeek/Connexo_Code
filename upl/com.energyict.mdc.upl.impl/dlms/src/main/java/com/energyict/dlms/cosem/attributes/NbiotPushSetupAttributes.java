package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public enum NbiotPushSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    PUSH_OBJECT_LIST(2, 0x08),
    SEND_DESTINATION_AND_METHOD(3, 0x10),
    COMMUNICATION_WINDOW(4, 0x18),
    RANDOMIZATION_START_INTERVAL(5, 0x20),
    NUMBER_OF_RETRIES(6, 0x28),
    REPETITION_DELAY(7, 0x30),
    PUSH(8, 0x38);

    private final int attributeNumber;
    private final int shortName;

    private NbiotPushSetupAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP;
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
    public static NbiotPushSetupAttributes findByAttributeNumber(int attributeNumber) {
        for (NbiotPushSetupAttributes attribute : NbiotPushSetupAttributes.values()) {
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
    public static NbiotPushSetupAttributes findByShortName(int shortName) {
        for (NbiotPushSetupAttributes attribute : NbiotPushSetupAttributes.values()) {
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
