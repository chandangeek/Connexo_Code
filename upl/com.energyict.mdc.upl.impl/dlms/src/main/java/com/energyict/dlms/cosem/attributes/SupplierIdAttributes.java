package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Copyrights EnergyICT
 * Date: 16-aug-2011
 * Time: 13:11:21
 */
public enum SupplierIdAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    VALUE(2, 0x08),
    SCALER_UNIT(3, 0x10),
    PASSIVE_VALUE(4, 0x18),
    PASSIVE_SCALER_UNIT(5, 0x20),
    ACTIVATION_TIME(6, 0x28),;

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute
     */
    private final int shortName;

    SupplierIdAttributes(final int attributeNumber, final int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.SUPPLIER_ID;
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
}
