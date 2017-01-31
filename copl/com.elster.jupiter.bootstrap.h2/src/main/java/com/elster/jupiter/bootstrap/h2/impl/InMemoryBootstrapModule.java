/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.h2.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.validation.ValidationProviderResolver;

public class InMemoryBootstrapModule extends AbstractModule {
	
	H2BootstrapService bootstrapService = new H2BootstrapService();

    @Override
    protected void configure() {
        bind(BootstrapService.class).toInstance(bootstrapService);
        bind(ValidationProviderResolver.class).to(ProviderResolverService.class).in(Scopes.SINGLETON);
    }
    
    public void deactivate() {
    	bootstrapService.deactivate();
    }
}
