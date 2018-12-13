/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SearchDomainExtensionSearchablePropertyTest {
    @Mock
    private SearchDomain searchDomain;

    @Mock
    private SearchDomainExtension searchDomainExtension;

    @Mock
    private SearchableProperty searchableProperty;

    private SearchDomainExtensionSearchableProperty getTestInstance() {
        return new SearchDomainExtensionSearchableProperty(this.searchDomain, this.searchDomainExtension, this.searchableProperty);
    }

    @Test
    public void testGetDomain() {
        assertThat(getTestInstance().getDomain()).isEqualTo(this.searchDomain);
    }

    @Test
    public void testAffectsAvailableDomainProperties() {
        getTestInstance().affectsAvailableDomainProperties();

        verify(this.searchableProperty).affectsAvailableDomainProperties();
    }

    @Test
    public void testGetGroup() {
        getTestInstance().getGroup();

        verify(this.searchableProperty).getGroup();
    }

    @Test
    public void testGetSpecification() {
        getTestInstance().getSpecification();

        verify(this.searchableProperty).getSpecification();
    }

    @Test
    public void testGetVisibility() {
        getTestInstance().getVisibility();

        verify(this.searchableProperty).getVisibility();
    }

    @Test
    public void testGetSelectionMode() {
        getTestInstance().getSelectionMode();

        verify(this.searchableProperty).getSelectionMode();
    }

    @Test
    public void testGetName() {
        getTestInstance().getName();

        verify(this.searchableProperty).getName();
    }

    @Test
    public void testGetDisplayName() {
        getTestInstance().getDisplayName();

        verify(this.searchableProperty).getDisplayName();
    }

    @Test
    public void testToDisplay() {
        getTestInstance().toDisplay("value");

        verify(this.searchableProperty).toDisplay(eq("value"));
    }

    @Test
    public void testHasName() {
        getTestInstance().hasName("value");

        verify(this.searchableProperty).hasName(eq("value"));
    }

    @Test
    public void testGetConstraints() {
        getTestInstance().getConstraints();

        verify(this.searchableProperty).getConstraints();
    }

    @Test
    public void testRefreshWithConstrictions() {
        List<SearchablePropertyConstriction> constrictions = Collections.emptyList();
        getTestInstance().refreshWithConstrictions(constrictions);

        verify(this.searchableProperty).refreshWithConstrictions(eq(constrictions));
    }
}
