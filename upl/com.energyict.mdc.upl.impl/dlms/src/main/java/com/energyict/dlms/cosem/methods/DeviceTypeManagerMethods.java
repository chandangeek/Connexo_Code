package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Copyrights EnergyICT
 * Date: 16-aug-2011
 * Time: 13:26:23
 */
public enum DeviceTypeManagerMethods implements DLMSClassMethods {

    ADD_DEVICE_TYPE(1, 0x10),
    REMOVE_DEVICE_TYPE(2, 0x18),
    UPDATE_DEVICE_TYPE(3, 0x20),
    ASSIGN_DEVICE_TYPE(4, 0x28);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    DeviceTypeManagerMethods(final int methodNumber, final int shortName) {
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
        return DLMSClassId.DEVICE_TYPE_MANAGER;
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