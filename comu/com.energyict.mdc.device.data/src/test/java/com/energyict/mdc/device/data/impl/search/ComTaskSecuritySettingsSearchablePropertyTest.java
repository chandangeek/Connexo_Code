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
import com.energyict.mdc.device.config.SecurityPropertySet;
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

@RunWith(MockitoJUnitRunner.class)
public class ComTaskSecuritySettingsSearchablePropertyTest {

    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private TimeService timeService;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private SearchablePropertyGroup parentGroup;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private Finder deviceTypeFinder;

    @Mock
    private SecurityPropertySet sps1;
    @Mock
    private SecurityPropertySet sps2;

    private BeanService beanService = new DefaultBeanService();
    private PropertySpecService propertySpecService;
    private DeviceTypeSearchableProperty deviceTypeSearchableProperty;

    @Before
    public void initializeThesaurus() {
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(this.messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(this.messageFormat);
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
    }

    @Before
    public void initializeMocks() {
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.propertySpecService = new PropertySpecServiceImpl(new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService), this.dataVaultService, this.ormService);

        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);
        this.deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
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
        assertThat(group.get()).isEqualTo(parentGroup);
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
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.COMTASK_SECURITY_SETTING);
    }

    @Test
    public void testSpecification() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isTrue();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(SecurityPropertySet.class);
    }

    @Test
    public void noPossibleValuesWithoutRefresh() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
        assertThat(specification.getPossibleValues().getAllValues()).hasSize(0);
        assertThat(specification.getPossibleValues().isExhaustive()).isTrue();
    }

    @Test
    public void propertyIsConstraintByDeviceTypeProperty() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).isEqualTo(this.deviceTypeSearchableProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithoutConstrictions() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        SearchableProperty property = this.getTestInstance();
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
        SearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, "Wrong", "type");

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts: see expected exception rule
    }

    @Test
    public void refreshWithConstriction() {
        DeviceConfiguration configuration1 = mock(DeviceConfiguration.class);
        when(configuration1.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));

        DeviceConfiguration configuration2 = mock(DeviceConfiguration.class);
        when(configuration2.getSecurityPropertySets()).thenReturn(Arrays.asList(sps2));

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(configuration1, configuration2));
        when(sps1.getName()).thenReturn("sps1");
        when(sps2.getName()).thenReturn("sps2");

        SearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, deviceType);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly(sps1, sps2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayWrongObject() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void displayCorrectValue() {
        SearchableProperty property = this.getTestInstance();
        String expectedDisplayValue = "sps (type / config)";
        when(sps1.getName()).thenReturn("sps");
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("type");
        DeviceConfiguration configuration = mock(DeviceConfiguration.class);
        when(configuration.getName()).thenReturn("config");
        when(configuration.getDeviceType()).thenReturn(deviceType);
        when(sps1.getDeviceConfiguration()).thenReturn(configuration);

        // Business method
        String displayValue = property.toDisplay(sps1);

        // Asserts
        assertThat(displayValue).isEqualTo(expectedDisplayValue);
    }

    private SearchableProperty getTestInstance() {
        return new ComTaskSecuritySettingSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain, this.parentGroup, this.deviceTypeSearchableProperty);
    }

}