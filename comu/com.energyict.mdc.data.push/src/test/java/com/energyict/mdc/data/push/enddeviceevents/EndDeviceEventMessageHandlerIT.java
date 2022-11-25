/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.data.push.enddeviceevents;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.h2.impl.TransientMessage;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;

import org.osgi.framework.BundleContext;

import java.time.Instant;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceEventMessageHandlerIT {
    private static final Instant INSTANT = Instant.ofEpochMilli(100500);
    private static final long LOGBOOK_ID = 33;
    private static final String ENDPOINT1_NAME = "Znaesh li ty vdol nochnyh dorog";
    private static final String ENDPOINT2_NAME = "Shla bosikom ne zhaleya nog";
    private static InMemoryIntegrationPersistence inMemoryIntegrationPersistence = new InMemoryIntegrationPersistence();
    private static BundleContext bundleContext;
    private static EndDeviceEventsServiceProvider endDeviceEventsServiceProvider;
    private static EndDeviceEventMessageHandlerFactory endDeviceEventMessageHandlerFactory;
    private static TransactionService transactionService;
    private static EndPointConfiguration endpoint1, endpoint2;
    private static Meter meter;
    private static EndDeviceEventRecord event, customEvent;
    private static EventType eventCreatedEventType;

    @Rule
    public TestRule transactional = new TransactionalRule(transactionService);

    @BeforeClass
    public static void beforeClass() throws Exception {
        inMemoryIntegrationPersistence.initializeDatabase();
        bundleContext = inMemoryIntegrationPersistence.getInstance(BundleContext.class);
        transactionService = inMemoryIntegrationPersistence.getTransactionService();
        endDeviceEventsServiceProvider = inMemoryIntegrationPersistence.getInstance(EndDeviceEventsServiceProvider.class);
        endDeviceEventMessageHandlerFactory = inMemoryIntegrationPersistence.getEndDeviceEventMessageHandlerFactory();

        transactionService.run(() -> {
            EndPointConfigurationService endPointConfigurationService = inMemoryIntegrationPersistence.getInstance(EndPointConfigurationService.class);
            endpoint1 = endPointConfigurationService
                    .newOutboundEndPointConfiguration(ENDPOINT1_NAME, EndDeviceEventsServiceProvider.NAME, "url1")
                    .logLevel(LogLevel.INFO)
                    .setAuthenticationMethod(EndPointAuthentication.NONE)
                    .create();
            endpoint2 = endPointConfigurationService
                    .newOutboundEndPointConfiguration(ENDPOINT2_NAME, EndDeviceEventsServiceProvider.NAME, "url2")
                    .logLevel(LogLevel.INFO)
                    .setAuthenticationMethod(EndPointAuthentication.NONE)
                    .create();
            MeteringService meteringService = inMemoryIntegrationPersistence.getInstance(MeteringService.class);
            AmrSystem mdc = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            meter = mdc.newMeter("11", "MeterrRRR!!1").create();
            EndDeviceEventType endDeviceEventType = meteringService.createEndDeviceEventType("1.2.3.44");
            event = meter.addEventRecord(endDeviceEventType, INSTANT, LOGBOOK_ID)
                    .create();
            endDeviceEventType = meteringService.getEndDeviceEventType("0.0.0.0").get();
            customEvent = meter.addEventRecord(endDeviceEventType, INSTANT, LOGBOOK_ID)
                    .setDeviceEventType("EVTMTRS01")
                    .create();
            eventCreatedEventType = inMemoryIntegrationPersistence.getInstance(EventService.class)
                    .getEventType(com.elster.jupiter.metering.EventType.END_DEVICE_EVENT_CREATED.topic())
                    .get();
        });
    }

    @AfterClass
    public static void afterClass() throws Exception {
        inMemoryIntegrationPersistence.cleanUpDataBase();
    }

    @After
    public void tearDown() {
        Mockito.reset(bundleContext, endDeviceEventsServiceProvider);
    }

    @Test
    @Transactional
    public void testNoProperties() {
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isFalse();

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verifyZeroInteractions(endDeviceEventsServiceProvider);
    }

    @Test
    @Transactional
    public void testEventIsSent() {
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.?.3.*");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));

        Mockito.verify(endDeviceEventsServiceProvider).call(event, endpoint1, endpoint2);
    }

    @Test
    @Transactional
    public void testCustomEventIsSent() {
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("*MTR?01");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verify(endDeviceEventsServiceProvider).call(customEvent, endpoint1, endpoint2);
    }

    @Test
    @Transactional
    public void testEventsAreFilteredOutByCodes() {
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.*.3.?");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("?MTR*01");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isFalse();

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verifyZeroInteractions(endDeviceEventsServiceProvider);
    }

    @Test
    @Transactional
    public void testBothEventsAreSent() {
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.*.3.?;1.?.3.*");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("?MTR*01;*MTR?01");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isFalse();

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verify(endDeviceEventsServiceProvider).call(event, endpoint1, endpoint2);
        Mockito.verify(endDeviceEventsServiceProvider).call(customEvent, endpoint1, endpoint2);
    }

    @Test
    @Transactional
    public void testNoEndpoints() {
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.?.3.*");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("*MTR?01");
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isFalse();

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verifyZeroInteractions(endDeviceEventsServiceProvider);
    }

    @Test
    @Transactional
    public void testBothEventsAreSentWithDeviceGroupFilter() {
        MeteringGroupsService meteringGroupsService = inMemoryIntegrationPersistence.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createEnumeratedEndDeviceGroup(meter).setName("MTRGroup").create();

        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.?.3.*");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("*MTR?01");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_GROUPS_PROPERTY_NAME)).thenReturn("MTRG*;Group");
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isTrue();

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verify(endDeviceEventsServiceProvider).call(event, endpoint1, endpoint2);
        Mockito.verify(endDeviceEventsServiceProvider).call(customEvent, endpoint1, endpoint2);
    }

    @Test
    @Transactional
    public void testNotMatchingGroup() {
        MeteringGroupsService meteringGroupsService = inMemoryIntegrationPersistence.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createEnumeratedEndDeviceGroup(meter).setName("MTRGroup").create();

        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.?.3.*");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("*MTR?01");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_GROUPS_PROPERTY_NAME)).thenReturn("MTRG?;Group");
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isTrue();

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verifyZeroInteractions(endDeviceEventsServiceProvider);
    }

    @Test
    @Transactional
    public void testGroupDoesNotContainTheDevice() {
        MeteringGroupsService meteringGroupsService = inMemoryIntegrationPersistence.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createEnumeratedEndDeviceGroup().setName("MTRGroup").create();

        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.?.3.*");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("*MTR?01");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_GROUPS_PROPERTY_NAME)).thenReturn("MTRG*;Group");
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isTrue();

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verifyZeroInteractions(endDeviceEventsServiceProvider);
    }

    @Test
    @Transactional
    public void testNoGroup() {
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.?.3.*");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("*MTR?01");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_GROUPS_PROPERTY_NAME)).thenReturn("*");
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isTrue();

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verifyZeroInteractions(endDeviceEventsServiceProvider);
    }

    @Test
    @Transactional
    public void testNoEvents() {
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.CIM_EVENT_CODES_PROPERTY_NAME)).thenReturn("1.?.3.*");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.DEVICE_EVENT_CODES_PROPERTY_NAME)).thenReturn("*MTR?01");
        Mockito.when(bundleContext.getProperty(EndDeviceEventMessageHandlerFactory.WS_ENDPOINTS_PROPERTY_NAME)).thenReturn(ENDPOINT1_NAME + ';' + ENDPOINT2_NAME);
        endDeviceEventMessageHandlerFactory.activate(bundleContext);

        assertThat(endDeviceEventMessageHandlerFactory.getCimEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceEventCodePattern()).isPresent();
        assertThat(endDeviceEventMessageHandlerFactory.getEndpoints()).isNotEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.getDeviceGroups()).isEmpty();
        assertThat(endDeviceEventMessageHandlerFactory.needToFilterByDeviceGroups()).isFalse();

        inMemoryIntegrationPersistence.getInstance(MeteringDataModelService.class)
                .getDataModel()
                .mapper(EndDeviceEventRecord.class)
                .remove(Arrays.asList(event, customEvent));

        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(event));
        endDeviceEventMessageHandlerFactory.newMessageHandler().process(createMessage(customEvent));

        Mockito.verifyZeroInteractions(endDeviceEventsServiceProvider);
    }

    private static Message createMessage(EndDeviceEventRecord event) {
        LocalEvent localEvent = eventCreatedEventType.create(event);
        return new TransientMessage(localEvent.toString().getBytes());
    }
}
