package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Straightforward summary of the {@link com.energyict.dlms.cosem.ExtendedRegister} attributes
 */
public enum ExtendedRegisterAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    VALUE(2, 0x08),
    UNIT(3, 0x10),
    STATUS(4, 0x18),
    CAPTURE_TIME(5, 0x20);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param shortName       the shortName of the attribute
     */
    private ExtendedRegisterAttributes(int attributeNumber, int shortName) {
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
        return DLMSClassId.EXTENDED_REGISTER;
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
