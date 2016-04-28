package com.energyict.mdc.device.config;

import com.elster.jupiter.calendar.Calendar;

import java.util.Optional;

public interface AllowedCalendar {

    /**
     * Checks if the calendar is available in the system
     * @return the ghost status of the calendar
     */
    boolean isGhost();

    /**
     * Gets the name of the calendar
     * @return the name of the calendar
     */
    String getName();

    Optional<Calendar> getCalendar();

}
