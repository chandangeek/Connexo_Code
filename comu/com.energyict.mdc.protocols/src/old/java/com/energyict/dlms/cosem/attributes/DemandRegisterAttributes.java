package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Straightforward summary of the DemandRegister attributes.
 * Note that the unit is attribute 4, instead of 3 (for registers)
 */
public enum DemandRegisterAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    CURRENT_AVG_VALUE(2, 0x08),
    LAST_AVG_VALUE(3, 0x10),
    UNIT(4, 0x18),
    STATUS(5, 0x20),
    CAPTURE_TIME(6, 0x28),
    START_TIME_CURRENT(7, 0x30),
    PERIOD(8, 0x38),
    NUMBER_OF_PERIODS(9, 0x40);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param shortName       the shortName of the attribute
     */
    private DemandRegisterAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
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
        return DLMSClassId.DEMAND_REGISTER;
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