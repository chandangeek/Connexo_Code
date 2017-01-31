/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.validation.MessageInterpolator;

/**
 * Simple module which defines the required bindings
 */
public class FileImportModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(NlsService.class);
        requireBinding(QueryService.class);
        requireBinding(UserService.class);
        requireBinding(MessageService.class);
        requireBinding(JsonService.class);
        bind(FileImportService.class).to(FileImportServiceImpl.class).in(Scopes.SINGLETON);
    }
}
