package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;

import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

abstract class SimpleQueryProvider<T extends HasId> implements QueryProvider<T> {

    private final Class<T> domainClass;
    private Supplier<Query<T>> basicQuerySupplier;
    private SearchService searchService;
    private Thesaurus thesaurus;

    SimpleQueryProvider(Class<T> domainClass) {
        this.domainClass = domainClass;
    }

    @Override
    public SimpleQueryProvider<T> init(Supplier<Query<T>> basicQuerySupplier) {
        this.basicQuerySupplier = basicQuerySupplier;
        return this;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringGroupsService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public List<T> executeQuery(Instant instant, List<SearchablePropertyCondition> conditions) {
        return executeQuery(instant, conditions, -1, 0);
    }

    @Override
    public List<T> executeQuery(Instant instant, List<SearchablePropertyCondition> conditions, int start, int limit) {
        SearchDomain searchDomain = searchService.findDomain(domainClass.getName())
                .orElseThrow(() -> new InvalidQueryGroupException(thesaurus, MessageSeeds.SEARCH_DOMAIN_NOT_FOUND));
        Subquery subQuery = searchDomain.finderFor(conditions).asSubQuery("id");
        Condition condition = ListOperator.IN.contains(subQuery, "id");
        return start > -1 ?
                basicQuerySupplier.get().select(condition, start + 1, start + limit + 1) :
                basicQuerySupplier.get().select(condition);
    }

    @Override
    public Query<T> getQuery(List<SearchablePropertyCondition> conditions) {
        SearchDomain searchDomain = searchService.findDomain(domainClass.getName())
                .orElseThrow(() -> new InvalidQueryGroupException(thesaurus, MessageSeeds.SEARCH_DOMAIN_NOT_FOUND));
        Subquery subQuery = searchDomain.finderFor(conditions).asSubQuery("id");
        Query<T> basicQuery = basicQuerySupplier.get();
        basicQuery.setRestriction(ListOperator.IN.contains(subQuery, "id"));
        return basicQuery;
    }
}
