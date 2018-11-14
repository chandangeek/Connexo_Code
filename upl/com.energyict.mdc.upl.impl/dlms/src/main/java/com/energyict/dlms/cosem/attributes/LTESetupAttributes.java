package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;


public enum LTESetupAttributes implements DLMSClassAttributes{
    LOGICAL_NAME(1),
    APN(2),
    PIN_Code(3),
    QoS(4),
    ;

    private final int attributeNumber;
    private final int shortName;

    /**
     * Constructor for the attribute types
     * @param lnAttribute the LONG_NAME attribute number
     */
    private LTESetupAttributes(int lnAttribute){
        this.attributeNumber = lnAttribute;
        this.shortName = (this.attributeNumber - 1) *8;
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
        return DLMSClassId.GPRS_SETUP;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

}
