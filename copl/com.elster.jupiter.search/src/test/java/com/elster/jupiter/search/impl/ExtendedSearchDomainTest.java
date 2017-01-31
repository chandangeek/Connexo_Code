/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExtendedSearchDomainTest {
    @Mock
    private SearchDomain searchDomain;
    @Mock
    private OrmService ormService;
    @Mock
    private SearchServiceImpl searchService;
    @Mock
    private SearchDomainExtension domainExtension;
    @Mock
    private SearchableProperty extensionProperty;

    private ExtendedSearchDomain getTestInstance() {
        return new ExtendedSearchDomain(this.ormService, this.searchService, this.searchDomain);
    }

    @Test
    public void testGetId() {
        when(this.searchDomain.getId()).thenReturn("id");
        assertThat(getTestInstance().getId()).isEqualTo("id");
        verify(this.searchDomain).getId();
    }

    @Test
    public void testDisplayName() {
        when(this.searchDomain.displayName()).thenReturn("id");
        assertThat(getTestInstance().displayName()).isEqualTo("id");
        verify(this.searchDomain).displayName();
    }

    @Test
    public void testTargetApplications() {
        List<String> applications = Collections.emptyList();
        when(this.searchDomain.targetApplications()).thenReturn(applications);
        assertThat(getTestInstance().targetApplications()).isEqualTo(applications);
        verify(this.searchDomain).targetApplications();
    }

    @Test
    public void testGetDomainClass() {
        doReturn(Object.class).when(this.searchDomain).getDomainClass();
        assertThat(getTestInstance().getDomainClass()).isEqualTo(Object.class);
        verify(this.searchDomain).getDomainClass();
    }

    @Test
    public void testGetProperties() {
        when(this.searchService.getSearchExtensions()).thenReturn(Collections.singletonList(this.domainExtension));
        when(this.searchDomain.getProperties()).thenReturn(Collections.emptyList());
        when(this.domainExtension.isExtensionFor(eq(this.searchDomain), anyList())).thenReturn(true);
        when(this.domainExtension.getProperties()).thenReturn(Collections.singletonList(this.extensionProperty));
        when(this.extensionProperty.getName()).thenReturn("id");

        List<SearchableProperty> properties = getTestInstance().getProperties();
        assertThat(properties).hasSize(1);
        assertThat(properties.get(0).getName()).isEqualTo("id");

        verify(this.domainExtension).getProperties();
        verify(this.searchDomain).getProperties();
        verify(this.searchService).getSearchExtensions();
    }

    @Test
    public void testGetPropertiesNoExtensions() {
        List<SearchableProperty> originalSearchableProperties = Collections.emptyList();
        when(this.searchService.getSearchExtensions()).thenReturn(Collections.emptyList());
        when(this.searchDomain.getProperties()).thenReturn(originalSearchableProperties);

        List<SearchableProperty> properties = getTestInstance().getProperties();
        assertThat(properties).hasSize(0);

        verify(this.domainExtension, never()).getProperties();
        verify(this.searchDomain).getProperties();
        verify(this.searchService).getSearchExtensions();
    }

    @Test
    public void testGetPropertiesWithConstrictions() {
        when(this.searchService.getSearchExtensions()).thenReturn(Collections.singletonList(this.domainExtension));
        when(this.searchDomain.getPropertiesWithConstrictions(anyList())).thenReturn(Collections.emptyList());
        when(this.domainExtension.isExtensionFor(eq(this.searchDomain), anyList())).thenReturn(true);
        when(this.domainExtension.getPropertiesWithConstrictions(anyList())).thenReturn(Collections.singletonList(this.extensionProperty));
        when(this.extensionProperty.getName()).thenReturn("id");

        List<SearchableProperty> properties = getTestInstance().getPropertiesWithConstrictions(Collections.emptyList());
        assertThat(properties).hasSize(1);
        assertThat(properties.get(0).getName()).isEqualTo("id");

        verify(this.domainExtension).getPropertiesWithConstrictions(anyList());
        verify(this.searchDomain).getPropertiesWithConstrictions(anyList());
        verify(this.searchService).getSearchExtensions();
    }

    @Test
    public void testGetPropertiesWithConstrictionsNoExtensions() {
        when(this.searchService.getSearchExtensions()).thenReturn(Collections.emptyList());
        when(this.searchDomain.getPropertiesWithConstrictions(anyList())).thenReturn(Collections.emptyList());
        when(this.domainExtension.getPropertiesWithConstrictions(anyList())).thenReturn(Collections.singletonList(this.extensionProperty));
        when(this.extensionProperty.getName()).thenReturn("id");

        List<SearchableProperty> properties = getTestInstance().getPropertiesWithConstrictions(Collections.emptyList());
        assertThat(properties).hasSize(0);

        verify(this.domainExtension, never()).getPropertiesWithConstrictions(anyList());
        verify(this.searchDomain).getPropertiesWithConstrictions(anyList());
        verify(this.searchService).getSearchExtensions();
    }
}
