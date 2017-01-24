package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * TODO - the SN method numbering is purely based on logical sequence numbering. This object is not defined in the BlueBook yet, so additional methods may be added in the future
 *
 * Copyrights EnergyICT
 * Date: 16-aug-2011
 * Time: 13:26:23
 */
public enum ChangeOfTenancyOrSupplierManagementMethods implements DLMSClassMethods {

    RESET(1, 0x70),
    ACTIVATE(2, 0x78);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    ChangeOfTenancyOrSupplierManagementMethods(final int methodNumber, final int shortName) {
        this.methodNumber = methodNumber;
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
     * Getter for the ClassId for this object
     *
     * @return the DLMS ClassID
     */
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.CHANGE_OF_TENANT_SUPPLIER_MANAGEMENT;
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
