/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.time.Clock;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalCapacitySearchablePropertyTest {
    @Mock
    private UsagePointSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private Clock clock;
    @Mock
    private MeteringTranslationService meteringTranslationService;

    private BeanService beanService = new DefaultBeanService();
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        this.propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(this.messageFormat);
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not support in unit tests");
    }

    @Test
    public void testGetDomain() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testWateryGroup() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group.get().getClass()).isEqualTo(WaterAttributesSearchablePropertyGroup.class);
    }

    @Test
    public void testRemovableVisibility() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testSingleSelection() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.SINGLE);
    }

    @Test
    public void testTranslation() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY);
    }

    @Test
    public void specificationIsNotAReference() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(Quantity.class);
    }

    @Test
    public void possibleValuesWithoutRefresh() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
    }

    @Test
    public void hasConstraints() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isNotEmpty();
    }

    @Test
    public void refreshWithoutConstrictions() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts
        PropertySpec specification = property.getSpecification();
        assertThat(specification.getPossibleValues()).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayBigDecimal() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void displayString() {
        PhysicalCapacitySearchableProperty property = this.getTestInstance();
        Quantity valueToDisplay = Quantity.create(BigDecimal.ZERO, 0, "Pa");

        // Business method
        String displayValue = property.toDisplay(valueToDisplay);

        // Asserts
        assertThat(displayValue).isEqualToIgnoringCase(valueToDisplay.getUnit().getSymbol());
    }

    private PhysicalCapacitySearchableProperty getTestInstance() {
        return new PhysicalCapacitySearchableProperty(this.propertySpecService, this.meteringTranslationService, this.thesaurus).init(this.domain, new WaterAttributesSearchablePropertyGroup(this.thesaurus), this.clock);
    }
}
