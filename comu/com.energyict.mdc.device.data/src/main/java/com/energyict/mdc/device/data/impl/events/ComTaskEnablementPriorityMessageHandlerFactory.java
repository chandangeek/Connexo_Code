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
 * Implements the {@link MessageHandlerFactory} for the {@link ComTaskEnablementPriorityMessageHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-30 (16:54)
 */
@Component(name = "com.energyict.mdc.device.data.update.comtaskenablement.priority.messagehandlerfactory", property = {"subscriber=CTEPMH", "destination=" + EventService.JUPITER_EVENTS}, service = MessageHandlerFactory.class, immediate = true)
public class ComTaskEnablementPriorityMessageHandlerFactory implements MessageHandlerFactory {

    public static final String SUBSCRIBER_NAME = "CTEPMH";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle priority updates on communication task configuration";

    private volatile JsonService jsonService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ServerCommunicationTaskService serverCommunicationTaskService;

    public ComTaskEnablementPriorityMessageHandlerFactory() {
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ComTaskEnablementPriorityMessageHandler(jsonService, deviceConfigurationService, serverCommunicationTaskService);
    }

    @Reference
    public void setServerCommunicationTaskService(ServerCommunicationTaskService serverCommunicationTaskService) {
        this.serverCommunicationTaskService = serverCommunicationTaskService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }
}