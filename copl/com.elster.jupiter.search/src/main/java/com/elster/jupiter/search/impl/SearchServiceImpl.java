package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchProvider;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.conditions.Condition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provides an implementation for the {@link SearchService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-27 (09:38)
 */
@Component(name = "com.elster.jupiter.search", service = {SearchService.class}, property = "name=" + SearchService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class SearchServiceImpl implements SearchService {

    private volatile List<SearchProvider> searchProviders = new CopyOnWriteArrayList<>();

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void register(SearchProvider searchProvider) {
        this.searchProviders.add(searchProvider);
    }

    @Override
    public void unregister(SearchProvider searchProvider) {
        this.searchProviders.remove(searchProvider);
    }

    @Override
    public List<SearchProvider> getProviders() {
        return Collections.unmodifiableList(this.searchProviders);
    }

    @Override
    public SearchBuilder<Object> search(SearchDomain searchDomain) {
        return new SearchBuilderImpl<>(searchDomain);
    }

    @Override
    public Finder<Object> search(SearchDomain searchDomain, Condition condition) {
        throw new UnsupportedOperationException("use search(SearchDomain) instead");
    }

}