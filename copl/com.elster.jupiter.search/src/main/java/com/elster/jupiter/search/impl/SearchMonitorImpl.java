/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.util.time.ExecutionTimer;
import com.elster.jupiter.util.time.ExecutionTimerService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an implementation for the {@link SearchMonitor} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-08 (16:27)
 */
@Component(name = "com.elster.jupiter.search.monitor", service = {SearchMonitor.class})
public class SearchMonitorImpl implements SearchMonitor {

    private volatile ExecutionTimerService executionTimerService;
    private ExecutionTimer globalSearch;
    private Map<String, ExecutionTimer> searchPerDomain = new ConcurrentHashMap<>();
    private ExecutionTimer globalCount;
    private Map<String, ExecutionTimer> countPerDomain = new ConcurrentHashMap<>();

    // For OSGi purposes
    public SearchMonitorImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public SearchMonitorImpl(ExecutionTimerService executionTimerService) {
        this();
        this.setExecutionTimerService(executionTimerService);
        this.activate();
    }

    @Activate
    public void activate() {
        this.globalSearch = this.executionTimerService.newTimer("SearchMonitor", this.defaultTimeout());
        this.globalCount = this.executionTimerService.newTimer("SearchCountMonitor", this.defaultTimeout());
    }

    @Deactivate
    public void deactivate() {
        this.globalSearch.deactivate();
        this.globalCount.deactivate();
        this.searchPerDomain.values().stream().forEach(ExecutionTimer::deactivate);
        this.countPerDomain.values().stream().forEach(ExecutionTimer::deactivate);
    }

    @Reference
    public void setExecutionTimerService(ExecutionTimerService executionTimerService) {
        this.executionTimerService = executionTimerService;
    }

    @Override
    public void searchDomainRegistered(SearchDomain searchDomain) {
        ExecutionTimer search = this.executionTimerService.newTimer("SearchMonitor " + searchDomain.getId(), this.defaultTimeout());
        this.globalSearch.join(search);
        this.searchPerDomain.put(
                searchDomain.getId(),
                search);
        ExecutionTimer count = this.executionTimerService.newTimer("SearchCountMonitor " + searchDomain.getId(), this.defaultTimeout());
        this.globalCount.join(count);
        this.countPerDomain.put(
                searchDomain.getId(),
                count);
    }

    private Duration defaultTimeout() {
        return Duration.ofMinutes(2);
    }

    @Override
    public void searchDomainUnregistered(SearchDomain searchDomain) {
        this.searchPerDomain.remove(searchDomain.getId()).deactivate();
        this.countPerDomain.remove(searchDomain.getId()).deactivate();
    }

    @Override
    public ExecutionTimer searchTimer(SearchDomain searchDomain) {
        return this.searchPerDomain.get(searchDomain.getId());
    }

    @Override
    public ExecutionTimer countTimer(SearchDomain searchDomain) {
        return this.countPerDomain.get(searchDomain.getId());
    }

}