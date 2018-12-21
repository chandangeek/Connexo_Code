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
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignCustomPropertySet;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignDomainExtension;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignServiceCallHandler;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ToUCampaignServiceIT {

    private static ToUCampaignInMemoryPersistence inMemoryPersistence = new ToUCampaignInMemoryPersistence();
    private static ServiceCallService serviceCallService;
    private static MeteringGroupsService meteringGroupsService;
    private static TransactionService transactionService;
    private static TimeOfUseCampaignService timeOfUseCampaignService;
    private static ServiceCallHandler serviceCallHandler;
    private static CalendarService calendarService;
    private static DeviceConfigurationService deviceConfigurationService;
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private static DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
    @Mock
    private static DeviceProtocol deviceProtocol;
    //    private static Calendar calendar;
//    private static DeviceType deviceType;
    //    private static DataExportServiceCallType dataExportServiceCallType;
    private static CustomPropertySet<ServiceCall, TimeOfUseCampaignDomainExtension> serviceCallCPS;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.get(TransactionService.class));

    @BeforeClass
    public static void setUp() {
        inMemoryPersistence.activate();
        serviceCallService = inMemoryPersistence.get(ServiceCallService.class);
        timeOfUseCampaignService = inMemoryPersistence.get(TimeOfUseCampaignService.class);
        meteringGroupsService = inMemoryPersistence.get(MeteringGroupsService.class);
        transactionService = inMemoryPersistence.get(TransactionService.class);
        calendarService = inMemoryPersistence.get(CalendarService.class);
        deviceConfigurationService = inMemoryPersistence.get(DeviceConfigurationService.class);
        when(deviceProtocolPluggableClass.getId()).thenReturn(1L);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        //  when(deviceConfigurationService.findDeviceType(0).get()).thenReturn(deviceType);

        transactionService.execute(() -> {
            EventServiceImpl eventService = inMemoryPersistence.get(EventServiceImpl.class);
            // add transition topic handlers to make fsm transitions work
            eventService.addTopicHandler(inMemoryPersistence.get(StateTransitionTriggerEventTopicHandler.class));
            eventService.addTopicHandler(inMemoryPersistence.get(ServiceCallStateChangeTopicHandler.class));
            PropertySpecService propertySpecService = inMemoryPersistence.get(PropertySpecService.class);
            serviceCallCPS = new TimeOfUseCampaignCustomPropertySet(thesaurus, propertySpecService, timeOfUseCampaignService);
//            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(serviceCallCPS);
//            dataExportServiceCallType = inMemoryPersistence.get(DataExportService.class).getDataExportServiceCallType();
            serviceCallHandler = new TimeOfUseCampaignServiceCallHandler(timeOfUseCampaignService);
            serviceCallService.addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", TimeOfUseCampaignServiceCallHandler.NAME));
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
        TimeOfUseCampaignDomainExtension timeOfUseCampaign = new TimeOfUseCampaignDomainExtension();
        timeOfUseCampaign.setName("tou-c-1");
        timeOfUseCampaign.setActivationStart(Instant.ofEpochSecond(1544400000));
        timeOfUseCampaign.setActivationEnd(Instant.ofEpochSecond(1544410000));
        timeOfUseCampaign.setCalendar(makeCalendar("Re-Cu-01"));
        timeOfUseCampaign.setDeviceGroup("group1");
        timeOfUseCampaign.setDeviceType(deviceConfigurationService.newDeviceType("Elster AS1440", deviceProtocolPluggableClass));
        timeOfUseCampaign.setUpdateType("fullCalendar");
        timeOfUseCampaign.setActivationDate("Immediately");
        timeOfUseCampaign.setTimeValidation(120);
        timeOfUseCampaignService.createToUCampaign(timeOfUseCampaign);
        assertTrue(serviceCallService.getServiceCallFinder().stream()
                .anyMatch(serviceCall -> serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).isPresent()));
    }

    @Test
    @Transactional
    public void getAllCampaignsTest() {
        assertThat(timeOfUseCampaignService.getAllCampaigns()).isEmpty();
    }

    @Test
    @Transactional
    public void touBuilderTest() {
        Calendar calendar1 = makeCalendar("Cal01");
        DeviceType deviceType1 = deviceConfigurationService.newDeviceType("Elster AS1440", deviceProtocolPluggableClass);
        String name="toucamp-01";
        long deviceType=1;
        String deviceGroup="Electro";
        Instant activationStart=Instant.ofEpochSecond(1544400000);
        Instant activationEnd=Instant.ofEpochSecond(1544410000);
        long calendar=1;
        String activationDate="immediately";
        String updateType="fullCalendar";
        long timeValidation=120;
        TimeOfUseCampaign timeOfUseCampaign1 = timeOfUseCampaignService.newToUbuilder(name,deviceType,deviceGroup,activationStart,activationEnd,calendar,activationDate,updateType,timeValidation).create();
        assertThat(timeOfUseCampaign1.getName()).isEqualTo(name);
        assertThat(timeOfUseCampaign1.getDeviceGroup()).isEqualTo(deviceGroup);
        assertThat(timeOfUseCampaign1.getDeviceType()).isEqualTo(deviceType1);
        assertThat(timeOfUseCampaign1.getActivationStart()).isEqualTo(activationStart);
        assertThat(timeOfUseCampaign1.getActivationEnd()).isEqualTo(activationEnd);
        assertThat(timeOfUseCampaign1.getCalendar()).isEqualTo(calendar1);
        assertThat(timeOfUseCampaign1.getActivationDate()).isEqualTo(activationDate);
        assertThat(timeOfUseCampaign1.getUpdateType()).isEqualTo(updateType);
        assertThat(timeOfUseCampaign1.getTimeValidation()).isEqualTo(timeValidation);
    }

    @Test
    @Transactional
    public void editCampaignTest() {
        TimeOfUseCampaignDomainExtension timeOfUseCampaign = new TimeOfUseCampaignDomainExtension();
        meteringGroupsService.createEnumeratedEndDeviceGroup().setName("group1").create();
        timeOfUseCampaign.setName("tou-c-1");
        timeOfUseCampaign.setActivationStart(Instant.ofEpochSecond(1544400000));
        timeOfUseCampaign.setActivationEnd(Instant.ofEpochSecond(1544410000));
        timeOfUseCampaign.setCalendar(makeCalendar("Cal01"));
        timeOfUseCampaign.setDeviceGroup("group1");
        timeOfUseCampaign.setDeviceType(deviceConfigurationService.newDeviceType("Elster AS1440", deviceProtocolPluggableClass));
        timeOfUseCampaign.setUpdateType("fullCalendar");
        timeOfUseCampaign.setActivationDate("immediately");
        timeOfUseCampaign.setTimeValidation(120);
        timeOfUseCampaignService.createToUCampaign(timeOfUseCampaign);
        TimeOfUseCampaignDomainExtension timeOfUseCampaign1 = timeOfUseCampaign;
        timeOfUseCampaign1.setName("tou-c-2");
        timeOfUseCampaign1.setActivationStart(Instant.ofEpochSecond(1544450000));
        timeOfUseCampaign1.setActivationEnd(Instant.ofEpochSecond(1544460000));
        timeOfUseCampaignService.edit("tou-c-1", timeOfUseCampaign1);
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(TimeOfUseCampaignDomainExtension.class).get().getName()).isEqualTo(timeOfUseCampaign1.getName());
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(TimeOfUseCampaignDomainExtension.class).get().getActivationStart()).isEqualTo(timeOfUseCampaign1.getActivationStart());
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(TimeOfUseCampaignDomainExtension.class).get().getActivationEnd()).isEqualTo(timeOfUseCampaign1.getActivationEnd());
    }

    @Test
    @Transactional
    public void test() {
        //timeOfUseCampaignService.retry();
    }


    private Calendar makeCalendar(String name){
        EventSet eventSet = calendarService.newEventSet("eventSet")
                .addEvent("On peak").withCode(3)
                .addEvent("Off peak").withCode(5)
                .addEvent("Demand response").withCode(97)
                .add();
        Category category = calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.name()).get();
        return calendarService.newCalendar(name, category, Year.of(2010), eventSet).description("Created via gogo command :-)")
                .mRID("1")
                .newDayType("Summer weekday")
                .event("Off peak").startsFrom(LocalTime.of(0,0,0))
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
                .event("Off peak").startsFrom(LocalTime.of(0,0,0))
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
