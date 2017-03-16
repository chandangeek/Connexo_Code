/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.usagepoint.calendar.calendarResolver", service = CalendarResolver.class)
@SuppressWarnings("unused")
public class UsagePointCalendarResolver implements CalendarResolver {

    private volatile ServerMeteringService meteringService;

    // For OSGi purposes
    public UsagePointCalendarResolver() {
    }

    // For testing purposes
    @Inject
    public UsagePointCalendarResolver(ServerMeteringService meteringService) {
        this();
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public boolean isCalendarInUse(Calendar calendar) {
        return meteringService.isCalendarEffectiveForAnyUsagePoint(calendar);
    }
}

