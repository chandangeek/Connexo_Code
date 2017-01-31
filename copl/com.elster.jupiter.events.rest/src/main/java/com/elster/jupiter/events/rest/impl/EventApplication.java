/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.event.rest", service = Application.class, immediate = true, property = {"alias=/evt", "app=SYS", "name=" + EventService.COMPONENTNAME})
public class EventApplication extends Application {
    public static final String COMPONENT_NAME = "EVT";

    private volatile EventService eventService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile Thesaurus thesaurus;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(EventTypeResource.class);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.addAll(super.getSingletons());
        singletons.add(new HK2Binder());
        return singletons;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(restQueryService).to(RestQueryService.class);
            bind(transactionService).to(TransactionService.class);
            bind(eventService).to(EventService.class);
            bind(thesaurus).to(Thesaurus.class);
        }
    }
}
