/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDynamicSearchablePropertyTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    DeviceSearchDomain domain;
    @Mock
    SearchablePropertyGroup group;
    @Mock
    PropertySpec propertySpec;
    @Mock
    SearchableProperty searchableProperty;

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Before
    public void initializeThesaurus() {
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(this.messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(this.messageFormat);
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
    }

    @Test
    public void testGetDomain() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testGroup() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get()).isEqualTo(this.group);
    }

    @Test
    public void testVisibility() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testMultiSelectionForNonEmptyDefault() {
        PropertySpecPossibleValues propertySpecPossibleValues = mock(PropertySpecPossibleValues.class);
        when(propertySpecPossibleValues.getAllValues()).thenReturn(Arrays.asList(1, 2));
        when(this.propertySpec.getPossibleValues()).thenReturn(propertySpecPossibleValues);
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testMultiSelectionForEmptyDefault() {
        PropertySpecPossibleValues propertySpecPossibleValues = mock(PropertySpecPossibleValues.class);
        when(propertySpecPossibleValues.getAllValues()).thenReturn(Collections.emptyList());
        when(this.propertySpec.getPossibleValues()).thenReturn(propertySpecPossibleValues);
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.SINGLE);
    }

    @Test
    public void testAffectsDomainSearchProperties() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        boolean affectsAvailableDomainProperties = property.affectsAvailableDomainProperties();

        // Asserts
        assertThat(affectsAvailableDomainProperties).isFalse();
    }

    @Test
    public void testTranslation() {
        String expected = "PropertyName";
        when(propertySpec.getName()).thenReturn(expected);
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.propertySpec).getDisplayName();
    }

    @Test
    public void testSpecification() {
        when(propertySpec.isReference()).thenReturn(true);
        SearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isEqualTo(propertySpec.isReference());
    }

    @Test
    public void testPropertyHasConstraints() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).hasSize(1);
    }

    protected abstract SearchableProperty getTestInstance();
}
