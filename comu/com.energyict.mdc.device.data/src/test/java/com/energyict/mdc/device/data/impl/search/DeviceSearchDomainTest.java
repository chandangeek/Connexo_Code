/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
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
    private DataVaultService dataVaultService;
    @Mock
    private OrmService ormService;
    @Mock
    private TimeService timeService;
    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Clock clock;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
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

    @Before
    public void initializeThesaurus() {
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(this.messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(this.messageFormat);
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
    }

    @SuppressWarnings("unchecked")
    @Before
    public void initializeMocks() {
        this.injector = Guice.createInjector(this.getModule());
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
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

        mockServiceCategories();
        mockComTasks();
        mockConnections();
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
        assertThat(this.getTestInstance().getDomainClass().isAssignableFrom(Device.class)).isTrue();
    }

    @Test
    public void doesNotSupportEndDevice() {
        // Testing this to make sure that nobody accidently mistakes the EndDevice for the mdc Device
        assertThat(this.getTestInstance().getDomainClass().isAssignableFrom(EndDevice.class)).isFalse();
    }

    @Test
    public void getPropertiesConstructsTheFixedProperties() {
        DeviceSearchDomain searchDomain = this.getTestInstance();

        // Business method
        searchDomain.getProperties();

        // Asserts
        verify(this.dataModel).getInstance(DeviceTypeSearchableProperty.class);
        verify(this.dataModel).getInstance(DeviceConfigurationSearchableProperty.class);
        verify(this.dataModel).getInstance(NameSearchableProperty.class);
        verify(this.dataModel).getInstance(SerialNumberSearchableProperty.class);
        verify(this.dataModel).getInstance(StateNameSearchableProperty.class);
        verify(this.dataModel).getInstance(DeviceGroupSearchableProperty.class);
        verify(this.dataModel).getInstance(BatchSearchableProperty.class);
        verify(this.dataModel).getInstance(YearOfCertificationSearchableProperty.class);
        verify(this.dataModel).getInstance(ConnectionMethodSearchableProperty.class);
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
        PropertySpecService propertySpecService = this.injector.getInstance(PropertySpecService.class);
        DeviceSearchDomain searchDomain = this.getTestInstance();
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(deviceTypeSearchableProperty, deviceType);
        DeviceConfigurationSearchableProperty deviceConfigurationSearchableProperty = new DeviceConfigurationSearchableProperty(propertySpecService, this.thesaurus);
        SearchablePropertyConstriction deviceConfigurationConstriction = SearchablePropertyConstriction.noValues(deviceConfigurationSearchableProperty);

        // Business method
        searchDomain.getPropertiesWithConstrictions(Arrays.asList(deviceTypeConstriction, deviceConfigurationConstriction));

        // Asserts
        verify(this.dataModel).getInstance(DeviceTypeSearchableProperty.class);
        verify(this.dataModel).getInstance(DeviceConfigurationSearchableProperty.class);
        verify(this.dataModel).getInstance(NameSearchableProperty.class);
        verify(this.dataModel).getInstance(SerialNumberSearchableProperty.class);
        verify(this.dataModel).getInstance(StateNameSearchableProperty.class);
    }

    private void mockServiceCategories() {
        when(meteringService.getServiceCategory(any())).thenReturn(Optional.empty());
    }

    private void mockComTasks() {
        Finder<ComTask> taskFinder = mock(Finder.class);
        when(taskService.findAllComTasks()).thenReturn(taskFinder);
        when(taskFinder.paged(anyInt(), anyInt())).thenReturn(taskFinder);
        when(taskFinder.find()).thenReturn(Collections.emptyList());
    }

    private void mockConnections() {
        InboundComPortPool mock = mock(InboundComPortPool.class);
        List<ComPortPool> comPortPools = new ArrayList<>();
        comPortPools.add(mock);
        when(engineConfigurationService.findAllComPortPools()).thenReturn(comPortPools);
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
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(OrmService.class).toInstance(ormService);
                bind(TimeService.class).toInstance(timeService);
                bind(BeanService.class).toInstance(new DefaultBeanService());
                bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(TaskService.class).toInstance(taskService);
                bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
                bind(MasterDataService.class).toInstance(masterDataService);
                bind(com.elster.jupiter.properties.PropertySpecService.class).to(com.elster.jupiter.properties.impl.PropertySpecServiceImpl.class);
                bind(PropertySpecService.class).to(PropertySpecServiceImpl.class);
            }
        };
    }
}