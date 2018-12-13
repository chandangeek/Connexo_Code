/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.nls.NlsService;

import com.google.inject.AbstractModule;

public class CalendarRestModule extends AbstractModule{

    @Override
    protected void configure() {
        requireBinding(NlsService.class);

        bind(CalendarInfoFactory.class).to(CalendarInfoFactoryImpl.class);
    }
}
