package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface CalendarResolver {

    boolean isCalendarInUse(Calendar calendar);

}
