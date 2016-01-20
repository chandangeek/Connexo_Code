package com.energyict.mdc.engine.impl.core.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.*;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ConnectionEvent;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.logging.ComPortOperationsLogger;
import com.energyict.mdc.engine.impl.events.*;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactoryImpl;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceImpl;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.logging.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 19/01/2016
 * Time: 15:34
 */
@RunWith(MockitoJUnitRunner.class)
public class ComPortOperationsLogHandlerTest {


    @Mock
    ServiceProvider serviceProvider;
    @Mock
    private ComPort comPort;
    @Mock
    private DeviceService deviceService;
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

    private ComPortOperationsLogHandler comportOperationsLogHandler;
    private ComPortOperationsLogger comPortOperationsLogger;
    private EmbeddedWebServerFactory embeddedWebServerFactory;
    private String eventRegistrationURL = "ws://localhost:8282/events/registration";


    @Before
    public void setupEmbeddedWebServerFactory() {
        comportOperationsLogHandler = new ComPortOperationsLogHandler(comPort, serviceProvider.eventPublisher(), serviceProvider);
        comPortOperationsLogger = LoggerFactory.getLoggerFor(ComPortOperationsLogger.class, this.getAnonymousLogger(comportOperationsLogHandler));

        WebSocketEventPublisherFactoryImpl webSocketEventPublisherFactory =
                new WebSocketEventPublisherFactoryImpl(
                        this.connectionTaskService,
                        this.communicationTaskService,
                        this.deviceService,
                        this.engineConfigurationService,
                        this.identificationService,
                        serviceProvider.eventPublisher());
        this.embeddedWebServerFactory = new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory);
        this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer);
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
        public EventPublisher eventPublisher() {
            return null;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return mdcReadingTypeUtilService;
        }

        @Override
        public EngineService engineService() {
            return engineService;
        }
    }

}
