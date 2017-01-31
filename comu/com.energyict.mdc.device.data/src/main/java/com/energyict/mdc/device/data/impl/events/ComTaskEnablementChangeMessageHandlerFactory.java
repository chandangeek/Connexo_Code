/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implements the {@link MessageHandlerFactory} for the {@link ComTaskEnablementChangeMessageHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-30 (16:15)
 */
@Component(name = "com.energyict.mdc.device.data.comtaskenablement.messagehandlerfactory",
        property = {"subscriber=" + ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_SUBSCRIBER, "destination=" + ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION},
        service = MessageHandlerFactory.class,
        immediate = true)
public class ComTaskEnablementChangeMessageHandlerFactory implements MessageHandlerFactory {

    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceService deviceService;
    private volatile DeviceDataModelService deviceDataModelService;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public ComTaskEnablementChangeMessageHandlerFactory() {
    }

    @Override
    public MessageHandler newMessageHandler() {
        ComTaskEnablementChangeMessageHandler.ComTaskEnablementConfig comTaskEnablementConfig = new ComTaskEnablementChangeMessageHandler.ComTaskEnablementConfig(messageService, jsonService, thesaurus, ((ServerDeviceService) deviceService), deviceDataModelService, deviceConfigurationService);
        return new ComTaskEnablementChangeMessageHandler(jsonService, comTaskEnablementConfig);
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }
}