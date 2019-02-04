/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.demo.impl.builders.CalendarBuilder;

public enum CalendarTpl implements Template<Calendar, CalendarBuilder> {
    RE_CU_01("Re-Cu-01"),
    RE_CU_02("Re-Cu-02"),;

    private final String mrid;

    CalendarTpl(String mrid) {
        this.mrid = mrid;
    }

    @Override
    public Class<CalendarBuilder> getBuilderClass() {
        return CalendarBuilder.class;
    }

    @Override
    public CalendarBuilder get(CalendarBuilder builder) {
        return builder.withMrid(this.mrid).withContentStream(this.getClass().getClassLoader().getResourceAsStream(this.mrid.toLowerCase() + ".xml"));
    }
}
