/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.impl.CalendarApplication;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

public class CalendarApplicationTest extends FelixRestApplicationJerseyTest {
    @Mock
    CalendarService calendarService;


    @Override
    protected Application getApplication() {
        CalendarApplication application = new CalendarApplication();
        application.setCalendarService(calendarService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        return application;
    }
}
