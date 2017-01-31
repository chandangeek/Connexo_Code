/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.concurrent.CopyOnWriteServiceContainer;
import com.elster.jupiter.util.concurrent.OptionalServiceContainer;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link SearchService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-27 (09:38)
 */
@Component(name = "com.elster.jupiter.search", service = {SearchService.class, MessageSeedProvider.class}, property = "name=" + SearchService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class SearchServiceImpl implements SearchService, MessageSeedProvider {

    private final OptionalServiceContainer<SearchDomain> searchProviders = new CopyOnWriteServiceContainer<>();
    private final List<SearchDomainExtension> searchExtensions = new CopyOnWriteArrayList<>();
    private volatile SearchMonitor searchMonitor;
    private volatile OrmService ormService;

    // For OSGi purposes
    public SearchServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public SearchServiceImpl(SearchMonitor searchMonitor) {
        this();
        this.setSearchMonitor(searchMonitor);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference(name = "AAA")
    public void setSearchMonitor(SearchMonitor searchMonitor) {
        this.searchMonitor = searchMonitor;
    }

    @Override
    @Reference(name = "ZZZ", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void register(SearchDomain searchDomain) {
        this.searchProviders.register(searchDomain);
        this.searchMonitor.searchDomainRegistered(searchDomain);
    }

    @Override
    @Reference(name = "MD-SearchDomainExtension", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void register(SearchDomainExtension searchDomainExtension) {
        this.searchExtensions.add(searchDomainExtension);
    }

    @Override
    public void unregister(SearchDomain searchDomain) {
        this.searchProviders.unregister(searchDomain);
        this.searchMonitor.searchDomainUnregistered(searchDomain);
    }

    List<SearchDomainExtension> getSearchExtensions() {
        return this.searchExtensions; // for internal use only, so can skip creating an unmodifiable instance
    }

    @Override
    public void unregister(SearchDomainExtension searchDomainExtension) {
        this.searchExtensions.remove(searchDomainExtension);
    }

    @Override
    public List<SearchDomain> getDomains() {
        return this.searchProviders.getServices()
                .stream()
                .map(domain -> new ExtendedSearchDomain(ormService, this, domain))
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchDomain> getDomains(String application) {
        return this.getDomains()
                .stream()
                .filter(targetApplicationMatchPredicate(application))
                .collect(Collectors.toList());
    }

    protected Predicate<SearchDomain> targetApplicationMatchPredicate(String application) {
        return searchDomain -> searchDomain.targetApplications().isEmpty() || searchDomain.targetApplications().contains(application);
    }

    @Override
    public Optional<SearchDomain> findDomain(String id) {
        return getDomains().stream().filter(isEqual(id)).findAny();
    }

    @Override
    public Optional<SearchDomain> pollDomain(String id, Duration timeout) throws InterruptedException {
        return searchProviders.get(isEqual(id), timeout).map(searchDomain -> new ExtendedSearchDomain(ormService, this, searchDomain));
    }

    private Predicate<SearchDomain> isEqual(String id) {
        return searchDomain -> searchDomain.getId().equals(id);
    }

    @Override
    public SearchBuilder<Object> search(SearchDomain searchDomain) {
        validateRegisteredDomain(searchDomain);
        return new SearchBuilderImpl<>(searchDomain, this.searchMonitor);
    }

    private void validateRegisteredDomain(SearchDomain searchDomain) {
        this.findDomain(searchDomain.getId()).orElseThrow(() -> new IllegalArgumentException("Not a registered domain " + searchDomain.getId()));
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}