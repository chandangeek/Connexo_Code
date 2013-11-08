package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Contains functionality to access methods of the ActivityCalendar (independent whether or not shortName or logicalNames are used)
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 17-dec-2010<br/>
 * Time: 10:27:39<br/>
 */
public enum ActivityCalendarMethods implements DLMSClassMethods {

    ACTIVATE_PASSIVE_CALENDAR(1, 0x50);

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
     * @param methodNumber the method number
     * @param shortName the shortname of the method
     */
    private ActivityCalendarMethods(int methodNumber, int shortName) {
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
        return DLMSClassId.ACTIVITY_CALENDAR;
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
