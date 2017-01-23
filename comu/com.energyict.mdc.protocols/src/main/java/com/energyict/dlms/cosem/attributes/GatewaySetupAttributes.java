package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;


/**
 * Copyrights EnergyICT
 * Date: 14-dec-2010
 * Time: 14:19:55
 */
public enum GatewaySetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    METER_WHITELIST(2, 0x08),
    OPERATING_WINDOW_START_TIME(3, 0x10),
    OPERATING_WINDOW_END_TIME(4, 0x18),
    WHITELIST_ENABLED(5, 0x20),
    OPERATING_WINDOW_ENABLED(6, 0x28);

    private final int attributeNumber;
    private final int shortName;

    private GatewaySetupAttributes(int attrNr, int sn) {
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
        return DLMSClassId.GATEWAY_SETUP;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static GatewaySetupAttributes findByAttributeNumber(int attribute){
        for(GatewaySetupAttributes limiterAttribute : GatewaySetupAttributes.values()){
            if(limiterAttribute.getAttributeNumber() == attribute){
                return limiterAttribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attribute);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}
