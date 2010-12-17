package com.energyict.dlms.cosem.attributes;

/**
 * Copyrights EnergyICT
 * Date: 17-dec-2010
 * Time: 9:53:02
 */
public enum SpecialDaysTableAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    ENTRIES(2, 0x08);

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortName;

    private SpecialDaysTableAttributes(int attributeNumber, int shortName) {
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
