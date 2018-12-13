package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Copyrights EnergyICT
 * Date: 16-aug-2011
 * Time: 13:26:23
 */
public enum ScheduleManagerMethods implements DLMSClassMethods {

    ADD_SCHEDULE(1, 0x18),
    REMOVE_SCHEDULE(2, 0x20),
    UPDATE_SCHEDULE(3, 0x28);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    ScheduleManagerMethods(final int methodNumber, final int shortName) {
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
        return DLMSClassId.SCHEDULE_MANAGER;
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