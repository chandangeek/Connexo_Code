/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.time.ExecutionTimerService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SearchModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(ExecutionTimerService.class);

        bind(SearchMonitor.class).to(SearchMonitorImpl.class).in(Scopes.SINGLETON);
        bind(SearchService.class).to(SearchServiceImpl.class).in(Scopes.SINGLETON);
    }
}