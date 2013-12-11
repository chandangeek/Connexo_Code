package com.elster.jupiter.security.thread.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

public class ThreadSecurityModule extends AbstractModule {

    private final ThreadPrincipalService service;

    public ThreadSecurityModule(Principal principal) {
        this.service = new ThreadPrincipalServiceImpl();
        service.set(principal);
    }

    @Override
    protected void configure() {
        bind(ThreadPrincipalService.class).toInstance(service);
    }
}
