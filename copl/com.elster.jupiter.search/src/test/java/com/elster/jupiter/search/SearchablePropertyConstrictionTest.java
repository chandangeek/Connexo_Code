/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.PropertySpecPossibleValuesImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SearchablePropertyConstriction} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (15:16)
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchablePropertyConstrictionTest {

    private static final String ID_PROPERTY_NAME = "id";
    private static final long ID1_VALUE = 97L;
    private static final long ID2_VALUE = 101L;
    private static final long ID3_VALUE = 103L;

    @Mock
    private SearchDomain searchDomain;
    @Mock
    private SearchableProperty idSearchProperty;

    @Before
    public void initializeMocks() {
        PropertySpec idPropertySpec = mock(PropertySpec.class);
        when(idPropertySpec.getName()).thenReturn(ID_PROPERTY_NAME);
        when(idPropertySpec.isRequired()).thenReturn(true);
        when(idPropertySpec.isReference()).thenReturn(false);
        when(idPropertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        when(idPropertySpec.getPossibleValues()).thenReturn(new PropertySpecPossibleValuesImpl());
        when(this.idSearchProperty.getConstraints()).thenReturn(Collections.<SearchableProperty>emptyList());
        when(this.idSearchProperty.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(this.idSearchProperty.getSpecification()).thenReturn(idPropertySpec);
        when(this.idSearchProperty.hasName(ID_PROPERTY_NAME)).thenReturn(true);
        when(this.searchDomain.getId()).thenReturn(Example.class.getName());
        doReturn(Example.class).when(this.searchDomain).getDomainClass();
        when(this.searchDomain.getProperties()).thenReturn(Arrays.asList(this.idSearchProperty));
    }

    @Test
    public void noValues() {
        // Business method
        SearchablePropertyConstriction constriction = SearchablePropertyConstriction.noValues(this.idSearchProperty);

        // Asserts
        assertThat(constriction.getConstrainingProperty()).isEqualTo(this.idSearchProperty);
        assertThat(constriction.getConstrainingValues()).isEmpty();
    }

    @Test
    public void withValues() {
        // Business method
        SearchablePropertyConstriction constriction = SearchablePropertyConstriction.withValues(this.idSearchProperty, ID1_VALUE, ID2_VALUE, ID3_VALUE);

        // Asserts
        assertThat(constriction.getConstrainingProperty()).isEqualTo(this.idSearchProperty);
        assertThat(constriction.getConstrainingValues()).containsOnly(ID1_VALUE, ID2_VALUE, ID3_VALUE);
    }

    private class Example {
        private String id;
        private String name;
    }

}