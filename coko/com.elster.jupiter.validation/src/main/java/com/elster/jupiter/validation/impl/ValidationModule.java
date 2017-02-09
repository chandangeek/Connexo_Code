/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiServiceImpl;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class ValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(EventService.class);
        requireBinding(MessageService.class);
        requireBinding(OrmService.class);
        requireBinding(MeteringGroupsService.class);

        bind(ServerValidationService.class).to(ValidationServiceImpl.class).in(Scopes.SINGLETON);
        bind(ValidationService.class).to(ServerValidationService.class);
        bind(DataValidationKpiService.class).to(DataValidationKpiServiceImpl.class).in(Scopes.SINGLETON);
    }

}