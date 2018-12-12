package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by H245796 on 18.12.2017.
 */
public enum NTPSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    ACTIVATED(2, 0x08),
    SERVER_ADDRESS(3, 0x10),
    SERVER_PORT(4, 0x18),
    AUTHENTICATION_METHOD(5, 0x20),
    AUTHENTICATION_KEYS(6, 0x28);

    private final int attributeNumber;
    private final int shortName;

    private NTPSetupAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.NTP_SETUP;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static NPTServerAddressAttributes findByAttributeNumber(int attribute) {
        for (NPTServerAddressAttributes limiterAttribute : NPTServerAddressAttributes.values()) {
            if (limiterAttribute.getAttributeNumber() == attribute) {
                return limiterAttribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attribute);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}