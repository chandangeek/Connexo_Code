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
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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
public class ProtocolDialectSearchablePropertyTest {

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
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private Finder deviceTypeFinder;

    private DeviceTypeSearchableProperty deviceTypeSearchableProperty;
    private BeanService beanService = new DefaultBeanService();
    private PropertySpecService propertySpecService;

    @Before
    public void initializeThesaurus() {
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(this.messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(this.messageFormat);
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
    }

    @Before
    public void initializeMocks() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        com.energyict.mdc.dynamic.PropertySpecService mdcPropertySpecService = new com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl(this.propertySpecService, dataVaultService, ormService);
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);
        this.deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, mdcPropertySpecService, this.thesaurus);
    }

    @Test
    public void testGetDomain() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testVisibility() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testMultiSelection() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testAffectsDomainSearchProperties() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        boolean affectsAvailableDomainProperties = property.affectsAvailableDomainProperties();

        // Asserts
        assertThat(affectsAvailableDomainProperties).isTrue();
    }

    @Test
    public void testTranslation() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.PROTOCOL_DIALECT);
    }

    @Test
    public void specificationIsAReference() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(ProtocolDialectSearchableProperty.ProtocolDialect.class);
    }

    @Test
    public void noPossibleValuesWithoutRefresh() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
        assertThat(specification.getPossibleValues().getAllValues()).isEmpty();
        assertThat(specification.getPossibleValues().isExhaustive()).isTrue();
    }

    @Test
    public void propertyIsConstraintByDeviceTypeProperty() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).isEqualTo(this.deviceTypeSearchableProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithoutConstrictions() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();
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
        ProtocolDialectSearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, "Wrong", "type");

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts: see expected exception rule
    }

    @Test
    public void refreshWithConstriction() {
        DeviceProtocolDialect protocolDialect1 = mock(DeviceProtocolDialect.class);
        when(protocolDialect1.getDisplayName()).thenReturn("Dialect 1");
        when(protocolDialect1.getDeviceProtocolDialectName()).thenReturn("dialect.1");
        DeviceProtocolDialect protocolDialect2 = mock(DeviceProtocolDialect.class);
        when(protocolDialect2.getDisplayName()).thenReturn("Dialect 2");
        when(protocolDialect2.getDeviceProtocolDialectName()).thenReturn("dialect.2");
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Arrays.asList(protocolDialect1, protocolDialect2));
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));

        ProtocolDialectSearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, deviceType);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues())
                .containsOnly(
                        new ProtocolDialectSearchableProperty.ProtocolDialect(pluggableClass, protocolDialect1),
                        new ProtocolDialectSearchableProperty.ProtocolDialect(pluggableClass, protocolDialect2));
    }

    @Test
    public void refreshWithMultipleDeviceTypes() {
        DeviceProtocolDialect protocolDialect1 = mock(DeviceProtocolDialect.class);
        when(protocolDialect1.getDisplayName()).thenReturn("Dialect 1");
        when(protocolDialect1.getDeviceProtocolDialectName()).thenReturn("dialect.1");
        DeviceProtocol deviceProtocol1 = mock(DeviceProtocol.class);
        when(deviceProtocol1.getDeviceProtocolDialects()).thenReturn(Arrays.asList(protocolDialect1));
        DeviceProtocolPluggableClass pluggableClass1 = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass1.getDeviceProtocol()).thenReturn(deviceProtocol1);
        DeviceType deviceType1 = mock(DeviceType.class);
        when(deviceType1.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass1));

        DeviceProtocolDialect protocolDialect2 = mock(DeviceProtocolDialect.class);
        when(protocolDialect2.getDisplayName()).thenReturn("Dialect 2");
        when(protocolDialect2.getDeviceProtocolDialectName()).thenReturn("dialect.2");
        DeviceProtocol deviceProtocol2 = mock(DeviceProtocol.class);
        when(deviceProtocol2.getDeviceProtocolDialects()).thenReturn(Arrays.asList(protocolDialect2));
        DeviceProtocolPluggableClass pluggableClass2 = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass2.getDeviceProtocol()).thenReturn(deviceProtocol2);
        DeviceType deviceType2 = mock(DeviceType.class);
        when(deviceType2.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass2));

        ProtocolDialectSearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, deviceType1, deviceType2);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues())
                .containsOnly(
                        new ProtocolDialectSearchableProperty.ProtocolDialect(pluggableClass1, protocolDialect1),
                        new ProtocolDialectSearchableProperty.ProtocolDialect(pluggableClass2, protocolDialect2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithConstrictionFromAnotherProperty() {
        ProtocolDialectSearchableProperty property = this.getTestInstance();
        SearchableProperty otherProperty = mock(SearchableProperty.class);
        when(otherProperty.hasName(anyString())).thenReturn(false);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(otherProperty, mock(DeviceType.class));

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts: see expected exception rule
    }

    private ProtocolDialectSearchableProperty getTestInstance() {
        return new ProtocolDialectSearchableProperty(this.propertySpecService, this.protocolPluggableService, this.thesaurus)
                .init(this.domain, this.deviceTypeSearchableProperty);
    }
}