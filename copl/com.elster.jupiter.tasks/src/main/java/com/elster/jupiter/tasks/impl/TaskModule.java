/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import java.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests that use the {@link TaskService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-08 (11:29)
 */
public class TaskModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(MessageService.class);      
        requireBinding(QueryService.class);
        requireBinding(TransactionService.class);
        requireBinding(CronExpressionParser.class);
        requireBinding(JsonService.class);

        bind(TaskService.class).to(TaskServiceImpl.class).in(Scopes.SINGLETON);
    }
}