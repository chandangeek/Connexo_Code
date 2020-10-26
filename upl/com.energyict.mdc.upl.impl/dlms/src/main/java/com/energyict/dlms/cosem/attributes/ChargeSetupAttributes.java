package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by H245796 on 18.12.2017.
 */
public enum ChargeSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    TOTAL_AMOUNT_PAID(2, 0x08),
    CHARGE_TYPE(3, 0x10),
    PRIORITY(4, 0x18),
    UNIT_CHARGE_ACTIVE(5, 0x20),
    UNIT_CHARGE_PASSIVE(6, 0x28),
    UNIT_CHARGE_ACTIVATION_TIME(7, 0x30),
    PERIOD(8, 0x38),
    CHARGE_CONFIGURATION(9, 0x40),
    LAST_COLLECTION_TIME(10, 0x48),
    LAST_COLLECTION_AMOUNT(11, 0x50),
    TOTAL_AMOUNT_REMAINING(12, 0x58),
    PROPORTION(13, 0x60);


    private final int attributeNumber;
    private final int shortName;

    private ChargeSetupAttributes(int attrNr, int sn) {
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
        return DLMSClassId.CHARGE_SETUP;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}