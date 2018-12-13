package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public enum UsbSetupAttribute implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    USB_STATE(2, 0x08),
    USB_ACTIVITY(3, 0x10),
    LAST_ACTIVITY_TIMESTAMP(4, 0x18);

    private final int attributeNumber;
    private final int shortName;

    private UsbSetupAttribute(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.USB_SETUP;
    }

    public int getShortName() {
        return shortName;
    }

    /**
     * @param attributeNumber
     * @return
     */
    public static UsbSetupAttribute findByAttributeNumber(int attributeNumber) {
        for (UsbSetupAttribute attribute : UsbSetupAttribute.values()) {
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
    public static UsbSetupAttribute findByShortName(int shortName) {
        for (UsbSetupAttribute attribute : UsbSetupAttribute.values()) {
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
