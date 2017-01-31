/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;

import java.math.BigDecimal;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceConfigurationSearchableProperty} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (09:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigurationSearchablePropertyTest {

    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private DataModel dataModel;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;

    private DeviceTypeSearchableProperty deviceTypeSearchableProperty;
    private BeanService beanService = new DefaultBeanService();
    private com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private PropertySpecService propertySpecService;

    @Before
    public void initializeThesaurus() {
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(this.messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(this.messageFormat);
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
    }

    @Before
    public void initializeMocks() {
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.jupiterPropertySpecService = new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        this.propertySpecService = new PropertySpecServiceImpl(this.jupiterPropertySpecService, this.dataVaultService, this.ormService);
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.emptyList());
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
        this.deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
    }

    @Test
    public void testGetDomain() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testStickyVisibility() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.STICKY);
    }

    @Test
    public void testMultiSelection() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.DEVICE_CONFIGURATION);
    }

    @Test
    public void specificationIsReference() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isTrue();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(DeviceConfiguration.class);
    }

    @Test
    public void noPossibleValuesWithoutRefresh() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
        assertThat(specification.getPossibleValues().isExhaustive()).isTrue();
        assertThat(specification.getPossibleValues().getAllValues()).isEmpty();
    }

    @Test
    public void propertyIsConstraintByDeviceTypeProperty() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).isEqualTo(this.deviceTypeSearchableProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithoutConstrictions() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceType deviceType = mock(DeviceType.class);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, deviceType);
        SearchableProperty otherSearchableProperty = mock(SearchableProperty.class);
        SearchablePropertyConstriction otherConstriction = SearchablePropertyConstriction.noValues(otherSearchableProperty);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction, otherConstriction));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithConstrictionsOfWrongType() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, "Wrong", "type");

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts: see expected exception rule
    }

    @Test
    public void refresh() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceConfiguration config1 = mock(DeviceConfiguration.class);
        DeviceConfiguration config2 = mock(DeviceConfiguration.class);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(config1, config2));
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, deviceType);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly(config1, config2);
    }

    @Test
    public void refreshWithMultipleDeviceTypes() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceConfiguration config1 = mock(DeviceConfiguration.class);
        DeviceType deviceType1 = mock(DeviceType.class);
        when(deviceType1.getConfigurations()).thenReturn(Arrays.asList(config1));
        DeviceConfiguration config2 = mock(DeviceConfiguration.class);
        DeviceType deviceType2 = mock(DeviceType.class);
        when(deviceType2.getConfigurations()).thenReturn(Arrays.asList(config2));
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, deviceType1, deviceType2);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly(config2, config1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithConstrictionFromAnotherProperty() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceConfiguration config1 = mock(DeviceConfiguration.class);
        DeviceConfiguration config2 = mock(DeviceConfiguration.class);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(config1, config2));
        SearchableProperty otherSearchableProperty = mock(SearchableProperty.class);
        when(otherSearchableProperty.hasName(anyString())).thenReturn(false);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(otherSearchableProperty, deviceType);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly(config1, config2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayBigDecimalValue() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayStringValue() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay("displayString");

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void toDisplayWithoutRefresh() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceConfiguration configuration = mock(DeviceConfiguration.class);
        String expectedDisplayValue = "toDisplayWithoutRefresh";
        when(configuration.getName()).thenReturn(expectedDisplayValue);

        // Business method
        String displayString = property.toDisplay("displayString");

        // Asserts
        assertThat(displayString).isEqualTo(expectedDisplayValue);
    }

    @Test
    public void toDisplayAfterRefreshWithOneDeviceType() {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("deviceType");
        String expectedDisplayValue = "toDisplayAfterRefreshWithOneDeviceType";
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceConfiguration config1 = mock(DeviceConfiguration.class);
        when(config1.getName()).thenReturn(expectedDisplayValue);
        when(config1.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration config2 = mock(DeviceConfiguration.class);
        when(config2.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(config1, config2));
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, deviceType);
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Business method
        String displayString = property.toDisplay(config1);

        // Asserts
        assertThat(displayString).isEqualTo(expectedDisplayValue + " (deviceType)");
    }

    @Test
    public void toDisplayAfterRefreshWithTwoDeviceTypes() {
        String config1Name = "toDisplayAfterRefreshWithTwoDeviceTypes";
        DeviceType deviceType1 = mock(DeviceType.class);
        DeviceType deviceType2 = mock(DeviceType.class);
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceConfiguration config1 = mock(DeviceConfiguration.class);
        when(config1.getName()).thenReturn(config1Name);
        when(config1.getDeviceType()).thenReturn(deviceType1);
        when(deviceType1.getConfigurations()).thenReturn(Arrays.asList(config1));
        when(deviceType1.getName()).thenReturn("DT-One");
        DeviceConfiguration config2 = mock(DeviceConfiguration.class);
        when(config2.getDeviceType()).thenReturn(deviceType2);
        when(deviceType2.getConfigurations()).thenReturn(Arrays.asList(config2));
        when(deviceType2.getName()).thenReturn("DT-Two");
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, deviceType1, deviceType2);
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));
        String expectedDisplayValue = config1Name + " (" + deviceType1.getName() + ")";
        // Business method
        String displayString = property.toDisplay(config1);

        // Asserts
        assertThat(displayString).isEqualTo(expectedDisplayValue);
    }

    private DeviceConfigurationSearchableProperty getTestInstance() {
        return new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain, this.deviceTypeSearchableProperty);
    }

}