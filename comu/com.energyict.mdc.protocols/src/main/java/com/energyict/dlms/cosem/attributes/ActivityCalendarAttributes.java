/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum ActivityCalendarAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    CALENDAR_NAME_ACTIVE(2, 0x08),
    SEASON_PROFILE_ACTIVE(3, 0x10),
    WEEK_PROFILE_TABLE_ACTIVE(4, 0x18),
    DAY_PROFILE_TABLE_ACTIVE(5, 0x20),
    CALENDAR_NAME_PASSIVE(6, 0x28),
    SEASON_PROFILE_PASSIVE(7, 0x30),
    WEEK_PROFILE_TABLE_PASSIVE(8, 0x38),
    DAY_PROFILE_TABLE_PASSIVE(9, 0x40),
    ACTIVATE_PASSIVE_CALENDAR_TIME(10, 0x48);

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortName;

    /**
     * Private constructor
     *
     * @param attributeNumber the chronological number of the attribute
     * @param shortName       the shortname of the attribute
     */
    private ActivityCalendarAttributes(int attributeNumber, int shortName) {
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

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

}
