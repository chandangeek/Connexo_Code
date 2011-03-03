package com.energyict.dlms.cosem.attributes;

/**
 * Straightforward summary of the {@link com.energyict.dlms.cosem.Register} attributes
 */
public enum RegisterAttributes {

    Logical_Name(1, 0x00),
    Register_Value(2, 0x08),
    Register_Unit(3, 0x10);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param shortName       the shortName of the attribute
     */
    private RegisterAttributes(int attributeNumber, int shortName) {
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

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }
}
