/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.calendar.rest.CalendarInfo;

import java.util.List;

public class TimeOfUseInfo {

    public List<PassiveCalendarInfo> passiveCalendars;
    public NextCalendarInfo nextPassiveCalendar;
    public CalendarInfo activeCalendar;
    public long lastVerified;
    public boolean activeIsGhost = false;
}
