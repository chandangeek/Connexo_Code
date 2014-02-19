package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides factory services for {@link DeviceProtocolPluggableClassDeletionMessageHandler}s
 * as an OSGi component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-19 (14:55)
 */
@Component(name="com.energyict.mdc.device.config.deviceprotocol.delete.event.handler", service = MessageHandlerFactory.class, property = {"subscriber=MDCEVENTHANDLER", "destination=" + EventService.JUPITER_EVENTS}, immediate = true)
public class DeviceProtocolPluggableClassDeletionMessageHandlerFactory implements MessageHandlerFactory {

    private volatile JsonService jsonService;
    private volatile NlsService nlsService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;

    @Override
    public MessageHandler newMessageHandler() {
        return new DeviceProtocolPluggableClassDeletionMessageHandler(this.deviceConfigurationService, this.protocolPluggableService, this.nlsService, this.jsonService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

}