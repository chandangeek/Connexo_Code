package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.concurrent.CountDownLatch;

public class LatchDrivenWebSocketEventPublisherFactory extends WebSocketEventPublisherFactoryImpl {
    private CountDownLatch latch;
    private RequestParser.ServiceProvider serviceProvider;
    private EventPublisher eventPublisher;
    private RunningComServer comServer;

    public LatchDrivenWebSocketEventPublisherFactory(CountDownLatch latch, RunningComServer comServer, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, DeviceService deviceService, EngineConfigurationService engineConfigurationService, IdentificationService identificationService, EventPublisher eventPublisher, RequestParser.ServiceProvider serviceProvider) {
        super(comServer, connectionTaskService, communicationTaskService, deviceService,  engineConfigurationService, identificationService, eventPublisher);
        this.latch = latch;
        this.eventPublisher = eventPublisher;
        this.comServer = comServer;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public WebSocketEventPublisher newWebSocketEventPublisher(WebSocketCloseEventListener webSocketCloseEventListener) {
        return new LatchDrivenWebSocketEventPublisher(this.latch, comServer, serviceProvider, eventPublisher, webSocketCloseEventListener);
    }

    private class LatchDrivenWebSocketEventPublisher extends WebSocketEventPublisher {
        private CountDownLatch latch;
        private LatchDrivenWebSocketEventPublisher(CountDownLatch latch, RunningComServer comServer, RequestParser.ServiceProvider serviceProvider, EventPublisher eventPublisher, WebSocketCloseEventListener closeEventListener) {

            super(comServer, serviceProvider, eventPublisher, closeEventListener);
            this.latch = latch;
        }

        @Override
        public void onWebSocketText(String message) {
            super.onWebSocketText(message);
            latch.countDown();
        }
    }

}

