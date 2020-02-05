package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

public enum PPPSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1),
    PHY_REFERENCE(2),
    LCP_OPTIONS(3),
    IPCP_OPTIONS(4),
    PPP_AUTHENTICATION(5);

    private final int attributeNumber;
    private final int shortName;

    PPPSetupAttributes(int attributeNumber) {
        this.attributeNumber = attributeNumber;
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

    @Override
    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PPP_SETUP;
    }

    @Override
    public int getShortName() {
        return shortName;
    }
}
