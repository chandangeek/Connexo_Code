package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Methods used in the LifeCycleManagement class id
 */
public enum LifeCycleManagementMethods implements DLMSClassMethods {

    /**
     * Method id 1, used to reboot the complete device
     */
    REBOOT_DEVICE(1, 0x18),

    /**
     * Method id 2, used to restart the application
     */
    RESTART_APPLICATION(2, 0x20),

    /**
     * Method id 3, used to hard reset the device
     */
    HARD_RESET(3, 0x28);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;

    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    LifeCycleManagementMethods(final int methodNumber, final int shortName) {
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

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.LIFE_CYCLE_MANAGEMENT;
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
