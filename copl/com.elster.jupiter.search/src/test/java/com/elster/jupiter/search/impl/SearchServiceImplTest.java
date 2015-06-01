package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import org.osgi.service.device.Device;

import java.util.List;
import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SearchServiceImpl} component.
 */
public class SearchServiceImplTest {

    @Test
    public void registeredDomainsWithoutRegistrations() {
        // Business method
        List<SearchDomain> domains = this.getTestInstance().getDomains();

        // Asserts
        assertThat(domains).isEmpty();
    }

    @Test
    public void registeredDomainsWithSingleDomain() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain searchDomain = mock(SearchDomain.class);
        searchService.register(searchDomain);

        // Business method
        List<SearchDomain> domains = searchService.getDomains();

        // Asserts
        assertThat(domains).containsOnly(searchDomain);
    }

    @Test
    public void registeredDomainsWithMultipleDomains() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain sd1 = mock(SearchDomain.class);
        SearchDomain sd2 = mock(SearchDomain.class);
        searchService.register(sd1);
        searchService.register(sd2);

        // Business method
        List<SearchDomain> domains = searchService.getDomains();

        // Asserts
        assertThat(domains).containsOnly(sd1, sd2);
    }

    @Test
    public void unregisterDomains() {
        SearchServiceImpl searchService = this.getTestInstance();
        SearchDomain sd1 = mock(SearchDomain.class);
        SearchDomain sd2 = mock(SearchDomain.class);
        searchService.register(sd1);
        searchService.register(sd2);

        searchService.unregister(sd1);

        // Business method
        List<SearchDomain> domains = searchService.getDomains();

        // Asserts
        assertThat(domains).containsOnly(sd2);
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
        assertThat(domain).contains(searchDomain);
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
        when(searchDomain.supports(SearchServiceImplTest.class)).thenReturn(true);
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
        when(searchDomain.supports(SearchServiceImplTest.class)).thenReturn(true);
        when(searchDomain.getId()).thenReturn("SearchServiceImplTest");
        searchService.register(searchDomain);

        // Business method
        SearchBuilder<SearchServiceImplTest> builder = searchService.search(SearchServiceImplTest.class);

        // Asserts
        assertThat(builder).isNotNull();
    }

    private SearchServiceImpl getTestInstance() {
        return new SearchServiceImpl();
    }

}