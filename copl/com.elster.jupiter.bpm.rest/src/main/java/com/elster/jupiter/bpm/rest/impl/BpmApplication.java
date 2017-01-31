/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.rest.TranslationKeys;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.bpm.rest",
        service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/bpm", "app=BPM", "name=" + BpmApplication.COMPONENT_NAME})
public class BpmApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {

    public static final String APP_KEY = "BPM";
    public static final String COMPONENT_NAME = "BPM";

    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile BpmService bpmService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile PropertyValueInfoService propertyValueInfoService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(BpmResource.class, BpmWorkGroupResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST).join(nlsService.getThesaurus("BPM", Layer.DOMAIN));
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Override
    public String getComponentName() {
        return BpmApplication.COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        return keys;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(bpmService).to(BpmService.class);
            bind(userService).to(UserService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(transactionService).to(TransactionService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
        }
    }
}

