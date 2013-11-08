package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Straightforward summary of the {@link com.energyict.dlms.cosem.Register} attributes
 */
public enum ClockAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    TIME(2, 0x08),
    TIMEZONE(3, 0x10),
    STATUS(4, 0x18),
    TIME_DS_BEGIN(5, 0x20),
	TIME_DS_END(6, 0x28),
    DS_DEVIATION(7, 0x30),
    DS_ENABLED(8, 0x38),
    CLOCK_BASE(9, 0x40);

    private final int attributeNumber;
    private final int shortName;



    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param shortName       the shortName of the attribute
     */
    private ClockAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.CLOCK;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
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
