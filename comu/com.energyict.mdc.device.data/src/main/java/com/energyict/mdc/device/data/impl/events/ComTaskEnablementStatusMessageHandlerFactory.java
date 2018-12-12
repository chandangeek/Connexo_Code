/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implements the {@link MessageHandlerFactory} for the {@link ComTaskEnablementStatusMessageHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-30 (16:54)
 */
@Component(name = "com.energyict.mdc.device.data.update.comtaskenablement.status.messagehandlerfactory", property = {"subscriber=CTESMH", "destination=" + EventService.JUPITER_EVENTS}, service = MessageHandlerFactory.class, immediate = true)
public class ComTaskEnablementStatusMessageHandlerFactory implements MessageHandlerFactory{

    public static final String SUBSCRIBER_NAME = "CTESMH";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle status updates on communication task configuration";
    private volatile JsonService jsonService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ServerCommunicationTaskService serverCommunicationTaskService;

    public ComTaskEnablementStatusMessageHandlerFactory() {
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ComTaskEnablementStatusMessageHandler(jsonService, this.deviceConfigurationService, this.serverCommunicationTaskService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setServerCommunicationTaskService(ServerCommunicationTaskService serverCommunicationTaskService) {
        this.serverCommunicationTaskService = serverCommunicationTaskService;
    }
}