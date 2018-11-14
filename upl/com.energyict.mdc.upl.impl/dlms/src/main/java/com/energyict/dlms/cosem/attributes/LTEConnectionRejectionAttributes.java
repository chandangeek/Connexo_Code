package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * LTE Connection Rejection - class 1
 */
public enum LTEConnectionRejectionAttributes implements DLMSClassAttributes {
    LOGICAL_NAME(1),
    /**
     * Attribute id: LAST_REJECT_CAUSE [71].
     */
    VALUE(2),
    /**
     * Attribute id: LAST_REJECT_CAUSE [71].
     */
    LAST_REJECT_CAUSE(71),
    /**
     * Attribute id: LAST_REJECTED_MCC [72].
     */
    LAST_REJECTED_MCC(72),

    /**
     * Attribute id: LAST_REJECTED_MNC [73].
     */
    LAST_REJECTED_MNC(73),

    /**
     * Attribute id: TIMESTAMP_LAST_REJECTION [74].
     */
    TIMESTAMP_LAST_REJECTION(74);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Constructor for the attribute types
     * @param lnAttribute the LONG_NAME attribute number
     */
    private LTEConnectionRejectionAttributes(int lnAttribute){
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
        return DLMSClassId.MAC_ADDRESS_SETUP;
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
