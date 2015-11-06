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
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyLong;
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
        verify(this.dataModel).getInstance(ChannelReadingTypeUnitOfMeasureSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelReadingTypeTimeOfUseSearchableProperty.class);
        verify(this.dataModel).getInstance(ChannelIntervalSearchableProperty.class);
        verify(this.dataModel).getInstance(LogbookNameSearchableProperty.class);
        verify(this.dataModel).getInstance(LogbookObisCodeSearchableProperty.class);
        verify(this.dataModel).getInstance(LoadProfileNameSearchableProperty.class);
        verify(this.dataModel).getInstance(LoadProfileLastReadingSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskNameSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskSecuritySettingSearchableProperty.class);
        verify(this.dataModel).getInstance(ComTaskConnectionMethodSearchableProperty.class);
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
        PropertySpec channelInterval = mock(PropertySpec.class);
        when(channelInterval.getName()).thenReturn(ChannelIntervalSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.longPropertySpec( // TODO change to the actual spec builder
                eq(ChannelIntervalSearchableProperty.PROPERTY_NAME),
                eq(false),
                anyLong())).thenReturn(channelInterval);
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
        PropertySpec nameSpec = mock(PropertySpec.class);
        when(nameSpec.getName()).thenReturn(ComTaskNameSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(ComTaskNameSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.COMTASK),
                anyList())).thenReturn(nameSpec);
        PropertySpec securitySetSpec = mock(PropertySpec.class);
        when(securitySetSpec.getName()).thenReturn(ComTaskSecuritySettingSearchableProperty.PROPERTY_NAME);
        when(this.propertySpecService.referencePropertySpec(
                eq(ComTaskSecuritySettingSearchableProperty.PROPERTY_NAME),
                eq(false),
                eq(FactoryIds.SECURITY_SET),
                anyList())).thenReturn(securitySetSpec);
        /** {@link #mockConnectionMethodPropertySpec()} */
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
            }
        };
    }
}