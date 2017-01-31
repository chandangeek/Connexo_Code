/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.device.Device;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SearchServiceImpl} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchServiceImplTest {

    @Mock
    private SearchMonitor searchMonitor;

    @Test
    public void registeredDomainsWithoutRegistrations() {
        // Business method
        List<SearchDomain> domains = this.getTestInstance().getDomains();

        // Asserts
        assertThat(domains).isEmpty();
        verify(this.searchMonitor, never()).searchDomainRegistered(any(SearchDomain.class));
    }

    @Test
    public void registeredDomainsWithSingleDomain() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn("id");
        searchService.register(searchDomain);

        // Business method
        List<SearchDomain> domains = searchService.getDomains();

        // Asserts
        assertThat(domains).hasSize(1);
        assertThat(domains.get(0).getId()).isEqualTo("id");
        verify(this.searchMonitor).searchDomainRegistered(searchDomain);
    }

    @Test
    public void registeredDomainsWithMultipleDomains() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain sd1 = mock(SearchDomain.class);
        when(sd1.getId()).thenReturn("id1");
        SearchDomain sd2 = mock(SearchDomain.class);
        when(sd2.getId()).thenReturn("id2");
        searchService.register(sd1);
        searchService.register(sd2);

        // Business method
        List<SearchDomain> domains = searchService.getDomains();

        // Asserts
        assertThat(domains).hasSize(2);
        assertThat(domains.stream().map(SearchDomain::getId).collect(Collectors.toList())).containsOnly("id1", "id2");
        verify(this.searchMonitor).searchDomainRegistered(sd1);
        verify(this.searchMonitor).searchDomainRegistered(sd2);
    }

    @Test
    public void unregisterDomains() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain sd1 = mock(SearchDomain.class);
        SearchDomain sd2 = mock(SearchDomain.class);
        when(sd2.getId()).thenReturn("id2");
        searchService.register(sd1);
        searchService.register(sd2);

        searchService.unregister(sd1);

        // Business method
        List<SearchDomain> domains = searchService.getDomains();

        // Asserts
        assertThat(domains).hasSize(1);
        assertThat(domains.get(0).getId()).isEqualTo("id2");
        verify(this.searchMonitor).searchDomainUnregistered(sd1);
    }

    @Test
    public void unregisterLastDomain() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        searchService.register(searchDomain);
        searchService.unregister(searchDomain);

        // Business method
        List<SearchDomain> domains = searchService.getDomains();

        // Asserts
        assertThat(domains).isEmpty();
    }

    @Test
    public void findDomainByIdWithNoRegisteredDomains() {
        SearchServiceImpl searchService = this.getTestInstance();

        // Business method
        Optional<SearchDomain> domain = searchService.findDomain("DoesNotExists");

        // Asserts
        assertThat(domain).isEmpty();
    }

    @Test
    public void findDomainByIdThatDoesNotExist() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn("findDomainByIdThatDoesNotExist");
        searchService.register(searchDomain);

        // Business method
        Optional<SearchDomain> domain = searchService.findDomain("DoesNotExists");

        // Asserts
        assertThat(domain).isEmpty();
    }

    @Test
    public void findDomainById() {
        String domainId = "findDomainById";
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(domainId);
        searchService.register(searchDomain);

        // Business method
        Optional<SearchDomain> domain = searchService.findDomain(domainId);

        // Asserts
        assertThat(domain).isPresent();
        assertThat(domain.get().getId()).isEqualTo(domainId);
    }

    @Test(timeout = 2000)
    public void pollDomainByIdWithNoRegisteredDomains() throws InterruptedException {
        SearchServiceImpl searchService = this.getTestInstance();

        // Business method
        Optional<SearchDomain> domain = searchService.pollDomain("DoesNotExists", Duration.ofSeconds(1));

        // Asserts
        assertThat(domain).isEmpty();
    }

    @Test(timeout = 2000)
    public void pollDomainByIdThatDoesNotExist() throws InterruptedException {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn("pollDomainByIdThatDoesNotExist");
        searchService.register(searchDomain);

        // Business method
        Optional<SearchDomain> domain = searchService.pollDomain("DoesNotExists", Duration.ofSeconds(1));

        // Asserts
        assertThat(domain).isEmpty();
    }

    @Test(timeout = 500)
    public void pollDomainById() throws InterruptedException {
        String domainId = "pollDomainById";
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(domainId);
        searchService.register(searchDomain);

        // Business method
        Optional<SearchDomain> domain = searchService.pollDomain(domainId, Duration.ofSeconds(1));

        // Asserts
        assertThat(domain).isPresent();
        assertThat(domain.map(SearchDomain::getId)).contains(searchDomain.getId());
    }

    @Test
    public void findDomainByApplicationWithoutRegistrations() {
        SearchServiceImpl searchService = this.getTestInstance();

        // Business method
        List<SearchDomain> domains = searchService.getDomains("APP");

        // Asserts
        assertThat(domains).isEmpty();
    }

    @Test
    public void findDomainByApplicationWithoutMatchingRegistrations() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain1 = mock(SearchDomain.class);
        when(searchDomain1.targetApplications()).thenReturn(Arrays.asList("APP-1", "APP-2"));
        searchService.register(searchDomain1);
        SearchDomain searchDomain2 = mock(SearchDomain.class);
        when(searchDomain2.targetApplications()).thenReturn(Arrays.asList("APP-3", "APP-4"));
        searchService.register(searchDomain2);

        // Business method
        List<SearchDomain> domains = searchService.getDomains("APP");

        // Asserts
        assertThat(domains).isEmpty();
    }

    @Test
    public void findDomainByApplicationWithSingleMatchingRegistration() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain1 = mock(SearchDomain.class);
        when(searchDomain1.targetApplications()).thenReturn(Arrays.asList("APP", "APP-2"));
        when(searchDomain1.getId()).thenReturn("id1");
        searchService.register(searchDomain1);
        SearchDomain searchDomain2 = mock(SearchDomain.class);
        when(searchDomain2.targetApplications()).thenReturn(Arrays.asList("APP-3", "APP-4"));
        when(searchDomain2.getId()).thenReturn("id2");
        searchService.register(searchDomain2);

        // Business method
        List<SearchDomain> domains = searchService.getDomains("APP");

        // Asserts
        assertThat(domains).hasSize(1);
        assertThat(domains.get(0).getId()).isEqualTo("id1");
    }

    @Test
    public void findDomainByApplicationWithMatchAllRegistrations() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain1 = mock(SearchDomain.class);
        when(searchDomain1.targetApplications()).thenReturn(Collections.emptyList());
        when(searchDomain1.getId()).thenReturn("id1");
        searchService.register(searchDomain1);
        SearchDomain searchDomain2 = mock(SearchDomain.class);
        when(searchDomain2.targetApplications()).thenReturn(Arrays.asList("APP-1", "APP-2"));
        when(searchDomain2.getId()).thenReturn("id2");
        searchService.register(searchDomain2);

        // Business method
        List<SearchDomain> domains = searchService.getDomains("APP");

        // Asserts
        assertThat(domains).hasSize(1);
        assertThat(domains.get(0).getId()).isEqualTo("id1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void startSearchWithNoDomainsRegistered() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);

        // Business method
        searchService.search(searchDomain);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void startSearchByClassWithNoDomainsRegistered() {
        SearchServiceImpl searchService = this.getTestInstance();

        // Business method
        searchService.search(SearchServiceImplTest.class);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void startSearchByClassThatWasNotRegistered() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        doReturn(SearchServiceImplTest.class).when(searchDomain).getDomainClass();
        when(searchDomain.getId()).thenReturn("SearchServiceImplTest");
        searchService.register(searchDomain);

        // Business method
        SearchBuilder<Device> builder = searchService.search(Device.class);

        // Asserts
        assertThat(builder).isNotNull();
    }

    @Test
    public void startSearchByClass() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        doReturn(SearchServiceImplTest.class).when(searchDomain).getDomainClass();
        when(searchDomain.getId()).thenReturn("SearchServiceImplTest");
        searchService.register(searchDomain);

        // Business method
        SearchBuilder<SearchServiceImplTest> builder = searchService.search(SearchServiceImplTest.class);

        // Asserts
        assertThat(builder).isNotNull();
    }

    @Test
    public void getLayerDoesNotReturnNull() {
        SearchServiceImpl searchService = this.getTestInstance();

        // Business method
        Layer layer = searchService.getLayer();

        // Asserts
        assertThat(layer).isNotNull();
    }

    @Test
    public void getSeedsDoesNotReturnNull() {
        SearchServiceImpl searchService = this.getTestInstance();

        // Business method
        List<MessageSeed> seeds = searchService.getSeeds();

        // Asserts
        assertThat(seeds).isNotNull();
    }

    private SearchServiceImpl getTestInstance() {
        return new SearchServiceImpl(this.searchMonitor);
    }

}