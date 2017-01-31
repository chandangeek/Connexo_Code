/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.nls.rest", service = Application.class, immediate = true, property = {"alias=/nls", "app=SYS", "name=" + NlsApplication.COMPONENT_NAME})
@SuppressWarnings("unused")
public class NlsApplication extends Application implements BinderProvider {

    static final String COMPONENT_NAME = "NLS";

    private final Set<Class<?>> classes = ImmutableSet.of(ThesaurusResource.class);
    private volatile NlsService nlsService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(nlsService).to(NlsService.class);
                bind(transactionService).to(TransactionService.class);
                bind(ThesaurusCache.class).to(ThesaurusCache.class).in(Singleton.class);
            }
        };
    }

}