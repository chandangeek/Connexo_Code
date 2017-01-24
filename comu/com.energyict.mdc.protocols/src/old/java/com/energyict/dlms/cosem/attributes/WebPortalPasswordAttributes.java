package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;


/**
 * Copyrights EnergyICT
 * Date: 14-dec-2010
 * Time: 14:19:55
 */
public enum WebPortalPasswordAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    LOGIN_USER_1(2, 0x08),
    LOGIN_USER_2(3, 0x10);

    private final int attributeNumber;
    private final int shortName;

    private WebPortalPasswordAttributes(int attrNr, int sn) {
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
        return DLMSClassId.WEB_PORTAL_PASSWORDS;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static WebPortalPasswordAttributes findByAttributeNumber(int attribute) {
        for (WebPortalPasswordAttributes limiterAttribute : WebPortalPasswordAttributes.values()) {
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