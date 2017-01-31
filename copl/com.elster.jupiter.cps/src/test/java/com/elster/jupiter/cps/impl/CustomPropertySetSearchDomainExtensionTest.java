/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomPropertySetSearchDomainExtensionTest {
    @Mock
    private CustomPropertySetServiceImpl customPropertySetService;
    @Mock
    private CustomPropertySet<?, ?> customPropertySet;
    @Mock
    private DataModel dataModel;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private ActiveCustomPropertySet activeCustomPropertySet;

    @Before
    public void setUp() {
        when(this.activeCustomPropertySet.getDataModel()).thenReturn(this.dataModel);
        when(this.activeCustomPropertySet.getCustomPropertySet()).thenReturn(this.customPropertySet);
        when(this.customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(this.propertySpec));
    }

    private CustomPropertySetSearchDomainExtension getTestInstance() {
        return new CustomPropertySetSearchDomainExtension(this.customPropertySetService, this.activeCustomPropertySet);
    }

    @Test
    public void testIsExtensionForByDefault() {
        SearchDomain searchDomain = mock(SearchDomain.class);
        doReturn(Object.class).when(searchDomain).getDomainClass();
        doReturn(Object.class).when(this.customPropertySet).getDomainClass();
        when(this.customPropertySet.isSearchableByDefault()).thenReturn(true);
        List<SearchablePropertyConstriction> constrictions = Collections.emptyList();

        assertThat(getTestInstance().isExtensionFor(searchDomain, constrictions)).isTrue();

        verify(searchDomain).getDomainClass();
        verify(this.customPropertySet).getDomainClass();
        verify(this.customPropertySet).isSearchableByDefault();
        verify(this.customPropertySetService, never()).isSearchEnabledForCustomPropertySet(eq(this.customPropertySet), eq(constrictions));
    }

    @Test
    public void testIsExtensionFor() {
        SearchDomain searchDomain = mock(SearchDomain.class);
        doReturn(Object.class).when(searchDomain).getDomainClass();
        doReturn(Object.class).when(this.customPropertySet).getDomainClass();
        when(this.customPropertySet.isSearchableByDefault()).thenReturn(false);
        List<SearchablePropertyConstriction> constrictions = Collections.emptyList();
        when(this.customPropertySetService.isSearchEnabledForCustomPropertySet(eq(this.customPropertySet), eq(constrictions))).thenReturn(true);

        assertThat(getTestInstance().isExtensionFor(searchDomain, constrictions)).isTrue();

        verify(searchDomain).getDomainClass();
        verify(this.customPropertySet).getDomainClass();
        verify(this.customPropertySet).isSearchableByDefault();
        verify(this.customPropertySetService).isSearchEnabledForCustomPropertySet(eq(this.customPropertySet), eq(constrictions));
    }

    @Test
    public void testIsExtensionForUnsupportedClass() {
        SearchDomain searchDomain = mock(SearchDomain.class);
        doReturn(String.class).when(searchDomain).getDomainClass();
        doReturn(Object.class).when(this.customPropertySet).getDomainClass();


        assertThat(getTestInstance().isExtensionFor(searchDomain, Collections.emptyList())).isFalse();

        verify(searchDomain).getDomainClass();
        verify(this.customPropertySet).getDomainClass();
        verify(this.customPropertySet, never()).isSearchableByDefault();
        verify(this.customPropertySetService, never())
                .isSearchEnabledForCustomPropertySet(eq(this.customPropertySet), anyListOf(SearchablePropertyConstriction.class));
    }

    @Test
    public void testNoPropertiesIfNotSearchableByDefault() {
        when(this.customPropertySet.isSearchableByDefault()).thenReturn(false);
        assertThat(getTestInstance().getProperties()).isEmpty();
    }

    @Test
    public void testHasPropertiesIfSearchableByDefault() {
        when(this.customPropertySet.isSearchableByDefault()).thenReturn(true);
        List<SearchableProperty> properties = getTestInstance().getProperties();

        assertThat(properties).hasSize(1);
        assertThat(properties.get(0).getSpecification()).isEqualTo(this.propertySpec);
    }

    @Test
    public void testGetDynamicPropertiesIfSearchableByDefault() {
        when(this.customPropertySet.isSearchableByDefault()).thenReturn(true);
        CustomPropertySetSearchDomainExtension instance = spy(getTestInstance());
        assertThat(instance.getPropertiesWithConstrictions(Collections.emptyList())).hasSize(1);

        verify(instance).getProperties();
    }

    @Test
    public void testGetDynamicPropertiesIfNotSearchableByDefault() {
        List<SearchableProperty> constrictions = new ArrayList<>();
        when(this.customPropertySet.isSearchableByDefault()).thenReturn(false);
        when(this.customPropertySetService.getConstrainingPropertiesForCustomPropertySet(
                eq(this.customPropertySet), anyListOf(SearchablePropertyConstriction.class)))
                .thenReturn(constrictions);
        CustomPropertySetSearchDomainExtension instance = spy(getTestInstance());
        List<SearchablePropertyConstriction> basePropConstrictions = Collections.emptyList();

        List<SearchableProperty> properties = instance.getPropertiesWithConstrictions(basePropConstrictions);
        assertThat(properties).hasSize(1);
        assertThat(properties.get(0).getConstraints()).isEqualTo(constrictions);

        verify(this.customPropertySetService)
                .getConstrainingPropertiesForCustomPropertySet(eq(this.customPropertySet), anyListOf(SearchablePropertyConstriction.class));
    }

    @Test
    public void testQueryOnCorrectDomain() {
        PersistenceSupport persistenceSupport = mock(PersistenceSupport.class);
        when(persistenceSupport.persistenceClass()).thenReturn(Object.class);
        when(persistenceSupport.domainFieldName()).thenReturn("id");
        when(this.customPropertySet.getPersistenceSupport()).thenReturn(persistenceSupport);
        QueryExecutor queryExecutor = mock(QueryExecutor.class);
        SqlFragment fragment = mock(SqlFragment.class);
        doReturn(fragment).when(queryExecutor).asFragment(any(Condition.class), anyVararg());
        when(this.dataModel.query(any())).thenReturn(queryExecutor);

        assertThat(getTestInstance().asFragment(Collections.emptyList())).isEqualTo(fragment);
        verify(this.dataModel).query(eq(Object.class));
        verify(queryExecutor).asFragment(any(Condition.class), eq("id"));
    }
}
