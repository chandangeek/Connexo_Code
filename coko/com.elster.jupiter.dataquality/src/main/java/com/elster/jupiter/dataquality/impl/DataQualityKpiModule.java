/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class DataQualityKpiModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(UpgradeService.class);
        requireBinding(MessageService.class);
        requireBinding(UserService.class);
        requireBinding(TaskService.class);
        requireBinding(MeteringService.class);
        requireBinding(KpiService.class);
        requireBinding(TransactionService.class);
        requireBinding(MeteringGroupsService.class);
        requireBinding(ValidationService.class);
        requireBinding(EstimationService.class);
        requireBinding(AppService.class);

        bind(DataQualityKpiService.class).to(DataQualityKpiServiceImpl.class).in(Scopes.SINGLETON);
    }
}