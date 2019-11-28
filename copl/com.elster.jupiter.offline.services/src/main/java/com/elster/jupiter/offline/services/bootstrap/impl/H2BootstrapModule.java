/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.bootstrap.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.validation.impl.ProviderResolverService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.validation.ValidationProviderResolver;

public class H2BootstrapModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BootstrapService.class).to(H2BootstrapService.class).in(Scopes.SINGLETON);
        bind(ValidationProviderResolver.class).to(ProviderResolverService.class).in(Scopes.SINGLETON);
    }
    
    public void deactivate() {
    	// for compatibility with InMemoryBootstrapModule
    }
}
