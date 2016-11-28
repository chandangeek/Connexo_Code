package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class UsagePointDataModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(NlsService.class);
        requireBinding(MeteringService.class);
        requireBinding(CustomPropertySetService.class);
        requireBinding(UsagePointConfigurationService.class);

        bind(UsagePointDataService.class).to(UsagePointDataServiceImpl.class).in(Scopes.SINGLETON);
    }
}
