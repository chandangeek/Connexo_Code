/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.logging.ComPortOperationsLogger;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactoryImpl;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceImpl;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.time.Clock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComPortOperationsLogHandlerTest {

    @Mock
    EventPublisher eventPublisher;
    @Mock
    ServiceProvider serviceProvider;
    @Mock
    private ComPort comPort;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceMessageService deviceMessageService;
    @Mock
    private OnlineComServer comServer;
    @Mock
    private RunningOnlineComServer runningComServer;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private EngineService engineService;
    @Mock
    private NlsService nlsService;
    @Mock
    private EventService eventService;
    @Mock
    private EventAPIStatistics eventApiStatistics;
    @Mock
    private TransactionService transactionService;

    private ComPortOperationsLogger comPortOperationsLogger;
    private static final String eventRegistrationURL = "ws://localhost:8282/events/registration";

    @Before
    public void setupEmbeddedWebServerFactory() {
        when(comServer.getEventRegistrationUriIfSupported()).thenReturn(eventRegistrationURL);
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(serviceProvider.eventPublisher()).thenReturn(eventPublisher);

        ComPortOperationsLogHandler comportOperationsLogHandler = new ComPortOperationsLogHandler(comPort, serviceProvider.eventPublisher(), serviceProvider);
        comPortOperationsLogger = LoggerFactory.getLoggerFor(ComPortOperationsLogger.class, this.getAnonymousLogger(comportOperationsLogHandler));

        WebSocketEventPublisherFactoryImpl webSocketEventPublisherFactory =
                new WebSocketEventPublisherFactoryImpl(
                        this.runningComServer,
                        this.connectionTaskService,
                        this.communicationTaskService,
                        this.deviceService,
                        this.engineConfigurationService,
                        this.identificationService,
                        serviceProvider.eventPublisher());
        EmbeddedWebServerFactory embeddedWebServerFactory = new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory);
        embeddedWebServerFactory.findOrCreateEventWebServer(comServer, eventApiStatistics);
    }

    @Test
    public void testStartedResultsInComPortOperationEvent(){
        comPortOperationsLogger.started("TheThreadName");
    }

    private Logger getAnonymousLogger (Handler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(handler);
        return logger;
    }

    private class ServiceProvider implements ExecutionContext.ServiceProvider,  AbstractComServerEventImpl.ServiceProvider  {

        private MdcReadingTypeUtilService mdcReadingTypeUtilService = new MdcReadingTypeUtilServiceImpl();

        @Override
        public Clock clock() {
            return Clock.systemDefaultZone();
        }

        @Override
        public NlsService nlsService() {
            return nlsService;
        }

        @Override
        public EventService eventService() {
            return eventService;
        }

        @Override
        public IssueService issueService() {
            return null;
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return connectionTaskService;
        }

        @Override
        public DeviceService deviceService() {
            return deviceService;
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return deviceMessageService;
        }

        @Override
        public EventPublisher eventPublisher() {
            return eventPublisher;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return mdcReadingTypeUtilService;
        }

        @Override
        public EngineService engineService() {
            return engineService;
        }

        @Override
        public TransactionService transactionService() {
            return transactionService;
        }
    }

}
