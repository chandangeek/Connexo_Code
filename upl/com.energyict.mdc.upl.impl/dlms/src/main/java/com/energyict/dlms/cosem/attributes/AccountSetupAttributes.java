package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by H245796 on 18.12.2017.
 */
public enum AccountSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    MODE_AND_STATUS(2, 0x08),
    CURRENT_CREDIT_IN_USE(3, 0x10),
    CURRENT_CREDIT_STATUS(4, 0x18),
    AVAILABLE_CREDIT(5, 0x20),
    AMOUNT_TO_CLEAR(6, 0x28),
    CREDIT_REFERENCE_LIST(7, 0x30),
    CURRENCY(8, 0x38),
    NEXT_CREDIT_AVAILABLE_THRESHOLD(9, 0x40);

    private final int attributeNumber;
    private final int shortName;

    private AccountSetupAttributes(int attrNr, int sn) {
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
        return DLMSClassId.ACCOUNT_SETUP;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}