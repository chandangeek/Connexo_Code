package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 14-dec-2010
 * Time: 14:19:55
 */
public enum NetworkManagementAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    NETWORK_MGMT_PARAMETERS(2, 0x08);

    private final int attributeNumber;
    private final int shortName;

    private NetworkManagementAttributes(int attrNr, int sn) {
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
        return DLMSClassId.NETWORK_MANAGEMENT;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }
    
    public static NetworkManagementAttributes findByAttributeNumber(int attribute){
        for(NetworkManagementAttributes limiterAttribute : NetworkManagementAttributes.values()){
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
