package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by H245796 on 18.12.2017.
 */
public enum CreditSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    CURRENT_CREDIT_AMOUNT(2, 0x08),
    CREDIT_TYPE(3, 0x10),
    PRIORITY(4, 0x18),

    WARNING_THRESHOLD(5, 0x20),
    LIMIT(6, 0x28),
    CREDIT_CONFIGURATION(7, 0x30),
    CREDIT_STATUS(8, 0x38),

    PRESET_CREDIT_AMOUNT(9, 0x38),
    CREDIT_AVAILABLE_THRESHOLD(10, 0x40),
    PERIOD(11, 0x48);

    private final int attributeNumber;
    private final int shortName;

    private CreditSetupAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
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
        return DLMSClassId.CREDIT_SETUP;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}