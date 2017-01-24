package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Methods used in the NetworkManagement class id
 */
public enum NetworkManagementMethods implements DLMSClassMethods {

    /**
     * Method id 1, used to reboot the complete device
     */
    RUN_METER_DISCOVERY(1, 0x30),

    /**
     * Method id 2, used to restart the application
     */
    RUN_ALARM_METER_DISCOVERY(2, 0x38),

    /**
     * Method id 3, used to hard reset the device
     */
    RUN_REPEATER_CALL(3, 0x40);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;

    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    NetworkManagementMethods(final int methodNumber, final int shortName) {
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
        return DLMSClassId.NETWORK_MANAGEMENT;
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
