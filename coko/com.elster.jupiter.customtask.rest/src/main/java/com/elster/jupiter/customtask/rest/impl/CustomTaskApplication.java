/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.customtask.rest",
        service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/ctk", "app=SYS", "name=" + CustomTaskApplication.COMPONENT_NAME})
public class CustomTaskApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {
    public static final String COMPONENT_NAME = "CTR";

    private volatile CustomTaskService customTaskService;
    private volatile TransactionService transactionService;
    private volatile TaskService taskService;
    private volatile RestQueryService restQueryService;
    private volatile Clock clock;

    private NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile TimeService timeService;
    private volatile PropertyValueInfoService propertyValueInfoService;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                RestValidationExceptionMapper.class,
                CustomTaskResource.class);
    }

    @Reference
    public void setCustomTaskService(CustomTaskService customTaskService) {
        this.customTaskService = customTaskService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(CustomTaskService.COMPONENTNAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus);
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
                bind(timeService).to(TimeService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(propertyValueInfoService).to(PropertyValueInfoService.class);
                bind(nlsService).to(NlsService.class);
                bind(customTaskService).to(CustomTaskService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(taskService).to(TaskService.class);
                bind(transactionService).to(TransactionService.class);
                bind(CustomTaskInfoFactory.class).to(CustomTaskInfoFactory.class);
                bind(CustomTaskHistoryInfoFactory.class).to(CustomTaskHistoryInfoFactory.class);
                bind(clock).to(Clock.class);
            }
        });
        return Collections.unmodifiableSet(hashSet);
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
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

}