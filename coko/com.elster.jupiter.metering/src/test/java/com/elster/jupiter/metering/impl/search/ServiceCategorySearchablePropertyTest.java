/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ServiceKind;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

/**
 * Tests the {@link ServiceCategorySearchableProperty} component
 *
 * @author Anton Fomchenko
 * @since 09-09-2015
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceCategorySearchablePropertyTest {

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
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testStickyVisibility() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.STICKY);
    }

    @Test
    public void testMultiSelection() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.USAGEPOINT_SERVICECATEGORY);
    }

    @Test
    public void specificationIsNotAReference() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(Enum.class);
    }

    @Test
    public void hasPossibleValuesWithoutRefresh() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
    }

    @Test
    public void noConstraints() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test
    public void refreshWithoutConstrictions() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts
        PropertySpec specification = property.getSpecification();
        assertThat(specification.getPossibleValues()).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayBigDecimal() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayString() {
        ServiceCategorySearchableProperty property = this.getTestInstance();
        String valueToDisplay = "displayString";

        // Business method
        property.toDisplay(valueToDisplay);

        // Asserts: see expected exception rule
    }

    @Test
    public void displayEnum() {
        ServiceCategorySearchableProperty property = this.getTestInstance();
        ServiceKind valueToDisplay = ServiceKind.ELECTRICITY;
        Stream.of(ServiceKind.values()).forEach(this::mockTranslationFor);

        // Business method
        String displayValue = property.toDisplay(valueToDisplay);

        // Asserts
        assertThat(displayValue).isEqualTo(ServiceKind.ELECTRICITY.getDefaultFormat());
    }

    private void mockTranslationFor(ServiceKind serviceKind) {
        when(this.meteringTranslationService.getDisplayName(serviceKind)).thenReturn(serviceKind.getDefaultFormat());
    }

    private ServiceCategorySearchableProperty getTestInstance() {
        return new ServiceCategorySearchableProperty(this.domain, this.propertySpecService, this.meteringTranslationService, this.thesaurus);
    }

}