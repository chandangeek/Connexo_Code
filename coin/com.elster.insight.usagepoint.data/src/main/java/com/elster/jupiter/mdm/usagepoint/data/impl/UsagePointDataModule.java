package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoritesService;
import com.elster.jupiter.mdm.usagepoint.data.impl.favorites.FavoritesServiceImpl;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class UsagePointDataModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(NlsService.class);
        requireBinding(OrmService.class);
        requireBinding(MeteringService.class);
        requireBinding(CustomPropertySetService.class);
        requireBinding(UsagePointConfigurationService.class);

        bind(UsagePointDataModelService.class).to(UsagePointDataModelServiceImpl.class).in(Scopes.SINGLETON);
        bind(UsagePointDataCompletionService.class).to(UsagePointDataCompletionServiceImpl.class).in(Scopes.SINGLETON);
        bind(FavoritesService.class).to(FavoritesServiceImpl.class).in(Scopes.SINGLETON);
    }
}
