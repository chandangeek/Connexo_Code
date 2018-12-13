/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.elster.jupiter.calendar.rest",
        service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true,
        property = {"alias=/cal", "app=" + CalendarApplication.APP_KEY, "name=" + CalendarApplication.COMPONENT_NAME})
public class CalendarApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {
    public static final String APP_KEY = "SYS";
    public static final String COMPONENT_NAME = "CAL";

    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile CalendarService calendarService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                CalendarResource.class,
                CategoryResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(CalendarInfoFactoryImpl.class).to(CalendarInfoFactory.class);
            bind(transactionService).to(TransactionService.class);
            bind(calendarService).to(CalendarService.class);
            bind(thesaurus).to(Thesaurus.class);
        }
    }
}
