package com.energyict.protocolimplv2.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * The ComposedActivityCalendar is just a ValueObject that holds the {@link DLMSAttribute} from a Clock object
 * @since 31/08/2015 - 16:21
 */
public class ComposedActivityCalendar implements ComposedObject {

    private final DLMSAttribute calendarNameActiveAttribute;


    public ComposedActivityCalendar(DLMSAttribute calendarNameActiveAttribute) {
        this.calendarNameActiveAttribute = calendarNameActiveAttribute;
    }

    public DLMSAttribute getCalendarNameActiveAttribute() {
        return calendarNameActiveAttribute;
    }
}