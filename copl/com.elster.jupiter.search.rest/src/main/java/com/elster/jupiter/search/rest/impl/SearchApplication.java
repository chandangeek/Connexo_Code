/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.location.SearchLocationService;
import com.elster.jupiter.search.rest.InfoFactoryService;
import com.elster.jupiter.search.rest.MessageSeeds;
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

/**
 * Created by bvn on 6/1/15.
 */
@Component(name = "com.elster.jupiter.search.rest",
        service = {Application.class, MessageSeedProvider.class},
        immediate = true,
        property = {"alias=/jsr", "app=MDC", "name=" + SearchApplication.COMPONENT_NAME})
public class SearchApplication extends Application implements MessageSeedProvider {

    public static final String COMPONENT_NAME = "JSR";

    private volatile SearchService searchService;
    private volatile SearchLocationService searchLocationService;
    private volatile Thesaurus thesaurus;
    private volatile InfoFactoryService infoFactoryService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                DynamicSearchResource.class
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
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setSearchLocationService(SearchLocationService searchLocationService) {
        this.searchLocationService = searchLocationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setInfoFactoryService(InfoFactoryService infoFactoryService) {
        this.infoFactoryService = infoFactoryService;
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
            bind(searchService).to(SearchService.class);
            bind(searchLocationService).to(SearchLocationService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(infoFactoryService).to(InfoFactoryService.class);
            bind(SearchCriterionInfoFactory.class).to(SearchCriterionInfoFactory.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
        }
    }

}