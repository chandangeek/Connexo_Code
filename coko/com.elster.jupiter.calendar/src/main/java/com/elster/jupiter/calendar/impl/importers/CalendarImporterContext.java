/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.elster.jupiter.calendar.importers.impl.CalendarImporterContext", service = {CalendarImporterContext.class})
public class CalendarImporterContext {
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile CalendarService calendarService;
    private volatile Clock clock;

    public CalendarImporterContext() {
    }

    @Inject
    public CalendarImporterContext(NlsService nlsService,
                                     UserService userService,
                                     ThreadPrincipalService threadPrincipalService,
                                     CalendarService calendarService,
                                     Clock clock) {
        this();
        setNlsService(nlsService);
        setUserService(userService);
        setThreadPrincipalService(threadPrincipalService);
        setCalendarService(calendarService);
        setClock(clock);
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public CalendarService getCalendarService() {
        return calendarService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CalendarService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public UserService getUserService() {
        return userService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

}