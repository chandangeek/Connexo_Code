/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignCustomPropertySet;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignDomainExtension;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignServiceImpl;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseItemDomainExtension;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseItemPersistenceSupport;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseSendHelper;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;

import org.osgi.framework.BundleContext;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ToUCampaignServiceTest {

    private static ThreadPrincipalService threadPrincipalService = mock(ThreadPrincipalService.class);
    private static TransactionService transactionService = mock(TransactionService.class);
    private static NlsService nlsService = mock(NlsService.class);
    private static UpgradeService upgradeService = mock(UpgradeService.class);
    private static UserService userService = mock(UserService.class);
    private static BatchService batchService = mock(BatchService.class);
    private static PropertySpecService propertySpecService = mock(PropertySpecService.class);
    private static CustomPropertySetService customPropertySetService = mock(CustomPropertySetService.class);
    private static MeteringGroupsService meteringGroupsService = mock(MeteringGroupsService.class);
    private static OrmService ormService = mock(OrmService.class);
    private static Clock clock = mock(Clock.class);
    private static DeviceService deviceService = mock(DeviceService.class);
    private static CalendarService calendarService = mock(CalendarService.class);
    private static DeviceConfigurationService deviceConfigurationService = mock(DeviceConfigurationService.class);
    private static DeviceMessageSpecificationService deviceMessageSpecificationService = mock(DeviceMessageSpecificationService.class);
    private static EventService eventService = mock(EventService.class);
    private DataModel dataModel = mock(DataModel.class);
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static ServiceCallService serviceCallService = mock(ServiceCallService.class);
    private RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
    private static TimeOfUseCampaignServiceImpl timeOfUseCampaignService;
    private TimeOfUseSendHelper timeOfUseSendHelper;
    private BundleContext bundleContext = mock(BundleContext.class);
    private Calendar calendar = mock(Calendar.class);
    private ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
    private ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);

    @Before
    public void setUp() {
        when(nlsService.getThesaurus(anyString(), any())).thenReturn(thesaurus);
        timeOfUseSendHelper = new TimeOfUseSendHelper(thesaurus, deviceConfigurationService, deviceMessageSpecificationService, timeOfUseCampaignService);
        when(upgradeService.newNonOrmDataModel()).thenReturn(dataModel);
        when(customPropertySetService.findActiveCustomPropertySet(TimeOfUseCampaignCustomPropertySet.CUSTOM_PROPERTY_SET_ID)).thenReturn(Optional.ofNullable(registeredCustomPropertySet));
        timeOfUseCampaignService = new TimeOfUseCampaignServiceImpl(threadPrincipalService, transactionService,
                nlsService, upgradeService, userService, batchService, propertySpecService,
                serviceCallService, customPropertySetService, meteringGroupsService, ormService, clock, deviceService,
                calendarService, deviceConfigurationService, deviceMessageSpecificationService, eventService, bundleContext);
        timeOfUseCampaignService = new TimeOfUseCampaignServiceImpl();
        timeOfUseCampaignService.setOrmService(ormService);
        timeOfUseCampaignService.setNlsService(nlsService);
        timeOfUseCampaignService.setServiceCallService(serviceCallService);
        timeOfUseCampaignService.setUpgradeService(upgradeService);
        timeOfUseSendHelper = new TimeOfUseSendHelper(thesaurus, deviceConfigurationService, deviceMessageSpecificationService, timeOfUseCampaignService);
        try {
            Field field = timeOfUseCampaignService.getClass().getDeclaredField("dataModel");
            field.setAccessible(true);
            field.set(timeOfUseCampaignService, dataModel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(dataModel.getInstance(TimeOfUseSendHelper.class)).thenReturn(timeOfUseSendHelper);
        when(dataModel.getInstance(ServiceCallService.class)).thenReturn(serviceCallService);
        when(dataModel.getInstance(Thesaurus.class)).thenReturn(thesaurus);
        when(calendar.getId()).thenReturn(1L);
    }

    @Test
    public void cancelDevice() {
        DataModel dataModel = mock(DataModel.class);
        when(ormService.getDataModel(TimeOfUseItemPersistenceSupport.COMPONENT_NAME)).thenReturn(java.util.Optional.of(dataModel));
        TimeOfUseItemDomainExtension timeOfUseItem = new TimeOfUseItemDomainExtension(timeOfUseCampaignService);
        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(timeOfUseItem), QueryStream.class);
        when(dataModel.stream(TimeOfUseItemDomainExtension.class)).thenReturn(queryStream);
        ServiceCall serviceCall = mock(ServiceCall.class);
        Device device = mock(Device.class);
        when(serviceCallService.getServiceCall(anyLong())).thenReturn(Optional.of(serviceCall));
        when(serviceCall.getExtension(TimeOfUseItemDomainExtension.class)).thenReturn(Optional.of(timeOfUseItem));
        when(serviceCall.canTransitionTo(DefaultState.CANCELLED)).thenReturn(true);
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        customPropertySetValues.setProperty("device", device);
        customPropertySetValues.setProperty("parentServiceCallId", 11L);
        customPropertySetValues.setProperty("deviceMessage", null);
        timeOfUseItem.copyFrom(serviceCall, customPropertySetValues);
        when(device.getId()).thenReturn(1L);
        EndDevice endDevice = mock(EndDevice.class);
        when(endDevice.getId()).thenReturn(1L);
        EndDeviceGroup group = mock(EndDeviceGroup.class);
        when(group.getMembers((Instant) any())).thenReturn(Collections.singletonList(endDevice));
        timeOfUseItem.cancel();
        verify(serviceCall).requestTransition(DefaultState.CANCELLED);
    }

    @Test
    public void retryDevice() {
        DataModel dataModel = mock(DataModel.class);
        when(ormService.getDataModel(TimeOfUseItemPersistenceSupport.COMPONENT_NAME)).thenReturn(java.util.Optional.of(dataModel));
        TimeOfUseItemDomainExtension timeOfUseItem = new TimeOfUseItemDomainExtension(timeOfUseCampaignService);
        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(timeOfUseItem), QueryStream.class);
        when(dataModel.stream(TimeOfUseItemDomainExtension.class)).thenReturn(queryStream);
        ServiceCall serviceCall = mock(ServiceCall.class);
        ServiceCall parent = mock(ServiceCall.class);
        Device device = createDevice();
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        List<ComTaskEnablement> comTaskEnablementList = new ArrayList<>();
        ComTaskEnablement comTaskEnablement1 = createCalendarsComTaskEnablement();
        ComTaskEnablement comTaskEnablement2 = createVerificationComTaskEnablement();
        comTaskEnablementList.add(comTaskEnablement1);
        comTaskEnablementList.add(comTaskEnablement2);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(comTaskEnablementList);
        TimeOfUseCampaignDomainExtension timeOfUseCampaignDomainExtension = createCampaignExtension();
        TimeOfUseItemDomainExtension timeOfUseItemDomainExtension = mock(TimeOfUseItemDomainExtension.class);
        when(serviceCallService.getServiceCall(anyLong())).thenReturn(Optional.of(serviceCall));
        when(serviceCall.getExtension(TimeOfUseItemDomainExtension.class)).thenReturn(Optional.ofNullable(timeOfUseItemDomainExtension));
        when(serviceCall.canTransitionTo(DefaultState.PENDING)).thenReturn(true);
        when(timeOfUseItemDomainExtension.getDevice()).thenReturn(device);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parent));
        when(parent.getExtension(any())).thenReturn(Optional.of(timeOfUseCampaignDomainExtension));
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(3600));

        TimeOfUseOptions timeOfUseOptions = mock(TimeOfUseOptions.class);

        when(deviceConfigurationService.findTimeOfUseOptions(any())).thenReturn(Optional.ofNullable(timeOfUseOptions));
        when(timeOfUseOptions.getOptions()).thenReturn(Collections.singleton(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME));
        EndDevice endDevice = mock(EndDevice.class);
        when(endDevice.getId()).thenReturn(1L);
        EndDeviceGroup group = mock(EndDeviceGroup.class);
        when(group.getMembers((Instant) any())).thenReturn(Collections.singletonList(endDevice));
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        customPropertySetValues.setProperty("device", device);
        customPropertySetValues.setProperty("parentServiceCallId", 11L);
        customPropertySetValues.setProperty("deviceMessage", null);
        timeOfUseItem.copyFrom(serviceCall, customPropertySetValues);

        timeOfUseItem.retry();
        verify(serviceCall).requestTransition(DefaultState.PENDING);
    }

    private ComTaskEnablement createCalendarsComTaskEnablement() {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        ComTask comTask = mock(ComTask.class);
        MessagesTask messagesTask = mock(MessagesTask.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(messagesTask));
        when(messagesTask.getDeviceMessageCategories()).thenReturn(Collections.singletonList(deviceMessageCategory));
        when(deviceMessageCategory.getId()).thenReturn(0);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        return comTaskEnablement;
    }

    private ComTaskEnablement createVerificationComTaskEnablement() {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        ComTask comTask = mock(ComTask.class);
        StatusInformationTask statusInformationTask = mock(StatusInformationTask.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(statusInformationTask));
        when(comTaskExecution2.getComTask()).thenReturn(comTask);
        return comTaskEnablement;
    }

    private TimeOfUseCampaignDomainExtension createCampaignExtension() {
        TimeOfUseCampaignDomainExtension timeOfUseCampaignDomainExtension = mock(TimeOfUseCampaignDomainExtension.class);
        when(timeOfUseCampaignDomainExtension.getUpdateType()).thenReturn("fullCalendar");
        when(timeOfUseCampaignDomainExtension.getUploadPeriodStart()).thenReturn(Instant.ofEpochSecond(36000));
        when(timeOfUseCampaignDomainExtension.getCalendar()).thenReturn(calendar);
        when(timeOfUseCampaignDomainExtension.getActivationOption()).thenReturn("immediately");
        return timeOfUseCampaignDomainExtension;
    }

    private Device createDevice() {
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);
        AllowedCalendar allowedCalendar = mock(AllowedCalendar.class);
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        Device.DeviceMessageBuilder messageBuilder = mock(Device.DeviceMessageBuilder.class);
        Device.CalendarSupport calendarSupport = mock(Device.CalendarSupport.class);
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        comTaskExecutions.add(comTaskExecution);
        comTaskExecutions.add(comTaskExecution2);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.of(connectionTask));

        when(device.getComTaskExecutions()).thenReturn(comTaskExecutions);
        when(device.newDeviceMessage(any())).thenReturn(messageBuilder);
        when(device.calendars()).thenReturn(calendarSupport);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.getValueType()).thenReturn(Date.class);
        when(deviceMessageSpec.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(deviceMessageSpecificationService.findMessageSpecById(anyLong())).thenReturn(Optional.of(deviceMessageSpec));
        when(device.getId()).thenReturn(1L);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getAllowedCalendars()).thenReturn(Collections.singletonList(allowedCalendar));
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(calendar));
        return device;
    }
}
