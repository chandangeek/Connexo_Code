/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.imports.impl.usagepoint.UsagePointsImporterFactory;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class MeteringImportsModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(PropertySpecService.class);
        requireBinding(NlsService.class);
        requireBinding(MeteringService.class);
        requireBinding(UserService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(CustomPropertySetService.class);
        requireBinding(LicenseService.class);
        requireBinding(Clock.class);
        requireBinding(MetrologyConfigurationService.class);
        requireBinding(CalendarService.class);
        requireBinding(UsagePointLifeCycleService.class);
        requireBinding(PropertyValueInfoService.class);
        requireBinding(TransactionService.class);

        bind(MeteringDataImporterContext.class).in(Scopes.SINGLETON);
        bind(UsagePointsImporterFactory.class).in(Scopes.SINGLETON);
    }
}
