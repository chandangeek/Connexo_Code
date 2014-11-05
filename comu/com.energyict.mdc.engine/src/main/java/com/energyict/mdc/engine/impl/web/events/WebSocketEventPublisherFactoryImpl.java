package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;
import com.energyict.mdc.engine.model.EngineModelService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides an implementation for the {@link WebSocketEventPublisherFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-09 (13:03)
 */
@Component(name = "com.energyict.mdc.engine.eventapi.publisher.factory", service = WebSocketEventPublisherFactory.class, immediate = true)
public class WebSocketEventPublisherFactoryImpl implements WebSocketEventPublisherFactory {

    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile DeviceService deviceService;
    private volatile EngineModelService engineModelService;

    @Override
    public WebSocketEventPublisher newWebSocketEventPublisher(WebSocketCloseEventListener closeEventListener) {
        return new WebSocketEventPublisher(new ServiceProvider(), closeEventListener);
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
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
        public EngineModelService engineModelService() {
            return engineModelService;
        }
    }

}