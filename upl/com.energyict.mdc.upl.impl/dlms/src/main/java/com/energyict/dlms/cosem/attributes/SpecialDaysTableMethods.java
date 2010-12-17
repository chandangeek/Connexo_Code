package com.energyict.dlms.cosem.attributes;

/**
 * Copyrights EnergyICT
 * Date: 17-dec-2010
 * Time: 9:48:44
 */
public enum SpecialDaysTableMethods implements DLMSClassMethods {

    INSERT(1, 0x10),
    DELETE(2, 0x18);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    /**
     * Private constructor
     *
     * @param methodNr  the number of the method
     * @param shortName the shortName of the method
     */
    private SpecialDaysTableMethods(int methodNr, int shortName) {
        this.methodNumber = methodNr;
        this.shortName = shortName;
    }

    /**
     * Getter for the method number
     *
     * @return the method number as int
     */
    public int getMethodNumber() {
        return this.methodNumber;
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
