package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

/**
 * Created by igh on 21/04/2016.
 */
public class CalendarModule  extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);

        bind(CalendarService.class).to(CalendarServiceImpl.class).in(Scopes.SINGLETON);
        bind(ServerCalendarService.class).to(CalendarServiceImpl.class).in(Scopes.SINGLETON);
    }
}
