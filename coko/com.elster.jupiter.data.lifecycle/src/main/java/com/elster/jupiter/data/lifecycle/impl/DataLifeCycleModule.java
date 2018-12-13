/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.google.inject.AbstractModule;

public class DataLifeCycleModule extends AbstractModule {
	
    public DataLifeCycleModule() {
    }

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(MessageService.class);
        requireBinding(MeteringService.class);
        requireBinding(NlsService.class);
        requireBinding(TaskService.class);

        bind(LifeCycleService.class).to(LifeCycleServiceImpl.class);
    }
}
