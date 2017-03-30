/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractReadingTypeUnitOfMeasureSearchablePropertyTest {

    @Mock
    DataVaultService dataVaultService;
    @Mock
    DataModel dataModel;
    @Mock
    TimeService timeService;
    @Mock
    OrmService ormService;
    @Mock
    DeviceSearchDomain domain;
    @Mock
    Thesaurus thesaurus;
    @Mock
    MeteringService meteringService;

    private BeanService beanService = new DefaultBeanService();
    com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.READING_TYPE_UNIT_OF_MEASURE.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.READING_TYPE_UNIT_OF_MEASURE)).thenReturn(messageFormat);
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        ReadingType rt1 = mockReadingType(MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);
        ReadingType rt2 = mockReadingType(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
        ReadingType rt3 = mockReadingType(MetricMultiplier.KILO, ReadingTypeUnit.WATT);
        when(meteringService.getAvailableNonEquidistantReadingTypes()).thenReturn(Arrays.asList(rt1, rt2, rt3));
        this.jupiterPropertySpecService = new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(timeService, this.ormService, this.beanService);
        this.propertySpecService = new PropertySpecServiceImpl(jupiterPropertySpecService, dataVaultService, ormService);
    }

    private ReadingType mockReadingType(MetricMultiplier multiplier, ReadingTypeUnit unit) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMultiplier()).thenReturn(multiplier);
        when(readingType.getUnit()).thenReturn(unit);
        return readingType;
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
    public void testVisibility() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testSelectionMode() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.READING_TYPE_UNIT_OF_MEASURE);
    }

    @Test
    public void testSpecification() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(RegisterReadingTypeUnitOfMeasureSearchableProperty.UnitOfMeasureInfo.class);
    }

    @Test
    public void testPossibleValues() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        List<RegisterReadingTypeUnitOfMeasureSearchableProperty.UnitOfMeasureInfo> allValues = specification.getPossibleValues().getAllValues();
        assertThat(allValues).hasSize(3);
        assertThat(allValues.get(0).getId()).isEqualTo("3:38");
        assertThat(allValues.get(0).getName()).isEqualTo("kW");
        assertThat(allValues.get(1).getId()).isEqualTo("3:72");
        assertThat(allValues.get(1).getName()).isEqualTo("kWh");
        assertThat(allValues.get(2).getId()).isEqualTo("0:72");
        assertThat(allValues.get(2).getName()).isEqualTo("Wh");
    }

    @Test
    public void testPropertyHasNoConstraints() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    protected abstract SearchableProperty getTestInstance();
}
