package com.elster.jupiter.security.thread.impl;

import java.security.Principal;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.google.inject.AbstractModule;

public class ThreadSecurityModule extends AbstractModule {

    private final ThreadPrincipalService service;

    public ThreadSecurityModule() {
    	this(new Principal() {	
			@Override
			public String getName() {
				return "Test";
			}
		});
    }
    
    public ThreadSecurityModule(Principal principal) {
        this.service = new ThreadPrincipalServiceImpl();
        service.set(principal);
    }

    @Override
    protected void configure() {
        bind(ThreadPrincipalService.class).toInstance(service);
    }
}
