package com.elster.jupiter.bootstrap.h2.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class InMemoryBootstrapModule extends AbstractModule {
	
	H2BootstrapService bootstrapService = new H2BootstrapService();

    @Override
    protected void configure() {
        bind(BootstrapService.class).toInstance(bootstrapService);
    }
    
    public void deactivate() {
    	bootstrapService.deactivate();
    }
}
