/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.security.thread.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

import com.google.inject.AbstractModule;

import java.security.Principal;

public class ThreadSecurityModule extends AbstractModule {

    private final ThreadPrincipalService service;

    public ThreadSecurityModule() {
    	this(() -> "Test");
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