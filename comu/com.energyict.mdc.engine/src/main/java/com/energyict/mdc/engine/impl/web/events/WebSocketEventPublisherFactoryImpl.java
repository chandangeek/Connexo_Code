package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;
import com.energyict.mdc.protocol.api.services.IdentificationService;

/**
 * Provides an implementation for the {@link WebSocketEventPublisherFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-09 (13:03)
 */
public class WebSocketEventPublisherFactoryImpl implements WebSocketEventPublisherFactory {

    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;
    private final DeviceService deviceService;
    private final EngineConfigurationService engineConfigurationService;
    private final IdentificationService identificationService;
    private final EventPublisher eventPublisher;

    public WebSocketEventPublisherFactoryImpl(ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, DeviceService deviceService, EngineConfigurationService engineConfigurationService, IdentificationService identificationService, EventPublisher eventPublisher) {
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.deviceService = deviceService;
        this.engineConfigurationService = engineConfigurationService;
        this.identificationService = identificationService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public WebSocketEventPublisher newWebSocketEventPublisher(WebSocketCloseEventListener closeEventListener) {
        return new WebSocketEventPublisher(new ServiceProvider(), this.eventPublisher, closeEventListener);
    }

    private class ServiceProvider implements RequestParser.ServiceProvider {

        @Override
        public ConnectionTaskService connectionTaskService() {
            return connectionTaskService;
        }

        @Override
        public CommunicationTaskService communicationTaskService() {
            return communicationTaskService;
        }

        @Override
        public DeviceService deviceService() {
            return deviceService;
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return engineConfigurationService;
        }

        @Override
        public IdentificationService identificationService() {
            return identificationService;
        }
    }

}