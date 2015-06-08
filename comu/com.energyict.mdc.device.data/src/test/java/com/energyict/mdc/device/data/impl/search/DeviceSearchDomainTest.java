package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.streams.Functions;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceSearchDomain} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-04 (13:08)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceSearchDomainTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Clock clock;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;

    private Injector injector;

    @SuppressWarnings("unchecked")
    @Before
    public void initializeMocks() {
        this.injector = Guice.createInjector(this.getModule());
        when(this.deviceDataModelService.dataModel()).thenReturn(this.dataModel);
        when(this.dataModel.getInstance(any(Class.class))).then(invocationOnMock -> {
            Class zClass = (Class) invocationOnMock.getArguments()[0];
            return injector.getInstance(zClass);
        });
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.emptyList());
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
    }

    @Test
    public void getIdDoesNotReturnNull() {
        assertThat(this.getTestInstance().getId()).isNotNull();
    }

    @Test
    public void getIdDoesNotReturnEmptyString() {
        assertThat(this.getTestInstance().getId()).isNotEmpty();
    }

    @Test
    public void supportsDevice() {
        assertThat(this.getTestInstance().supports(Device.class)).isTrue();
    }

    @Test
    public void doesNotSupportEndDevice() {
        // Testing this to make sure that nobody accidently mistakes the EndDevice for the mdc Device
        assertThat(this.getTestInstance().supports(EndDevice.class)).isFalse();
    }

    @Test
    public void getPropertiesConstructsTheFixedProperties() {
        DeviceSearchDomain searchDomain = this.getTestInstance();

        // Business method
        searchDomain.getProperties();

        // Asserts
        verify(this.dataModel).getInstance(DeviceTypeSearchableProperty.class);
        verify(this.dataModel).getInstance(DeviceConfigurationSearchableProperty.class);
        verify(this.dataModel).getInstance(MasterResourceIdentifierSearchableProperty.class);
        verify(this.dataModel).getInstance(SerialNumberSearchableProperty.class);
        verify(this.dataModel).getInstance(StateNameSearchableProperty.class);
    }

    @Test
    public void getPropertiesAttemptsToConstructConnectionTypeProperties() {
        DeviceSearchDomain searchDomain = this.getTestInstance();

        // Business method
        searchDomain.getProperties();

        // Asserts
        verify(this.protocolPluggableService).findAllConnectionTypePluggableClasses();
    }

    @Test
    public void getPropertiesConstructsConnectionTypeProperties() {
        ConnectionType ct1 = mock(ConnectionType.class);
        PropertySpec ct1Prop1 = mock(PropertySpec.class);
        PropertySpec ct1Prop2 = mock(PropertySpec.class);
        List<PropertySpec> ct1Props = Arrays.asList(ct1Prop1, ct1Prop2);
        when(ct1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionTypePluggableClass pc1 = mock(ConnectionTypePluggableClass.class);
        when(pc1.getName()).thenReturn("PC1");
        when(pc1.getConnectionType()).thenReturn(ct1);
        when(pc1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionType ct2 = mock(ConnectionType.class);
        PropertySpec ct2Prop1 = mock(PropertySpec.class);
        PropertySpec ct2Prop2 = mock(PropertySpec.class);
        PropertySpec ct2Prop3 = mock(PropertySpec.class);
        List<PropertySpec> ct2Props = Arrays.asList(ct2Prop1, ct2Prop2, ct2Prop3);
        when(ct2.getPropertySpecs()).thenReturn(ct2Props);
        ConnectionTypePluggableClass pc2 = mock(ConnectionTypePluggableClass.class);
        when(pc2.getName()).thenReturn("PC2");
        when(pc2.getConnectionType()).thenReturn(ct2);
        when(pc2.getPropertySpecs()).thenReturn(ct2Props);
        when(this.protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Arrays.asList(pc1, pc2));

        DeviceSearchDomain searchDomain = this.getTestInstance();

        // Business method
        List<SearchableProperty> properties = searchDomain.getProperties();

        // Asserts
        List<SearchableProperty> connectionTypeProperties = properties
                .stream()
                .filter(property -> property instanceof ConnectionTypeSearchableProperty)
                .collect(Collectors.toList());
        assertThat(connectionTypeProperties).hasSize(5);
        Set<String> groupNames =
                connectionTypeProperties
                        .stream()
                        .map(SearchableProperty::getGroup)
                        .flatMap(Functions.asStream())
                        .map(SearchablePropertyGroup::getDisplayName)
                        .collect(Collectors.toSet());
        assertThat(groupNames).containsOnly("PC1", "PC2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertiesWithEmptyListOfConstrictions() {
        DeviceSearchDomain searchDomain = this.getTestInstance();

        // Business method
        searchDomain.getPropertiesWithConstrictions(Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertiesWithOnlyDeviceTypeConstrictions() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
        DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction constriction = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(constriction));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertiesWithTwoDeviceTypeConstrictions() {
        DeviceSearchDomain searchDomain = this.getTestInstance();
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
        DeviceTypeSearchableProperty deviceTypeSearchableProperty1 = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction constriction1 = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty1);
        DeviceTypeSearchableProperty deviceTypeSearchableProperty2 = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction constriction2 = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty2);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(constriction1, constriction2));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertiesWithOnlyDeviceConfigurationConstrictions() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                DeviceTypeSearchableProperty.PROPERTY_NAME,
                false,
                FactoryIds.DEVICE_TYPE)).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
        DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction constriction = SearchablePropertyConstriction.noValues(deviceConfigurationSearchableProperty);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(constriction));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertiesWithTwoDeviceConfigurationConstrictions() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                DeviceTypeSearchableProperty.PROPERTY_NAME,
                false,
                FactoryIds.DEVICE_TYPE)).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
        DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty1 = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction constriction1 = SearchablePropertyConstriction.noValues(deviceConfigurationSearchableProperty1);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty2 = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction constriction2 = SearchablePropertyConstriction.noValues(deviceConfigurationSearchableProperty2);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(constriction1, constriction2));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertiesWithTooManyConstrictions() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
        DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.noValues(deviceConfigurationSearchableProperty);
        SearchableProperty otherProperty = mock(SearchableProperty.class);
        when(otherProperty.getName()).thenReturn("getPropertiesWithTooManyConstrictions");
        SearchablePropertyConstriction otherPropertyConstriction = SearchablePropertyConstriction.noValues(otherProperty);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction, otherPropertyConstriction));

        // Asserts: see expected exception rule
    }

    @Test
    public void getPropertiesWithDeviceTypeConstrictionsConstructsTheFixedProperties() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
                DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(deviceTypeSearchableProperty, deviceType);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.noValues(deviceConfigurationSearchableProperty);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction));

        // Asserts
        verify(this.dataModel).getInstance(DeviceTypeSearchableProperty.class);
        verify(this.dataModel).getInstance(DeviceConfigurationSearchableProperty.class);
        verify(this.dataModel).getInstance(MasterResourceIdentifierSearchableProperty.class);
        verify(this.dataModel).getInstance(SerialNumberSearchableProperty.class);
        verify(this.dataModel).getInstance(StateNameSearchableProperty.class);
    }

    @Test
    public void getPropertiesWithDeviceTypeConstrictionsAttemptsToCreateConnectionTypeProperties() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
                DeviceSearchDomain searchDomain = this.getTestInstance();
        ConnectionType connectionType = mock(ConnectionType.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(Arrays.asList(connectionType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(deviceTypeSearchableProperty, deviceType);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.noValues(deviceConfigurationSearchableProperty);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction));

        // Asserts
        verify(this.protocolPluggableService).findConnectionTypePluggableClassByClassName(anyString());
    }

    @Test
    public void getPropertiesWithDeviceTypeConstrictionsCreatesConnectionTypeProperties() {
        ConnectionType ct1 = mock(ConnectionType.class);
        PropertySpec ct1Prop1 = mock(PropertySpec.class);
        PropertySpec ct1Prop2 = mock(PropertySpec.class);
        List<PropertySpec> ct1Props = Arrays.asList(ct1Prop1, ct1Prop2);
        when(ct1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionTypePluggableClass pc1 = mock(ConnectionTypePluggableClass.class);
        when(pc1.getName()).thenReturn("PC1");
        when(pc1.getConnectionType()).thenReturn(ct1);
        when(pc1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionType ct2 = mock(ConnectionType.class);
        PropertySpec ct2Prop1 = mock(PropertySpec.class);
        PropertySpec ct2Prop2 = mock(PropertySpec.class);
        PropertySpec ct2Prop3 = mock(PropertySpec.class);
        List<PropertySpec> ct2Props = Arrays.asList(ct2Prop1, ct2Prop2, ct2Prop3);
        when(ct2.getPropertySpecs()).thenReturn(ct2Props);
        ConnectionTypePluggableClass pc2 = mock(ConnectionTypePluggableClass.class);
        when(pc2.getName()).thenReturn("PC2");
        when(pc2.getConnectionType()).thenReturn(ct2);
        when(pc2.getPropertySpecs()).thenReturn(ct2Props);
        when(this.protocolPluggableService.findConnectionTypePluggableClassByClassName(anyString())).thenReturn(Arrays.asList(pc1, pc2));

        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
                DeviceSearchDomain searchDomain = this.getTestInstance();
        ConnectionType connectionType = mock(ConnectionType.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(Arrays.asList(connectionType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(deviceTypeSearchableProperty, deviceType);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.noValues(deviceConfigurationSearchableProperty);

        // Business method
        List<SearchableProperty> properties = searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction));

        // Asserts
        List<SearchableProperty> connectionTypeProperties = properties
                .stream()
                .filter(property -> property instanceof ConnectionTypeSearchableProperty)
                .collect(Collectors.toList());
        assertThat(connectionTypeProperties).hasSize(5);
        Set<String> groupNames =
                connectionTypeProperties
                        .stream()
                        .map(SearchableProperty::getGroup)
                        .flatMap(Functions.asStream())
                        .map(SearchablePropertyGroup::getDisplayName)
                        .collect(Collectors.toSet());
        assertThat(groupNames).containsOnly("PC1", "PC2");
    }

    @Test
    public void getPropertiesWithDeviceConfigurationConstrictionsConstructsTheFixedProperties() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
                DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Collections.emptyList());
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.withValues(deviceConfigurationSearchableProperty, deviceConfiguration);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction));

        // Asserts
        verify(this.dataModel).getInstance(DeviceTypeSearchableProperty.class);
        verify(this.dataModel).getInstance(DeviceConfigurationSearchableProperty.class);
        verify(this.dataModel).getInstance(MasterResourceIdentifierSearchableProperty.class);
        verify(this.dataModel).getInstance(SerialNumberSearchableProperty.class);
        verify(this.dataModel).getInstance(StateNameSearchableProperty.class);
    }

    @Test
    public void getPropertiesWithDeviceConfigurationConstrictionsAttemptsToCreateConnectionTypeProperties() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
                DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Collections.emptyList());
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.withValues(deviceConfigurationSearchableProperty, deviceConfiguration);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction));

        // Asserts
        verify(deviceConfiguration).getPartialConnectionTasks();
    }

    @Test
    public void getPropertiesWithDeviceConfigurationConstrictionsCreatesConnectionTypeProperties() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
                DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.withValues(deviceConfigurationSearchableProperty, deviceConfiguration);

        ConnectionType ct1 = mock(ConnectionType.class);
        PropertySpec ct1Prop1 = mock(PropertySpec.class);
        PropertySpec ct1Prop2 = mock(PropertySpec.class);
        List<PropertySpec> ct1Props = Arrays.asList(ct1Prop1, ct1Prop2);
        when(ct1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionTypePluggableClass pc1 = mock(ConnectionTypePluggableClass.class);
        when(pc1.getId()).thenReturn(97L);
        when(pc1.getName()).thenReturn("PC1");
        when(pc1.getConnectionType()).thenReturn(ct1);
        when(pc1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionType ct2 = mock(ConnectionType.class);
        PropertySpec ct2Prop1 = mock(PropertySpec.class);
        PropertySpec ct2Prop2 = mock(PropertySpec.class);
        PropertySpec ct2Prop3 = mock(PropertySpec.class);
        List<PropertySpec> ct2Props = Arrays.asList(ct2Prop1, ct2Prop2, ct2Prop3);
        when(ct2.getPropertySpecs()).thenReturn(ct2Props);
        ConnectionTypePluggableClass pc2 = mock(ConnectionTypePluggableClass.class);
        when(pc2.getId()).thenReturn(101L);
        when(pc2.getName()).thenReturn("PC2");
        when(pc2.getConnectionType()).thenReturn(ct2);
        when(pc2.getPropertySpecs()).thenReturn(ct2Props);
        PartialConnectionTask pct1 = mock(PartialConnectionTask.class);
        when(pct1.getConnectionType()).thenReturn(ct1);
        when(pct1.getPluggableClass()).thenReturn(pc1);
        PartialConnectionTask pct2 = mock(PartialConnectionTask.class);
        when(pct2.getConnectionType()).thenReturn(ct2);
        when(pct2.getPluggableClass()).thenReturn(pc2);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(pct1, pct2));

        // Business method
        List<SearchableProperty> properties = searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction));

        // Asserts
        List<SearchableProperty> connectionTypeProperties = properties
                .stream()
                .filter(property -> property instanceof ConnectionTypeSearchableProperty)
                .collect(Collectors.toList());
        assertThat(connectionTypeProperties).hasSize(5);
        Set<String> groupNames =
                connectionTypeProperties
                        .stream()
                        .map(SearchableProperty::getGroup)
                        .flatMap(Functions.asStream())
                        .map(SearchablePropertyGroup::getDisplayName)
                        .collect(Collectors.toSet());
        assertThat(groupNames).containsOnly("PC1", "PC2");
    }

    @Test
    public void getPropertiesWithDeviceConfigurationConstrictionsWithTheSamePluggableClassDoesNotCreateDuplicateConnectionTypeProperties() {
        PropertySpec deviceTypePropertySpec = mock(PropertySpec.class);
        when(deviceTypePropertySpec.getName()).thenReturn(DeviceTypeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceTypeSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_TYPE),
                anyList())).thenReturn(deviceTypePropertySpec);
        PropertySpec deviceConfigurationPropertySpec = mock(PropertySpec.class);
        when(deviceConfigurationPropertySpec.getName()).thenReturn(DeviceConfigurationSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceConfigurationSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_CONFIGURATION),
                anyList())).thenReturn(deviceConfigurationPropertySpec);
                DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty);
        DeviceConfiguration deviceConfiguration1 = mock(DeviceConfiguration.class);
        DeviceConfiguration deviceConfiguration2 = mock(DeviceConfiguration.class);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.withValues(deviceConfigurationSearchableProperty, deviceConfiguration1, deviceConfiguration2);

        ConnectionType ct1 = mock(ConnectionType.class);
        PropertySpec ct1Prop1 = mock(PropertySpec.class);
        PropertySpec ct1Prop2 = mock(PropertySpec.class);
        List<PropertySpec> ct1Props = Arrays.asList(ct1Prop1, ct1Prop2);
        when(ct1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getId()).thenReturn(97L);
        when(connectionTypePluggableClass.getName()).thenReturn("PC1");
        when(connectionTypePluggableClass.getConnectionType()).thenReturn(ct1);
        when(connectionTypePluggableClass.getPropertySpecs()).thenReturn(ct1Props);
        PartialConnectionTask pct1 = mock(PartialConnectionTask.class);
        when(pct1.getConnectionType()).thenReturn(ct1);
        when(pct1.getPluggableClass()).thenReturn(connectionTypePluggableClass);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Arrays.asList(pct1));

        ConnectionType ct2 = mock(ConnectionType.class);
        PropertySpec ct2Prop1 = mock(PropertySpec.class);
        PropertySpec ct2Prop2 = mock(PropertySpec.class);
        PropertySpec ct2Prop3 = mock(PropertySpec.class);
        List<PropertySpec> ct2Props = Arrays.asList(ct2Prop1, ct2Prop2, ct2Prop3);
        when(ct2.getPropertySpecs()).thenReturn(ct2Props);
        PartialConnectionTask pct2 = mock(PartialConnectionTask.class);
        when(pct2.getConnectionType()).thenReturn(ct2);
        when(pct2.getPluggableClass()).thenReturn(connectionTypePluggableClass);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Arrays.asList(pct2));

        // Business method
        List<SearchableProperty> properties = searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction));

        // Asserts
        List<SearchableProperty> connectionTypeProperties = properties
                .stream()
                .filter(property -> property instanceof ConnectionTypeSearchableProperty)
                .collect(Collectors.toList());
        assertThat(connectionTypeProperties).hasSize(2);
        Set<String> groupNames =
                connectionTypeProperties
                        .stream()
                        .map(SearchableProperty::getGroup)
                        .flatMap(Functions.asStream())
                        .map(SearchablePropertyGroup::getDisplayName)
                        .collect(Collectors.toSet());
        assertThat(groupNames).containsOnly("PC1");
    }

    private DeviceSearchDomain getTestInstance() {
        return new DeviceSearchDomain(this.deviceDataModelService, this.clock, this.protocolPluggableService);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(Clock.class).toInstance(clock);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(DataModel.class).toInstance(dataModel);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(com.elster.jupiter.properties.PropertySpecService.class).toInstance(propertySpecService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            }
        };
    }

}