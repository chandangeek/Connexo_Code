package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;


/**
 * Copyrights EnergyICT
 * Date: 14-dec-2010
 * Time: 14:19:55
 */
public enum UplinkPingConfigurationAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    INTERVAL(2, 0x08),
    TIMEOUT(3, 0x10),
    DEST_ADDRESS(4, 0x18),
    ENABLE(5, 0x20);

    private final int attributeNumber;
    private final int shortName;

    private UplinkPingConfigurationAttributes(int attrNr, int sn) {
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
        return DLMSClassId.UPLINK_PING_SETUP;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static UplinkPingConfigurationAttributes findByAttributeNumber(int attribute) {
        for (UplinkPingConfigurationAttributes limiterAttribute : UplinkPingConfigurationAttributes.values()) {
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