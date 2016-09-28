package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by astor on 18.09.2016.
 */
public enum GprsModemSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    APN(2, 0x08),
    PIN_CODE(3, 0x10),
    QUALITY_OF_SERVICE(4, 0x18),
    NETWORK_SELECTION_MODE(-1, 0x20),
    PREFERRED_OPERATOR_LIST(-2, 0x28),
    INTL_ROAMING_ALLOWED(-4, 0x30),
    MINIMUM_RSSI(-5, 0x38),
    MAXIMUM_BER(-6, 0x40),
    NETWORK_TECHNOLOGY(-7, 0x48),
    IS_GPRS_PREFERRED(-8, 0x40);

    private final int attributeNumber;
    private final int shortName;

    private GprsModemSetupAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.GPRS_SETUP;
    }

    public int getShortName() {
        return shortName;
    }

    /**
     * @param attributeNumber
     * @return
     */
    public static GprsModemSetupAttributes findByAttributeNumber(int attributeNumber) {
        for (GprsModemSetupAttributes attribute : GprsModemSetupAttributes.values()) {
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
    public static GprsModemSetupAttributes findByShortName(int shortName) {
        for (GprsModemSetupAttributes attribute : GprsModemSetupAttributes.values()) {
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

