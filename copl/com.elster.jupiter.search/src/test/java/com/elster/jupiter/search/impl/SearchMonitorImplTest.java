/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.util.time.ExecutionTimer;
import com.elster.jupiter.util.time.ExecutionTimerService;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SearchMonitorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-10 (16:31)
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchMonitorImplTest {

    public static final String SEARCH_DOMAIN_ID = SearchMonitorImplTest.class.getSimpleName();

    @Mock
    private ExecutionTimerService executionTimerService;
    @Mock
    private ExecutionTimer timer;
    @Mock
    private SearchDomain searchDomain;

    @Before
    public void initializeMocks() {
        when(this.searchDomain.getId()).thenReturn(SEARCH_DOMAIN_ID);
    }

    @Test
    public void activationCreatesSearchAndCountTimers() {
        SearchMonitorImpl searchMonitor = new SearchMonitorImpl();
        searchMonitor.setExecutionTimerService(this.executionTimerService);

        // Business method
        searchMonitor.activate();

        // Asserts
        verify(this.executionTimerService, times(2)).newTimer(anyString(), any(Duration.class));
    }

    @Test
    public void searchDomainRegistrationCreatesSearchAndCountTimers() {
        SearchMonitorImpl searchMonitor = this.getTestInstance();
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);
        searchMonitor.activate();
        reset(this.executionTimerService);
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);

        // Business method
        searchMonitor.searchDomainRegistered(this.searchDomain);

        // Asserts
        verify(this.searchDomain, atLeastOnce()).getId();
        verify(this.executionTimerService, times(2)).newTimer(anyString(), any(Duration.class));
    }

    @Test
    public void searchDomainRegistrationJoinsGlobalTimer() {
        SearchMonitorImpl searchMonitor = this.getTestInstance();
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);
        searchMonitor.activate();
        reset(this.executionTimerService);
        ExecutionTimer searchDomainTimer = mock(ExecutionTimer.class);
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(searchDomainTimer);

        // Business method
        searchMonitor.searchDomainRegistered(this.searchDomain);

        // Asserts
        verify(this.timer, times(2)).join(searchDomainTimer);   // once for search and once for the count
    }

    @Test
    public void deactivationDeactivatesSearchAndCountTimers() {
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);
        SearchMonitorImpl searchMonitor = this.getTestInstance();

        // Business method
        searchMonitor.deactivate();

        // Asserts
        verify(this.timer, times(2)).deactivate();
    }

    @Test
    public void searchDomainUnregistrationDeactivatesSearchAndCountTimers() {
        SearchMonitorImpl searchMonitor = this.getTestInstance();
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);
        searchMonitor.activate();
        searchMonitor.searchDomainRegistered(this.searchDomain);
        reset(this.executionTimerService);

        // Business method
        searchMonitor.searchDomainUnregistered(this.searchDomain);

        // Asserts
        verify(this.searchDomain, atLeastOnce()).getId();
        verify(this.timer, times(2)).deactivate();
    }

    @Test
    public void getSearchTimerForRegisteredDomain() {
        SearchMonitorImpl searchMonitor = this.getTestInstance();
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);
        searchMonitor.activate();
        searchMonitor.searchDomainRegistered(this.searchDomain);

        // Business method
        ExecutionTimer timer = searchMonitor.searchTimer(this.searchDomain);

        // Asserts
        assertThat(timer).isEqualTo(this.timer);
    }

    @Test
    public void getCountTimerForRegisteredDomain() {
        SearchMonitorImpl searchMonitor = this.getTestInstance();
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);
        searchMonitor.activate();
        searchMonitor.searchDomainRegistered(this.searchDomain);

        // Business method
        ExecutionTimer timer = searchMonitor.countTimer(this.searchDomain);

        // Asserts
        assertThat(timer).isEqualTo(this.timer);
    }

    @Test
    public void getSearchTimerForUnregisteredDomain() {
        SearchMonitorImpl searchMonitor = this.getTestInstance();
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);

        // Business method
        ExecutionTimer timer = searchMonitor.searchTimer(this.searchDomain);

        // Asserts
        assertThat(timer).isNull();
    }

    @Test
    public void getCountTimerForUnregisteredDomain() {
        SearchMonitorImpl searchMonitor = this.getTestInstance();
        when(this.executionTimerService.newTimer(anyString(), any(Duration.class))).thenReturn(this.timer);

        // Business method
        ExecutionTimer timer = searchMonitor.countTimer(this.searchDomain);

        // Asserts
        assertThat(timer).isNull();
    }

    private SearchMonitorImpl getTestInstance() {
        return new SearchMonitorImpl(this.executionTimerService);
    }

}