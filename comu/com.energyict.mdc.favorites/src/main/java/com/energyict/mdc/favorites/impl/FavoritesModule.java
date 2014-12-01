package com.energyict.mdc.favorites.impl;

import java.time.Clock;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.favorites.FavoritesService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class FavoritesModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class); 
        requireBinding(NlsService.class);
        requireBinding(OrmService.class);
        
        bind(FavoritesService.class).to(FavoritesServiceImpl.class).in(Scopes.SINGLETON);
    }

}
