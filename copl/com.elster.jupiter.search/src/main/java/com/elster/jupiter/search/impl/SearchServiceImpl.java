package com.elster.jupiter.search.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.concurrent.CopyOnWriteServiceContainer;
import com.elster.jupiter.util.concurrent.OptionalServiceContainer;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    private volatile OptionalServiceContainer<SearchDomain> searchProviders = new CopyOnWriteServiceContainer<>();

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void register(SearchDomain searchDomain) {
        this.searchProviders.register(searchDomain);
    }

    @Override
    public void unregister(SearchDomain searchDomain) {
        this.searchProviders.unregister(searchDomain);
    }

    @Override
    public List<SearchDomain> getDomains() {
        return this.searchProviders.getServices();
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
        return searchProviders.get(isEqual(id), timeout);
    }

    private Predicate<SearchDomain> isEqual(String id) {
        return searchDomain -> searchDomain.getId().equals(id);
    }

    @Override
    public SearchBuilder<Object> search(SearchDomain searchDomain) {
        validateRegisteredDomain(searchDomain);
        return new SearchBuilderImpl<>(searchDomain);
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