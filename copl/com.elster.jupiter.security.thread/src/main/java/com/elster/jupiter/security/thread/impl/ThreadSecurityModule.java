package com.elster.jupiter.security.thread.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.security.Principal;

public class ThreadSecurityModule extends AbstractModule {

    private final Principal principal;

    public ThreadSecurityModule(Principal principal) {
        this.principal = principal;
    }

    @Override
    protected void configure() {
        bind(ThreadPrincipalService.class).to(ThreadPrincipalServiceImpl.class).in(Scopes.SINGLETON);
        bindListener(new ServiceMatcher(ThreadPrincipalService.class), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register(new InjectionListener<I>() {
                    @Override
                    public void afterInjection(I injectee) {
                        ((ThreadPrincipalService) injectee).set(principal);
                    }
                });
            }
        });
    }
}
