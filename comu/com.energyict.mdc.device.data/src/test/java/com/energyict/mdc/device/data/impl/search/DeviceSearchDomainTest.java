package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.anyBoolean;
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
    @Mock
    private DeviceService deviceService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private SchedulingService schedulingService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private TaskService taskService;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private MasterDataService masterDataService;

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
        mockLogbookPropertySpecs();
        mockLoadProfilePropertySpecs();
        mockComTasks();
        mockConnections();
        mockTransitions();
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
        verify(this.dataModel).getInstance(RegisterLastReadingSearchableProperty.class);
        verify(this.dataModel).getInstance(ProtocolDialectSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelReadingTypeNameSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelReadingTypeUnitOfMeasureSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelReadingTypeTimeOfUseSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelLastReadingSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelLastValueSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelIntervalSearchableProperty.class);
        verify(this.dataModel).getInstance(LogbookNameSearchableProperty.class);
        verify(this.dataModel).getInstance(LogbookObisCodeSearchableProperty.class);
        verify(this.dataModel).getInstance(LogbookLastReadingSearchableProperty.class);
        verify(this.dataModel).getInstance(LogbookLastEventTimestampSearchableProperty.class);
        verify(this.dataModel).getInstance(LoadProfileNameSearchableProperty.class);
        verify(this.dataModel).getInstance(LoadProfileLastReadingSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskNameSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskSecuritySettingSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskConnectionMethodSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskUrgencySearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskNextCommunicationSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskLastCommunicationSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskStatusSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskScheduleTypeSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskScheduleNameSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskPlannedDateSearchableProperty.class);
        verify(this.dataModel).getInstance(ConnectionNameSearchableProperty.class);
        verify(this.dataModel).getInstance(ConnectionDirectionSearchableProperty.class);
        verify(this.dataModel).getInstance(ConnectionCommunicationPortPoolSearchableProperty.class);
        verify(this.dataModel).getInstance(ConnectionSimultaneousSearchableProperty.class);
        verify(this.dataModel).getInstance(ConnectionStatusSearchableProperty.class);
        verify(this.dataModel).getInstance(TransitionShipmentDateSearchableProperty.class);
        verify(this.dataModel).getInstance(TransitionInstallationDateSearchableProperty.class);
        verify(this.dataModel).getInstance(TransitionDeactivationDateSearchableProperty.class);
        verify(this.dataModel).getInstance(TransitionDecommissioningDateSearchableProperty.class);
    }

    public void getPropertiesWithEmptyListOfConstrictions() {
        DeviceSearchDomain searchDomain = this.getTestInstance();

        // Business method
        List<SearchableProperty> propertiesWithConstrictions = searchDomain.getPropertiesWithConstrictions(Collections.emptyList());
        List<SearchableProperty> properties = searchDomain.getProperties();

        // Asserts
        assertThat(properties.size()).isEqualTo(propertiesWithConstrictions.size());
    }

    @Test
    public void getPropertiesWithDeviceTypeConstrictionsConstructsTheFixedProperties() {
        this.mockReferencePropertySpec(DeviceTypeSearchableProperty.PROPERTY_NAME, DeviceType.class);
        this.mockReferencePropertySpec(DeviceConfigurationSearchableProperty.PROPERTY_NAME, DeviceConfiguration.class);
        this.mockReferencePropertySpec(StateNameSearchableProperty.VIRTUAL_FIELD_NAME, State.class);

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
        this.mockStringPropertySpec(StateNameSearchableProperty.VIRTUAL_FIELD_NAME);
    }

    private void mockDeviceGroupPropertySpec() {
        this.mockReferencePropertySpec(DeviceGroupSearchableProperty.PROPERTY_NAME, EndDeviceGroup.class);
    }

    private void mockYearOfCertificationPropertySpec() {
        this.mockLongPropertySpec(DeviceFields.CERT_YEAR.fieldName());
    }

    private void mockBatchPropertySpec() {
        this.mockStringPropertySpec(DeviceFields.BATCH.fieldName());
    }

    private void mockConnectionMethodPropertySpec() {
        this.mockReferencePropertySpec(ConnectionMethodSearchableProperty.PROPERTY_NAME, ConnectionTask.class);
    }

    private void mockSharedSchedulePropertySpec() {
        this.mockReferencePropertySpec(SharedScheduleSearchableProperty.PROPERTY_NAME, ComSchedule.class);
    }

    private void mockServiceCategoryPropertySpec() {
        this.mockReferencePropertySpec(ServiceCategorySearchableProperty.PROPERTY_NAME, ServiceCategory.class);
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
                Matchers.<StringFactory>anyObject())).thenReturn(obisCode);
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
        PropertySpec lastReadingSpec = mock(PropertySpec.class);
        when(lastReadingSpec.getName()).thenReturn(RegisterLastReadingSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(RegisterLastReadingSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(lastReadingSpec);
    }

    private void mockProtocolDialectPropertySpec() {
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
        PropertySpec obisCode = mock(PropertySpec.class);
        when(obisCode.getName()).thenReturn(ChannelObisCodeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(ChannelObisCodeSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(obisCode);
        PropertySpec tou = mock(PropertySpec.class);
        when(tou.getName()).thenReturn(ChannelReadingTypeTimeOfUseSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.longPropertySpecWithValues(
                eq(ChannelReadingTypeTimeOfUseSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<Long>anyVararg())).thenReturn(tou);
        PropertySpec unitOfMeasure = mock(PropertySpec.class);
        when(unitOfMeasure.getName()).thenReturn(ChannelReadingTypeUnitOfMeasureSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.stringReferencePropertySpec(
                eq(ChannelReadingTypeUnitOfMeasureSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.anyObject(),
                Matchers.anyVararg())).thenReturn(unitOfMeasure);
        PropertySpec channelIntervalSpec = mock(PropertySpec.class);
        when(channelIntervalSpec.getName()).thenReturn(ChannelIntervalSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.stringReferencePropertySpec(
                eq(ChannelIntervalSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.anyObject(),
                Matchers.anyVararg())).thenReturn(channelIntervalSpec);
        PropertySpec lastDateProperty = mock(PropertySpec.class);
        when(lastDateProperty.getName()).thenReturn("device.channel.last.");
        when(this.propertySpecService.basicPropertySpec(
                startsWith("device.channel.last."),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(lastDateProperty);
    }

    private void mockLogbookPropertySpecs() {
        PropertySpec nameSpec = mock(PropertySpec.class);
        when(nameSpec.getName()).thenReturn(LogbookNameSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(LogbookNameSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(nameSpec);
        PropertySpec obisCodeSpec = mock(PropertySpec.class);
        when(obisCodeSpec.getName()).thenReturn(LogbookObisCodeSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(LogbookObisCodeSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(obisCodeSpec);
        PropertySpec lastReadingSpec = mock(PropertySpec.class);
        when(lastReadingSpec.getName()).thenReturn(LogbookLastReadingSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(LogbookLastReadingSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(lastReadingSpec);
        PropertySpec lastEventSpec = mock(PropertySpec.class);
        when(lastEventSpec.getName()).thenReturn(LogbookLastEventTimestampSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(LogbookLastEventTimestampSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(lastEventSpec);
    }


    private void mockLoadProfilePropertySpecs() {
        PropertySpec nameSpec = mock(PropertySpec.class);
        when(nameSpec.getName()).thenReturn(LoadProfileNameSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(LoadProfileNameSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(nameSpec);
        PropertySpec lastReadingSpec = mock(PropertySpec.class);
        when(lastReadingSpec.getName()).thenReturn(LoadProfileLastReadingSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(LoadProfileLastReadingSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(lastReadingSpec);
    }

    private void mockComTasks() {
        Finder taskFinder = mock(Finder.class);
        when(taskService.findAllComTasks()).thenReturn(taskFinder);
        when(taskFinder.paged(anyInt(), anyInt())).thenReturn(taskFinder);
        when(taskFinder.find()).thenReturn(Collections.emptyList());
        this.mockStringPropertySpec(ComTaskNameSearchableProperty.PROPERTY_NAME);
        this.mockReferencePropertySpec(ComTaskNameSearchableProperty.PROPERTY_NAME, ComTask.class);
        this.mockReferencePropertySpec(ComTaskSecuritySettingSearchableProperty.PROPERTY_NAME, SecurityPropertySet.class);
        this.mockStringPropertySpec(ComTaskStatusSearchableProperty.PROPERTY_NAME);
        this.mockLongPropertySpec(ComTaskUrgencySearchableProperty.PROPERTY_NAME);
        this.mockReferencePropertySpec(ComTaskScheduleNameSearchableProperty.PROPERTY_NAME, ComSchedule.class);
        this.mockStringPropertySpec(ComTaskScheduleTypeSearchableProperty.PROPERTY_NAME);
    }

    private void mockConnections() {
        PropertySpec nameSpec = mock(PropertySpec.class);
        when(nameSpec.getName()).thenReturn(ConnectionNameSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.basicPropertySpec(
                eq(ConnectionNameSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.<StringFactory>anyObject())).thenReturn(nameSpec);

        this.mockStringPropertySpec(ConnectionDirectionSearchableProperty.PROPERTY_NAME);

        InboundComPortPool mock = mock(InboundComPortPool.class);
        List<ComPortPool> comPortPools = new ArrayList<>();
        comPortPools.add(mock);
        when(engineConfigurationService.findAllComPortPools()).thenReturn(comPortPools);
        this.mockReferencePropertySpec(ConnectionCommunicationPortPoolSearchableProperty.PROPERTY_NAME, ConnectionTask.class);
        PropertySpec simultaneousSpec = mock(PropertySpec.class);
        when(simultaneousSpec.getName()).thenReturn(ConnectionSimultaneousSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.booleanPropertySpec(
                eq(ConnectionSimultaneousSearchableProperty.PROPERTY_NAME),
                eq(ConnectionSimultaneousSearchableProperty.PROPERTY_NAME),
                eq(false),
                anyBoolean())).thenReturn(simultaneousSpec);
        PropertySpec statusSpec = mock(PropertySpec.class);
        when(statusSpec.getName()).thenReturn(ConnectionStatusSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.stringReferencePropertySpec(
                eq(ConnectionStatusSearchableProperty.PROPERTY_NAME),
                eq(false),
                Matchers.anyObject(),
                Matchers.anyVararg())).thenReturn(statusSpec);
    }

    private void mockTransitions(){
            PropertySpec anyDateProperty = mock(PropertySpec.class);
            when(anyDateProperty.getName()).thenReturn("device.transition.");
            when(this.propertySpecService.basicPropertySpec(
                    startsWith("device.transition."),
                    eq(false),
                    Matchers.<StringFactory>anyObject())).thenReturn(anyDateProperty);
    }

    private void mockReferencePropertySpec(String name, Class referencedClass) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(name, any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(this.propertySpecService.referenceSpec(referencedClass)).thenReturn(nlsOptions);
    }

    private void mockStringPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(name, any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(this.propertySpecService.stringSpec()).thenReturn(nlsOptions);
    }

    private void mockLongPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(name, any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(this.propertySpecService.longSpec()).thenReturn(nlsOptions);
    }

    private void mockBooleanPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(name, any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(this.propertySpecService.booleanSpec()).thenReturn(nlsOptions);
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
                bind(TaskService.class).toInstance(taskService);
                bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
                bind(MasterDataService.class).toInstance(masterDataService);
            }
        };
    }
}