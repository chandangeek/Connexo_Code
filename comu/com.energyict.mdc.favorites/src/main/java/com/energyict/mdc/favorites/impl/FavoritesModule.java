/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.favorites.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.favorites.FavoritesService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class FavoritesModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class); 
        requireBinding(NlsService.class);
        requireBinding(OrmService.class);
        
        bind(FavoritesService.class).to(FavoritesServiceImpl.class).in(Scopes.SINGLETON);
    }

}
