package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TimeOfUseInfo {

    public List<PassiveCalendarInfo> passiveCalendars;
    public NextCalendarInfo nextPassiveCalendar;
    public CalendarInfo activeCalendar;
    public long lastVerified;
    public boolean activeIsGhost = false;
    public List<String> supportedOptions;
}
