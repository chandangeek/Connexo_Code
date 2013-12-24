package com.elster.jupiter.bootstrap.h2.impl;

import javax.validation.ValidationProviderResolver;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.validation.impl.ProviderResolverService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

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
