/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EstimationModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(MeteringService.class);
        requireBinding(TimeService.class);
        requireBinding(QueryService.class);
        requireBinding(NlsService.class);
        requireBinding(EventService.class);
        requireBinding(TaskService.class);
        requireBinding(MeteringGroupsService.class);
        requireBinding(MessageService.class);

        bind(EstimationService.class).to(EstimationServiceImpl.class).in(Scopes.SINGLETON);
    }
}
