/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallStateChangeTopicHandler;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignCustomPropertySet;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignDomainExtension;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignServiceCallHandler;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignServiceImpl;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseItemServiceCallHandler;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.google.common.collect.ImmutableMap;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ToUCampaignServiceIT {

    private static ToUCampaignInMemoryPersistence inMemoryPersistence = new ToUCampaignInMemoryPersistence();
    private static ServiceCallService serviceCallService;
    private static MeteringGroupsService meteringGroupsService;
    private static TransactionService transactionService;
    private static TimeOfUseCampaignServiceImpl timeOfUseCampaignService;
    private static ServiceCallHandler serviceCallHandler;
    private static CalendarService calendarService;
    private static DeviceConfigurationService deviceConfigurationService;
    private static DeviceMessageSpecificationService deviceMessageSpecificationService;
    private Clock clock = inMemoryPersistence.get(Clock.class);
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
    private static DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
    private static DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
    private static ProtocolPluggableService protocolPluggableService;
    private static CustomPropertySet<ServiceCall, TimeOfUseCampaignDomainExtension> serviceCallCPS;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.get(TransactionService.class));

    @BeforeClass
    public static void setUp() {
        inMemoryPersistence.activate();
        serviceCallService = inMemoryPersistence.get(ServiceCallService.class);
        timeOfUseCampaignService = inMemoryPersistence.get(TimeOfUseCampaignServiceImpl.class);
        meteringGroupsService = inMemoryPersistence.get(MeteringGroupsService.class);
        transactionService = inMemoryPersistence.get(TransactionService.class);
        calendarService = inMemoryPersistence.get(CalendarService.class);
        deviceConfigurationService = inMemoryPersistence.get(DeviceConfigurationService.class);
        deviceMessageSpecificationService = inMemoryPersistence.get(DeviceMessageSpecificationService.class);
        protocolPluggableService = inMemoryPersistence.get(ProtocolPluggableService.class);
        List<DeviceMessageSpec> deviceMessageIds;
        deviceMessageIds = new ArrayList<>();
        DeviceMessageSpec deviceMessageSpec1 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        DeviceMessageSpec deviceMessageSpec2 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);
        DeviceMessageSpec deviceMessageSpec3 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec3.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_SEND.dbValue());
        deviceMessageIds.add(deviceMessageSpec3);
        DeviceMessageSpec deviceMessageSpec4 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec4.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME.dbValue());
        deviceMessageIds.add(deviceMessageSpec4);
        DeviceMessageSpec deviceMessageSpec5 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec5.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND.dbValue());
        deviceMessageIds.add(deviceMessageSpec5);
        when(deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.empty());
        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encryptionAccessLevel = mock(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Arrays.asList(deviceProtocolDialect));
        when(deviceProtocolPluggableClass.getId()).thenReturn(1L);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolDialect.getDeviceProtocolDialectName()).thenReturn("name");
        when(protocolPluggableService.findDeviceProtocolPluggableClass(anyInt())).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(protocolPluggableService.adapt(any(AuthenticationDeviceAccessLevel.class))).thenAnswer(invocation -> UPLAuthenticationLevelAdapter.adaptTo(invocation.getArgumentAt(0, DeviceAccessLevel.class), thesaurus));
        when(protocolPluggableService.adapt(any(EncryptionDeviceAccessLevel.class))).thenAnswer(invocation -> UPLEncryptionLevelAdapter.adaptTo(invocation.getArgumentAt(0, DeviceAccessLevel.class), thesaurus));
        transactionService.execute(() -> {
            EventServiceImpl eventService = inMemoryPersistence.get(EventServiceImpl.class);
            eventService.addTopicHandler(inMemoryPersistence.get(StateTransitionTriggerEventTopicHandler.class));
            eventService.addTopicHandler(inMemoryPersistence.get(ServiceCallStateChangeTopicHandler.class));
            PropertySpecService propertySpecService = inMemoryPersistence.get(PropertySpecService.class);
            serviceCallCPS = new TimeOfUseCampaignCustomPropertySet(thesaurus, propertySpecService, timeOfUseCampaignService);
            serviceCallHandler = new TimeOfUseCampaignServiceCallHandler(timeOfUseCampaignService);
            serviceCallService.addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", TimeOfUseCampaignServiceCallHandler.NAME));
            serviceCallService.addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", TimeOfUseItemServiceCallHandler.NAME));
            return null;
        });
    }

    @AfterClass
    public static void tearDown() {
        inMemoryPersistence.deactivate();
    }

    @Test
    @Transactional
    public void createCampaignTest() {
        makeDefaultCampaign();
        assertTrue(serviceCallService.getServiceCallFinder().stream()
                .anyMatch(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).isPresent()));
    }

    @Test
    @Transactional
    public void getAllCampaignsTest() {
        assertThat(timeOfUseCampaignService.streamAllCampaigns().collect(Collectors.toList())).isEmpty();
        makeDefaultCampaign();
        assertThat(timeOfUseCampaignService.streamAllCampaigns().collect(Collectors.toList())).isNotEmpty();
    }

    @Test
    @Transactional
    public void touBuilderTest() {
        Calendar calendar1 = makeCalendar("Cal01", "1");
        DeviceType deviceType1 = deviceConfigurationService.newDeviceType("Elster AS1440", deviceProtocolPluggableClass);
        String name = "toucamp-01";
        String deviceGroup = "Electro";
        Instant activationStart = Instant.ofEpochSecond(70000);
        Instant activationEnd = Instant.ofEpochSecond(75000);
        String activationOption = "immediately";
        String updateType = "fullCalendar";
        long timeValidation = 120;
        TimeOfUseCampaign timeOfUseCampaign1 = timeOfUseCampaignService.newTouCampaignBuilder(name, deviceType1, calendar1)
                .withUploadTimeBoundaries(activationStart, activationEnd)
                .withDeviceGroup(deviceGroup)
                .withActivationOption(activationOption)
                .withActivationOption(activationOption)
                .withUpdateType(updateType)
                .withValidationTimeout(timeValidation)
                .create();
        assertThat(timeOfUseCampaign1.getName()).isEqualTo(name);
        assertThat(timeOfUseCampaign1.getDeviceGroup()).isEqualTo(deviceGroup);
        assertThat(timeOfUseCampaign1.getDeviceType()).isEqualTo(deviceType1);
        assertThat(timeOfUseCampaign1.getUploadPeriodStart()).isEqualTo(activationStart);
        assertThat(timeOfUseCampaign1.getUploadPeriodEnd()).isEqualTo(activationEnd);
        assertThat(timeOfUseCampaign1.getCalendar()).isEqualTo(calendar1);
        assertThat(timeOfUseCampaign1.getActivationOption()).isEqualTo(activationOption);
        assertThat(timeOfUseCampaign1.getUpdateType()).isEqualTo(updateType);
        assertThat(timeOfUseCampaign1.getValidationTimeout()).isEqualTo(timeValidation);
    }

    @Test
    @Transactional
    public void editCampaignTest() {
        TimeOfUseCampaignDomainExtension timeOfUseCampaign1 = makeDefaultCampaign();
        timeOfUseCampaign1.setName("tou-c-2");
        timeOfUseCampaign1.setUploadPeriodStart(Instant.ofEpochSecond(1544450000));
        timeOfUseCampaign1.setUploadPeriodEnd(Instant.ofEpochSecond(1544460000));
        timeOfUseCampaign1.update();
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(TimeOfUseCampaignDomainExtension.class).get().getName()).isEqualTo(timeOfUseCampaign1.getName());
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(TimeOfUseCampaignDomainExtension.class).get().getUploadPeriodStart()).isEqualTo(timeOfUseCampaign1.getUploadPeriodStart());
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(TimeOfUseCampaignDomainExtension.class).get().getUploadPeriodEnd()).isEqualTo(timeOfUseCampaign1.getUploadPeriodEnd());
    }

    @Test
    @Transactional
    public void getCampaignByNameTest() {
        makeDefaultCampaign();
        assertThat(timeOfUseCampaignService.getCampaign("tou-c-1").get()).isInstanceOf(TimeOfUseCampaign.class);
    }

    @Test
    @Transactional
    public void getDeviceTypesWithCalendarTest() {
        DeviceType trueType = deviceConfigurationService.newDeviceType("Elster 999", deviceProtocolPluggableClass);
        Calendar calendar = makeCalendar("cal01", "2");
        calendar.activate();
        deviceConfigurationService.findAllDeviceTypes().find().get(0).addCalendar(calendar);
        Set<ProtocolSupportedCalendarOptions> set = new HashSet<>();
        set.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        TimeOfUseOptions timeOfUseOptions = deviceConfigurationService.newTimeOfUseOptions(trueType);
        timeOfUseOptions.setOptions(set);
        timeOfUseOptions.save();
        deviceConfigurationService.findTimeOfUseOptions(trueType).get().setOptions(set);
        assertThat(timeOfUseCampaignService.getDeviceTypesWithCalendars()).contains(trueType);
    }

    @Test
    @Transactional
    public void cancelCampaignTest() {
        TimeOfUseCampaign timeOfUseCampaign = makeDefaultCampaign();
        timeOfUseCampaign.cancel();
        assertThat(serviceCallService.getServiceCallFinder().find().get(0).getLogs().find().contains("campaign cancelled by user"));
    }


    private TimeOfUseCampaignDomainExtension makeDefaultCampaign() {
        return makeCampaign(null, 0, 0, null, null, null, null, null, null, 0);
    }

    private TimeOfUseCampaignDomainExtension makeCampaign(String name, long activationStart, long activationEnd, Calendar calendar,
                                                          String group, DeviceType deviceType, String updateType,
                                                          String activationOption, Instant activationDate, long timeValidation) {
        TimeOfUseCampaignDomainExtension timeOfUseCampaign = new TimeOfUseCampaignDomainExtension(timeOfUseCampaignService);
        timeOfUseCampaign.setName(name == null ? "tou-c-1" : name);
        timeOfUseCampaign.setUploadPeriodStart(Instant.ofEpochSecond(activationStart == 0 ? 1544400000 : activationStart));
        timeOfUseCampaign.setUploadPeriodEnd(Instant.ofEpochSecond(activationEnd == 0 ? 1544410000 : activationEnd));
        timeOfUseCampaign.setCalendar(calendar == null ? makeCalendar("Re-Cu-01", "1") : calendar);
        timeOfUseCampaign.setDeviceGroup(group == null ? "group1" : group);
        timeOfUseCampaign.setDeviceType(deviceType == null ? deviceConfigurationService.newDeviceType("Elster AS1440", deviceProtocolPluggableClass) : deviceType);
        timeOfUseCampaign.setUpdateType(updateType == null ? "fullCalendar" : updateType);
        timeOfUseCampaign.setActivationOption(activationOption == null ? "immediately" : activationOption);
        timeOfUseCampaign.setActivationDate(activationDate == null ? clock.instant() : activationDate);
        timeOfUseCampaign.setValidationTimeout(timeValidation == 0 ? 120 : timeValidation);
        timeOfUseCampaignService.createServiceCallAndTransition(timeOfUseCampaign);
        return timeOfUseCampaign;
    }

    private Calendar makeCalendar(String name, String mrId) {
        EventSet eventSet = calendarService.newEventSet("eventSet")
                .addEvent("On peak").withCode(3)
                .addEvent("Off peak").withCode(5)
                .addEvent("Demand response").withCode(97)
                .add();
        Category category = calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.name()).get();
        return calendarService.newCalendar(name, category, Year.of(2010), eventSet).description("Created via gogo command :-)")
                .mRID(mrId)
                .newDayType("Summer weekday")
                .event("Off peak").startsFrom(LocalTime.of(0, 0, 0))
                .eventWithCode(3).startsFrom(LocalTime.of(13, 0, 0))
                .event("Off peak").startsFrom(LocalTime.of(20, 0, 0))
                .add()
                .newDayType("Weekend")
                .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                .add()
                .newDayType("Holiday")
                .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                .add()
                .newDayType("Winter day")
                .event("Off peak").startsFrom(LocalTime.of(0, 0, 0))
                .event("On peak").startsFrom(LocalTime.of(5, 0, 0))
                .event("Off peak").startsFrom(LocalTime.of(21, 0, 0))
                .add()
                .newDayType("Demand response")
                .eventWithCode(97).startsFrom(LocalTime.MIDNIGHT)
                .add()
                .addPeriod("Summer", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Weekend", "Weekend")
                .addPeriod("Winter", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day")
                .on(MonthDay.of(5, 1)).transitionTo("Summer")
                .on(MonthDay.of(11, 1)).transitionTo("Winter")
                .except("Holiday")
                .occursOnceOn(LocalDate.of(2016, 1, 18))
                .occursOnceOn(LocalDate.of(2016, 2, 15))
                .occursOnceOn(LocalDate.of(2016, 5, 30))
                .occursAlwaysOn(MonthDay.of(7, 4))
                .occursOnceOn(LocalDate.of(2016, 9, 5))
                .occursOnceOn(LocalDate.of(2016, 10, 10))
                .occursAlwaysOn(MonthDay.of(11, 11))
                .occursOnceOn(LocalDate.of(2016, 11, 24))
                .occursAlwaysOn(MonthDay.of(12, 25))
                .occursAlwaysOn(MonthDay.of(12, 26))
                .add()
                .add();
    }
}
