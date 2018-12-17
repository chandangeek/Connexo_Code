/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl;

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
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignCustomPropertySet;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignDomainExtension;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignServiceCallHandler;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ToUCampaignServiceIT {

    private static ToUCampaignInMemoryPersistence inMemoryPersistence = new ToUCampaignInMemoryPersistence();
    private static ServiceCallService serviceCallService;
    private static MeteringGroupsService meteringGroupsService;
    private static TransactionService transactionService;
    private static TimeOfUseCampaignService timeOfUseCampaignService;
    private static ServiceCallHandler serviceCallHandler;
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
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
        timeOfUseCampaign.setCalendar("cal01");
        timeOfUseCampaign.setDeviceGroup("group1");
        timeOfUseCampaign.setDeviceType("Elster AS1440");
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
        TimeOfUseCampaignDomainExtension timeOfUseCampaign = new TimeOfUseCampaignDomainExtension();
        timeOfUseCampaign.setName("tou-c-1");
        timeOfUseCampaign.setActivationStart(Instant.ofEpochSecond(1544400000));
        timeOfUseCampaign.setActivationEnd(Instant.ofEpochSecond(1544410000));
        timeOfUseCampaign.setCalendar("cal01");
        timeOfUseCampaign.setDeviceGroup("group1");
        timeOfUseCampaign.setDeviceType("Elster AS1440");
        timeOfUseCampaign.setUpdateType("fullCalendar");
        timeOfUseCampaign.setActivationDate("Immediately");
        timeOfUseCampaign.setTimeValidation(120);
        TimeOfUseCampaign timeOfUseCampaign1 = timeOfUseCampaignService.newToUbuilder(timeOfUseCampaign.getName(), timeOfUseCampaign.getDeviceType(),
                timeOfUseCampaign.getDeviceGroup(), timeOfUseCampaign.getActivationStart(), timeOfUseCampaign.getActivationEnd(),
                timeOfUseCampaign.getCalendar(), timeOfUseCampaign.getActivationDate(), timeOfUseCampaign.getUpdateType(),
                timeOfUseCampaign.getTimeValidation()).create();
        assertThat(timeOfUseCampaign.getName()).isEqualTo(timeOfUseCampaign1.getName());
        assertThat(timeOfUseCampaign.getDeviceGroup()).isEqualTo(timeOfUseCampaign1.getDeviceGroup());
        assertThat(timeOfUseCampaign.getDeviceType()).isEqualTo(timeOfUseCampaign1.getDeviceType());
        assertThat(timeOfUseCampaign.getActivationStart()).isEqualTo(timeOfUseCampaign1.getActivationStart());
        assertThat(timeOfUseCampaign.getActivationEnd()).isEqualTo(timeOfUseCampaign1.getActivationEnd());
        assertThat(timeOfUseCampaign.getCalendar()).isEqualTo(timeOfUseCampaign1.getCalendar());
        assertThat(timeOfUseCampaign.getActivationDate()).isEqualTo(timeOfUseCampaign1.getActivationDate());
        assertThat(timeOfUseCampaign.getUpdateType()).isEqualTo(timeOfUseCampaign1.getUpdateType());
        assertThat(timeOfUseCampaign.getTimeValidation()).isEqualTo(timeOfUseCampaign1.getTimeValidation());
    }

    @Test
    @Transactional
    public void editCampaignTest() {
        TimeOfUseCampaignDomainExtension timeOfUseCampaign = new TimeOfUseCampaignDomainExtension();
        meteringGroupsService.createEnumeratedEndDeviceGroup().setName("group1").create();
        timeOfUseCampaign.setName("tou-c-1");
        timeOfUseCampaign.setActivationStart(Instant.ofEpochSecond(1544400000));
        timeOfUseCampaign.setActivationEnd(Instant.ofEpochSecond(1544410000));
        timeOfUseCampaign.setCalendar("cal01");
        timeOfUseCampaign.setDeviceGroup("group1");
        timeOfUseCampaign.setDeviceType("Elster AS1440");
        timeOfUseCampaign.setUpdateType("fullCalendar");
        timeOfUseCampaign.setActivationDate("Immediately");
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
    public void test(){
        //timeOfUseCampaignService.retry();
    }

}
