package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SearchModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SearchService.class).to(SearchServiceImpl.class).in(Scopes.SINGLETON);
    }
}