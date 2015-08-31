package com.elster.jupiter.time.rest.impl;


import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.impl.i18n.MessageSeeds;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
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

@Component(
        name = "com.jupiter.time.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/tmr", "app=SYS", "name=" + TimeApplication.COMPONENT_NAME})
public class TimeApplication extends Application implements TranslationKeyProvider {
    public static final String COMPONENT_NAME = "TMR";

    private volatile TimeService timeService;
    private volatile RestQueryService restQueryService;
    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                RelativePeriodResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
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
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
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
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }


    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(timeService).to(TimeService.class);
            bind(transactionService).to(TransactionService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(restQueryService).to(RestQueryService.class);
        }
    }
}
