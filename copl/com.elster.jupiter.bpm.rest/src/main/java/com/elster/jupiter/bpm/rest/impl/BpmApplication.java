package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.rest.TranslationKeys;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.*;
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
import java.util.*;

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

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(BpmResource.class);
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
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
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
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
        }
    }

    @Override
    public String getComponentName() {
        return BpmApplication.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys =  new ArrayList<>();
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}

