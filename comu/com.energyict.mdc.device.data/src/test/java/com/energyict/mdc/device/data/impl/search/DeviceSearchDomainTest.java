package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.*;

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
    @Mock
    private DeviceService deviceService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private SchedulingService schedulingService;
    @Mock
    private MeteringService meteringService;

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
        Finder scheduleFinder = mock(Finder.class);
        when(scheduleFinder.find()).thenReturn(Collections.emptyList());
        when(this.schedulingService.findAllSchedules()).thenReturn(scheduleFinder);

        mockMRIDPropertySpec();
        mockSerialNumberPropertySpec();
        mockStateNamePropertySpec();
        mockDeviceGroupPropertySpec();
        mockYearOfCertificationPropertySpec();
        mockBatchPropertySpec();
        mockConnectionMethodPropertySpec();
        mockServiceCategoryPropertySpec();
        mockSharedSchedulePropertySpec();
        mockUsagePointPropertySpec();
        mockMasterDevicePropertySpec();
        mockSlaveDevicePropertySpec();
        mockValidationStatusPropertySpec();
        mockEstimationStatusPropertySpec();
        mockSecurityNamePropertySpec();
        mockRegisterProperties();
        mockProtocolDialectPropertySpec();
        mockChannelPropertySpecs();
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
        verify(this.dataModel).getInstance(DeviceGroupSearchableProperty.class);
        verify(this.dataModel).getInstance(BatchSearchableProperty.class);
        verify(this.dataModel).getInstance(YearOfCertificationSearchableProperty.class);
        verify(this.dataModel).getInstance(ConnectionMethodSearchableProperty.class);
        verify(this.dataModel).getInstance(ServiceCategorySearchableProperty.class);
        verify(this.dataModel).getInstance(UsagePointSearchableProperty.class);
        verify(this.dataModel).getInstance(SharedScheduleSearchableProperty.class);
        verify(this.dataModel).getInstance(MasterDeviceSearchableProperty.class);
        verify(this.dataModel).getInstance(SlaveDeviceSearchableProperty.class);
        verify(this.dataModel).getInstance(ValidationStatusSearchableProperty.class);
        verify(this.dataModel).getInstance(EstimationStatusSearchableProperty.class);
        verify(this.dataModel).getInstance(RegisterReadingTypeNameSearchableProperty.class);
        verify(this.dataModel).getInstance(RegisterObisCodeSearchableProperty.class);
        verify(this.dataModel).getInstance(RegisterReadingTypeUnitOfMeasureSearchableProperty.class);
        verify(this.dataModel).getInstance(RegisterReadingTypeTimeOfUseSearchableProperty.class);
        verify(this.dataModel).getInstance(ProtocolDialectSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelReadingTypeNameSearchableProperty.class);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        when(ct1Prop1.getName()).thenReturn("ct1Prop1");
        PropertySpec ct1Prop2 = mock(PropertySpec.class);
        when(ct1Prop2.getName()).thenReturn("ct1Prop2");
        List<PropertySpec> ct1Props = Arrays.asList(ct1Prop1, ct1Prop2);
        when(ct1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionTypePluggableClass pc1 = mock(ConnectionTypePluggableClass.class);
        when(pc1.getName()).thenReturn("PC1");
        when(pc1.getConnectionType()).thenReturn(ct1);
        when(pc1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionType ct2 = mock(ConnectionType.class);
        PropertySpec ct2Prop1 = mock(PropertySpec.class);
        when(ct2Prop1.getName()).thenReturn("ct2Prop1");
        PropertySpec ct2Prop2 = mock(PropertySpec.class);
        when(ct2Prop2.getName()).thenReturn("ct2Prop2");
        PropertySpec ct2Prop3 = mock(PropertySpec.class);
        when(ct2Prop3.getName()).thenReturn("ct2Prop3");
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);

        DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.withValues(deviceConfigurationSearchableProperty, deviceConfiguration);

        ConnectionType ct1 = mock(ConnectionType.class);
        PropertySpec ct1Prop1 = mock(PropertySpec.class);
        when(ct1Prop1.getName()).thenReturn("ct1Prop1");
        PropertySpec ct1Prop2 = mock(PropertySpec.class);
        when(ct1Prop2.getName()).thenReturn("ct1Prop2");
        List<PropertySpec> ct1Props = Arrays.asList(ct1Prop1, ct1Prop2);
        when(ct1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionTypePluggableClass pc1 = mock(ConnectionTypePluggableClass.class);
        when(pc1.getId()).thenReturn(97L);
        when(pc1.getName()).thenReturn("PC1");
        when(pc1.getConnectionType()).thenReturn(ct1);
        when(pc1.getPropertySpecs()).thenReturn(ct1Props);
        ConnectionType ct2 = mock(ConnectionType.class);
        PropertySpec ct2Prop1 = mock(PropertySpec.class);
        when(ct2Prop1.getName()).thenReturn("ct2Prop1");
        PropertySpec ct2Prop2 = mock(PropertySpec.class);
        when(ct2Prop2.getName()).thenReturn("ct2Prop2");
        PropertySpec ct2Prop3 = mock(PropertySpec.class);
        when(ct2Prop3.getName()).thenReturn("ct2Prop3");
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

    private void mockMRIDPropertySpec() {
        PropertySpec mridPropertySpec = mock(PropertySpec.class);
        when(mridPropertySpec.getName()).thenReturn(DeviceFields.MRID.fieldName());
        when(this.propertySpecService.basicPropertySpec(
                eq(DeviceFields.MRID.fieldName()),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(mridPropertySpec);
    }

    private void mockSerialNumberPropertySpec() {
        PropertySpec serialNumber = mock(PropertySpec.class);
        when(serialNumber.getName()).thenReturn(DeviceFields.SERIALNUMBER.fieldName());
        when(this.propertySpecService.basicPropertySpec(
                eq(DeviceFields.SERIALNUMBER.fieldName()),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(serialNumber);
    }

    private void mockStateNamePropertySpec() {
        PropertySpec state = mock(PropertySpec.class);
        when(state.getName()).thenReturn(DeviceFields.SERIALNUMBER.fieldName());
        when(this.propertySpecService.stringPropertySpecWithValues(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                Matchers.<String>anyVararg())).thenReturn(state);
    }

    private void mockDeviceGroupPropertySpec() {
        PropertySpec deviceGroup = mock(PropertySpec.class);
        when(deviceGroup.getName()).thenReturn(DeviceFields.DEVICEGROUP.fieldName());
        when(this.propertySpecService.referencePropertySpec(
                eq(DeviceGroupSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.DEVICE_GROUP),
                anyList())).thenReturn(deviceGroup);
    }

    private void mockYearOfCertificationPropertySpec() {
        PropertySpec year = mock(PropertySpec.class);
        when(year.getName()).thenReturn(DeviceFields.SERIALNUMBER.fieldName());
        when(this.propertySpecService.longPropertySpecWithValues(
                eq(DeviceFields.CERT_YEAR.fieldName()),
                eq(false),
                Matchers.<Long>anyVararg())).thenReturn(year);
    }

    private void mockBatchPropertySpec() {
        PropertySpec batch = mock(PropertySpec.class);
        when(batch.getName()).thenReturn(DeviceFields.BATCH.fieldName());
        when(this.propertySpecService.basicPropertySpec(
                eq(DeviceFields.BATCH.fieldName()),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(batch);
    }

    private void mockConnectionMethodPropertySpec() {
        PropertySpec connectionMethod = mock(PropertySpec.class);
        when(connectionMethod.getName()).thenReturn(ConnectionMethodSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(ConnectionMethodSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.CONNECTION_TYPE),
                anyList())).thenReturn(connectionMethod);
    }

    private void mockSharedSchedulePropertySpec() {
        PropertySpec sharedSchedule = mock(PropertySpec.class);
        when(sharedSchedule.getName()).thenReturn(SharedScheduleSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(SharedScheduleSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.COMSCHEDULE),
                anyList())).thenReturn(sharedSchedule);
    }

    private void mockServiceCategoryPropertySpec() {
        PropertySpec serviceCategory = mock(PropertySpec.class);
        when(serviceCategory.getName()).thenReturn(ServiceCategorySearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(ServiceCategorySearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.SERVICE_CATEGORY),
                anyList())).thenReturn(serviceCategory);
        when(meteringService.getServiceCategory(any())).thenReturn(Optional.empty());
    }

    private void mockUsagePointPropertySpec() {
        PropertySpec usagePoint = mock(PropertySpec.class);
        when(usagePoint.getName()).thenReturn(UsagePointSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(UsagePointSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(usagePoint);
    }

    private void mockMasterDevicePropertySpec() {
        PropertySpec masterDevice = mock(PropertySpec.class);
        when(masterDevice.getName()).thenReturn(MasterDeviceSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(MasterDeviceSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(masterDevice);
    }

    private void mockSlaveDevicePropertySpec() {
        PropertySpec slaveDevice = mock(PropertySpec.class);
        when(slaveDevice.getName()).thenReturn(SlaveDeviceSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(SlaveDeviceSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(slaveDevice);
    }

    private void mockValidationStatusPropertySpec() {
        PropertySpec validationStatus = mock(PropertySpec.class);
        when(validationStatus.getName()).thenReturn(ValidationStatusSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.booleanPropertySpec(
                eq(ValidationStatusSearchableProperty.PROPERTY_NAME),
                eq(ValidationStatusSearchableProperty.PROPERTY_NAME),
                eq(false),
                anyBoolean())).thenReturn(validationStatus);
    }

    private void mockEstimationStatusPropertySpec() {
        PropertySpec validationStatus = mock(PropertySpec.class);
        when(validationStatus.getName()).thenReturn(EstimationStatusSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.booleanPropertySpec(
                eq(EstimationStatusSearchableProperty.PROPERTY_NAME),
                eq(EstimationStatusSearchableProperty.PROPERTY_NAME),
                eq(false),
                anyBoolean())).thenReturn(validationStatus);
    }

    private void mockSecurityNamePropertySpec() {
        PropertySpec securityName = mock(PropertySpec.class);
        when(securityName.getName()).thenReturn(SecurityNameSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(SecurityNameSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(securityName);
    }

    private void mockRegisterProperties() {
        PropertySpec readingTypeName = mock(PropertySpec.class);
        when(readingTypeName.getName()).thenReturn(RegisterReadingTypeNameSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(RegisterReadingTypeNameSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(readingTypeName);
        PropertySpec obisCode = mock(PropertySpec.class);
        when(obisCode.getName()).thenReturn(RegisterObisCodeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(RegisterObisCodeSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(readingTypeName);
        PropertySpec tou = mock(PropertySpec.class);
        when(tou.getName()).thenReturn(RegisterReadingTypeTimeOfUseSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.longPropertySpecWithValues(
                eq(RegisterReadingTypeTimeOfUseSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<Long>anyVararg())).thenReturn(tou);
        PropertySpec unitOfMeasure = mock(PropertySpec.class);
        when(unitOfMeasure.getName()).thenReturn(RegisterReadingTypeUnitOfMeasureSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.stringReferencePropertySpec(
                eq(RegisterReadingTypeUnitOfMeasureSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.anyObject(),
                Matchers.anyVararg())).thenReturn(unitOfMeasure);
    }

    private void mockProtocolDialectPropertySpec(){
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(ProtocolDialectSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.stringReferencePropertySpec(
                eq(ProtocolDialectSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.anyObject(),
                Matchers.anyVararg())).thenReturn(propertySpec);
    }


    private void mockChannelPropertySpecs() {
        PropertySpec readingTypeName = mock(PropertySpec.class);
        when(readingTypeName.getName()).thenReturn(ChannelReadingTypeNameSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(ChannelReadingTypeNameSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(readingTypeName);
//        PropertySpec obisCode = mock(PropertySpec.class);
//        when(obisCode.getName()).thenReturn(RegisterObisCodeSearchableProperty.PROPERTY_NAME);
//        when(this.propertySpecService.basicPropertySpec(
//                eq(RegisterObisCodeSearchableProperty.PROPERTY_NAME),
//                eq(false),
//                Matchers.<StringFactory>anyObject())).thenReturn(readingTypeName);
//        PropertySpec tou = mock(PropertySpec.class);
//        when(tou.getName()).thenReturn(RegisterReadingTypeTimeOfUseSearchableProperty.PROPERTY_NAME);
//        when(this.propertySpecService.longPropertySpecWithValues(
//                eq(RegisterReadingTypeTimeOfUseSearchableProperty.PROPERTY_NAME),
//                eq(false),
//                Matchers.<Long>anyVararg())).thenReturn(tou);
//        PropertySpec unitOfMeasure = mock(PropertySpec.class);
//        when(unitOfMeasure.getName()).thenReturn(RegisterReadingTypeUnitOfMeasureSearchableProperty.PROPERTY_NAME);
//        when(this.propertySpecService.stringReferencePropertySpec(
//                eq(RegisterReadingTypeUnitOfMeasureSearchableProperty.PROPERTY_NAME),
//                eq(false),
//                Matchers.anyObject(),
//                Matchers.anyVararg())).thenReturn(unitOfMeasure);
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
        PropertySpec finiteStatePropertySpec = mock(PropertySpec.class);
        when(finiteStatePropertySpec.getName()).thenReturn(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(StateNameSearchableProperty.VIRTUAL_FIELD_NAME),
                eq(false),
                eq(FactoryIds.FINITE_STATE),
                anyList())).thenReturn(finiteStatePropertySpec);
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.noValues(deviceTypeSearchableProperty);
        DeviceConfiguration deviceConfiguration1 = mock(DeviceConfiguration.class);
        DeviceConfiguration deviceConfiguration2 = mock(DeviceConfiguration.class);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(this.propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.withValues(deviceConfigurationSearchableProperty, deviceConfiguration1, deviceConfiguration2);

        ConnectionType ct1 = mock(ConnectionType.class);
        PropertySpec ct1Prop1 = mock(PropertySpec.class);
        when(ct1Prop1.getName()).thenReturn("ct1Prop1");
        PropertySpec ct1Prop2 = mock(PropertySpec.class);
        when(ct1Prop2.getName()).thenReturn("ct1Prop2");
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
        when(ct2Prop1.getName()).thenReturn("ct2Prop1");
        PropertySpec ct2Prop2 = mock(PropertySpec.class);
        when(ct2Prop2.getName()).thenReturn("ct2Prop2");
        PropertySpec ct2Prop3 = mock(PropertySpec.class);
        when(ct2Prop3.getName()).thenReturn("ct2Prop3");
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
                bind(DeviceService.class).toInstance(deviceService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(MeteringService.class).toInstance(meteringService);
            }
        };
    }
}