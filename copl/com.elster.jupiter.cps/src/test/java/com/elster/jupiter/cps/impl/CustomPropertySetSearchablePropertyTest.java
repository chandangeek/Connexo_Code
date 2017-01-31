/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomPropertySetSearchablePropertyTest {

    @Mock
    private CustomPropertySetServiceImpl customPropertySetService;
    @Mock
    private CustomPropertySet<?, ?> customPropertySet;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private SearchablePropertyGroup propertyGroup;

    @Before
    public void setUp() {
        when(this.propertySpec.getName()).thenReturn("property.specification");
        when(this.customPropertySet.getId()).thenReturn("custom.property.set");
        when(this.customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(this.propertySpec));
    }

    private CustomPropertySetSearchableProperty getTestInstance() {
        return this.getTestInstance(Collections.emptyList());
    }

    private CustomPropertySetSearchableProperty getTestInstance(List<SearchableProperty> constraints) {
        return new CustomPropertySetSearchableProperty(this.customPropertySet, this.propertySpec, this.propertyGroup, constraints);
    }

    @Test
    public void testGetSearchDomain() {
        assertThat(getTestInstance().getDomain()).isNull();
    }

    @Test
    public void testAffectsDomainProperties() {
        assertThat(getTestInstance().affectsAvailableDomainProperties()).isFalse();
    }

    @Test
    public void testGetGroup() {
        Optional<SearchablePropertyGroup> group = getTestInstance().getGroup();
        assertThat(group).isPresent();
        assertThat(group.get()).isEqualTo(this.propertyGroup);
    }

    @Test
    public void testName() {
        assertThat(getTestInstance().getName()).isEqualTo("custom.property.set.property.specification");
    }

    @Test
    public void testGetSpecification() {
        assertThat(getTestInstance().getSpecification()).isEqualTo(this.propertySpec);
    }

    @Test
    public void testGetVisibility() {
        assertThat(getTestInstance().getVisibility()).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testGetMultiSelectionMode() {
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getAllValues()).thenReturn(Collections.singletonList(new Object()));
        when(this.propertySpec.getPossibleValues()).thenReturn(possibleValues);
        assertThat(getTestInstance().getSelectionMode()).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testGetSingleSelectionMode() {
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getAllValues()).thenReturn(Collections.emptyList());
        when(this.propertySpec.getPossibleValues()).thenReturn(possibleValues);
        assertThat(getTestInstance().getSelectionMode()).isEqualTo(SearchableProperty.SelectionMode.SINGLE);
    }

    @Test
    public void testGetSingleSelectionModeWithNull() {
        when(this.propertySpec.getPossibleValues()).thenReturn(null);
        assertThat(getTestInstance().getSelectionMode()).isEqualTo(SearchableProperty.SelectionMode.SINGLE);
    }

    @Test
    public void testGetDisplayName() {
        when(this.propertySpec.getDisplayName()).thenReturn("Property");
        assertThat(getTestInstance().getDisplayName()).isEqualTo("Property");
        verify(this.propertySpec).getDisplayName();
    }

    @Test
    public void testToDisplay() {
        assertThat(getTestInstance().toDisplay("Value")).isEqualTo("Value");
    }

    @Test
    public void getConstraints() {
        List<SearchableProperty> constraints = new ArrayList<>();
        assertThat(getTestInstance(constraints).getConstraints()).isEqualTo(constraints);
    }
}
