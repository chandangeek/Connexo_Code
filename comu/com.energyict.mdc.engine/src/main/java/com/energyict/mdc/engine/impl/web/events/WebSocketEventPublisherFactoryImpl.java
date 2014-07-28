package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.DeviceDataService;
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

    private volatile DeviceDataService deviceDataService;
    private volatile EngineModelService engineModelService;

    @Override
    public WebSocketEventPublisher newWebSocketEventPublisher(WebSocketCloseEventListener closeEventListener) {
        return new WebSocketEventPublisher(new ServiceProvider(), closeEventListener);
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    private class ServiceProvider implements RequestParser.ServiceProvider {
        @Override
        public DeviceDataService deviceDataService() {
            return deviceDataService;
        }

        @Override
        public EngineModelService engineModelService() {
            return engineModelService;
        }
    }

}